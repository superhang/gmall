package com.service;

import com.beans.PmsSkuInfo;

import java.util.List;

public interface SkuService {
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    PmsSkuInfo getSkuById(String skuId);
}
