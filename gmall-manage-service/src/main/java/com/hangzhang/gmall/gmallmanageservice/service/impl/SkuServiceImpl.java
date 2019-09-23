package com.hangzhang.gmall.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.beans.PmsSkuAttrValue;
import com.beans.PmsSkuImage;
import com.beans.PmsSkuInfo;
import com.beans.PmsSkuSaleAttrValue;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsSkuAttrValueMapper;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsSkuImageMapper;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsSkuInfoMapper;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsSkuSaleAttrValueMapper;
import com.hangzhang.gmall.util.RedisUtil;
import com.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //插入skuinfo
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuid = pmsSkuInfo.getId();
        //插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue:skuAttrValueList){
            pmsSkuAttrValue.setSkuId(skuid);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuAttrValue:skuSaleAttrValueList){
            pmsSkuAttrValue.setSkuId(skuid);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuAttrValue:skuImageList){
            pmsSkuAttrValue.setSkuId(skuid);
            pmsSkuImageMapper.insertSelective(pmsSkuAttrValue);
        }
    }
    public PmsSkuInfo getSkuByIdFromDb(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        //图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> select = pmsSkuImageMapper.select(pmsSkuImage);
        pmsSkuInfo1.setSkuImageList(select);
        return pmsSkuInfo1;
    }
    @Override
    public PmsSkuInfo getSkuById(String skuId,String remoteAddr) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接缓存
        final Jedis jedis = redisUtil.getJedis();
        //查询缓存
        String skuKey = "sku:"+skuId+":info";
        String skujson = jedis.get(skuKey);
        if(StringUtils.isNotBlank(skujson)){
            System.out.println("ip: "+remoteAddr+"从缓存中获取商品详情");
            pmsSkuInfo = JSON.parseObject(skujson, PmsSkuInfo.class);
            System.err.println( Thread.currentThread().getName()+"：命中缓存"  );
        }else {
            //如果缓存没有，再去查询数据库
            System.out.println("ip: "+remoteAddr+"缓存中没有，申请分布式锁"+Thread.currentThread().getName()+" sku:"+skuId+":lock");
            //设置分布式锁
            String token = UUID.randomUUID().toString();
            String ok = jedis.set("sku:"+skuId+":lock",token,"nx","px",10000);
            System.out.println(StringUtils.isNotBlank(ok)+"---"+ok);
            if(StringUtils.isNotBlank(ok)&&ok.equals("OK")){
                //设置成功，失效期前有权访问数据库
                System.out.println("ip: "+remoteAddr+Thread.currentThread().getName()+"10秒前有权访问数据库"+" sku:"+skuId+":lock");
                pmsSkuInfo= getSkuByIdFromDb(skuId);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.err.println( Thread.currentThread().getName()+"： 查询数据##################### ##" );
                if(pmsSkuInfo!=null){
                    //数据库查询结果存入redis
                    jedis.set("sku:"+skuId+":info",JSON.toJSONString(pmsSkuInfo));
                    System.err.println( Thread.currentThread().getName()+"：数据库缓存更新完毕############### #####" );
                }else{
                    //数据库中不存在该sku
                    //为了防止缓存穿透，将null值存入redis,3分钟失效
                    jedis.setex("sku:"+skuId+":info",3*60,JSON.toJSONString(""));
                }
                //访问MySQL后释放锁 测试用
                String lockToken = jedis.get("sku:"+skuId+":lock");
                if(StringUtils.isNotBlank(lockToken)&&lockToken.equals(token)){
                    //解决删除的时候key失效
//                    String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else " +
//                            "return 0 end";
//                    jedis.eval(script, Collections.singletonList("lock"),Collections.singletonList(token));
                    //确认删除的是自己的锁
                    jedis.del("sku:"+skuId+":lock");
                }
                System.out.println("ip: "+remoteAddr+"使用完毕，将锁归还"+Thread.currentThread().getName()+" sku:"+skuId+":lock");
            }else {
                System.out.println("ip: "+remoteAddr+"无权访问数据库，自旋"+Thread.currentThread().getName() );
                //无权访问数据库，自旋
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //线程问题
                jedis.close();
                return   getSkuById(skuId,remoteAddr);
            }


        }

        jedis.close();
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo:
        pmsSkuInfos) {
            String skuid = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuid);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(select);

        }
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productprice) {
        //不能校验缓存的数据，必须校验数据库实际数据
        boolean b = false;
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        BigDecimal price = pmsSkuInfo1.getPrice();
        if(price.compareTo(productprice)==0){
            b=true;
        }

        return b;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfoList;
    }
}
