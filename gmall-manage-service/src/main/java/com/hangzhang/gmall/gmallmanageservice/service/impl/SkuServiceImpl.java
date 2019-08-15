package com.hangzhang.gmall.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.beans.PmsSkuAttrValue;
import com.beans.PmsSkuImage;
import com.beans.PmsSkuInfo;
import com.beans.PmsSkuSaleAttrValue;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsSkuAttrValueMapper;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsSkuImageMapper;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsSkuInfoMapper;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsSkuSaleAttrValueMapper;
import com.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;

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
}
