package com.hangzhang.gmall.gmallitemweb.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.beans.PmsProductSaleAttr;
import com.beans.PmsSkuInfo;
import com.beans.PmsSkuSaleAttrValue;
import com.service.SkuService;
import com.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {
    @Reference
    SpuService spuService;
    @Reference
    SkuService skuService;
    @RequestMapping("index")
    public String index(ModelMap modelMap){
        List<String> ls = new ArrayList<>();
        for(int i=0; i<5; i++){
            ls.add("循环数据"+i);
        }
        modelMap.put("list",ls);
        modelMap.put("hello","hello superhang");
        return "index";
    }
    @RequestMapping("{skuId}.html")
    public String item(@PathVariable  String skuId,ModelMap map){
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        //sku对象
        map.put("skuInfo",pmsSkuInfo);
        //返回销售属性的列表
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),pmsSkuInfo.getId());
        map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);
        //查询当前sku的spu的其它sku的集合的hash表
        final HashMap<String, String> stringStringHashMap = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos   =   skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());
        for (PmsSkuInfo pmsSkuInfo1:
        pmsSkuInfos) {
            String k = "";
            String v = pmsSkuInfo1.getId();

            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo1.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue:
            skuSaleAttrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";
            }
            stringStringHashMap.put(k,v);
        }
        //将sku集合放到hash页
        String jsonString = JSON.toJSONString(stringStringHashMap);
        map.put("skujsonString",jsonString);
        return "item";
    }

}
