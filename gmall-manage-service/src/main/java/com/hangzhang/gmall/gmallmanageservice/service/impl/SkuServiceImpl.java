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

import java.util.List;

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
    public PmsSkuInfo getSkuById(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接缓存
        final Jedis jedis = redisUtil.getJedis();
        //查询缓存
        String skuKey = "sku:"+skuId+":info";
        String skujson = jedis.get(skuKey);
        if(StringUtils.isNotBlank(skujson)){
            pmsSkuInfo = JSON.parseObject(skujson, PmsSkuInfo.class);
            System.err.println( Thread.currentThread().getName()+"：命中缓存"  );
        }else {
            //如果缓存没有，再去查询数据库
            pmsSkuInfo= getSkuByIdFromDb(skuId);
            System.err.println( Thread.currentThread().getName()+"： 查询数据##################### ##" );
            if(pmsSkuInfo!=null){
                //数据库查询结果存入redis
                jedis.set("sku:"+skuId+":info",JSON.toJSONString(pmsSkuInfo));
                System.err.println( Thread.currentThread().getName()+"：数据库缓存更新完毕############### #####" );
            }

        }

        jedis.close();
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfoList;
    }
}
