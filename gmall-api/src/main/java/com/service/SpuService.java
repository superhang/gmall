package com.service;

import com.beans.PmsProductImage;
import com.beans.PmsProductInfo;
import com.beans.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> spulist(String catalog3Id);


    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);

//    PmsSkuInfo getSkuById(String skuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuid);
}
