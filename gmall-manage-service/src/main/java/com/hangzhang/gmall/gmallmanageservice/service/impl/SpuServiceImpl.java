package com.hangzhang.gmall.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.beans.*;
import com.hangzhang.gmall.gmallmanageservice.mapper.*;
import com.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;
    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;
    @Autowired
    PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Override
    public List<PmsProductInfo> spulist(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        return pmsProductInfoMapper.select(pmsProductInfo);
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        System.out.println("###"+pmsProductInfo.getId());
        int i = pmsProductInfoMapper.insertSelective(pmsProductInfo);
        for (PmsProductSaleAttr pmsProductSaleAttr:pmsProductInfo.getSpuSaleAttrList()){
            int j = pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);
            if(j<=0){return "error";}else {
                for (PmsProductSaleAttrValue pmsProductSaleAttrValue:pmsProductSaleAttr.getSpuSaleAttrValueList()){
                    int k = pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
                    if(k<=0){return "error";}
                }
            }
        }
        for (PmsProductImage pmsProductImage:pmsProductInfo.getSpuImageList()){
            int z = pmsProductImageMapper.insertSelective(pmsProductImage);
            if(z<=0){return "error";}
        }
         return "success";

    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> select = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
        for (PmsProductSaleAttr pmsProductSaleAttr1:select){
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(pmsProductSaleAttr1.getSaleAttrId());//关键
            List<PmsProductSaleAttrValue> select1 = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            pmsProductSaleAttr1.setSpuSaleAttrValueList(select1);
        }
        return select;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        return pmsProductImageMapper.select(pmsProductImage);
    }



    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {
//        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
//        pmsProductSaleAttr.setProductId(productId);
//        List<PmsProductSaleAttr> select = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
//        for (PmsProductSaleAttr pmsProductSaleAttr1:
//        select) {
//            String saleAttrId = pmsProductSaleAttr1.getSaleAttrId();
//            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
//            pmsProductSaleAttrValue.setSaleAttrId(saleAttrId);
//            pmsProductSaleAttrValue.setProductId(productId);
//            List<PmsProductSaleAttrValue> select1 = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
//            pmsProductSaleAttr1.setSpuSaleAttrValueList(select1);
//        }
        List<PmsProductSaleAttr> select = pmsProductSaleAttrMapper.spuSaleAttrListCheckBySku(productId,skuId);
        return select;
    }


}
