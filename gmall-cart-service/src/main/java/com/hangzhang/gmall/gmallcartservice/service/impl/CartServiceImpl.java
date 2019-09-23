package com.hangzhang.gmall.gmallcartservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.beans.OmsCartItem;
import com.hangzhang.gmall.gmallcartservice.mapper.OmsCartItemMapper;
import com.hangzhang.gmall.util.RedisUtil;
import com.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    OmsCartItemMapper omsCartItemMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem ifCartExitsByUser(String memberId,String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        //判断是否有用户ID
        if(StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insertSelective(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemnew) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id",omsCartItemnew.getId());
        //有字段才更新
        omsCartItemMapper.updateByExample(omsCartItemnew,e);
    }

    @Override
    public void flushCartCache(String memberId) {
       //操作频率很高，是否只需要更新缓存
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> select = omsCartItemMapper.select(omsCartItem);
        //同步缓存
        Jedis jedis = redisUtil.getJedis();
        //购物车的缓存结构
        /**
         * 用户id   购物车集合  购物车缓存中某一个数据更新
         *
         * */
        Map<String,String> map = new HashMap<>();
        for (OmsCartItem cartItem : select) {
            //每次都单个sku商品的计算总价格
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }
        //先删后添加
        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:"+memberId+":cart",map);
        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        try{
            jedis = redisUtil.getJedis();

            List<String> hvals = jedis.hvals("user:" + userId + ":cart");
            if(hvals.size()>0){
                for (String hval : hvals) {
                    OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                    omsCartItems.add(omsCartItem);
                }
            }else{
                OmsCartItem omsCartItem = new OmsCartItem();
                omsCartItem.setMemberId(userId);
                omsCartItems = omsCartItemMapper.select(omsCartItem);
            }


        }catch (Exception e){
            e.printStackTrace();
            return null;
            //处理异常，记录系统日志
            //e.getmessage() logservice.addErrlog()
        }finally {
            jedis.close();
        }


        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,example);
        //缓存同步
        flushCartCache(omsCartItem.getMemberId());

    }
}
