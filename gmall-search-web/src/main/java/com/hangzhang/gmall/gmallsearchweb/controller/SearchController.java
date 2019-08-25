package com.hangzhang.gmall.gmallsearchweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.beans.PmsBaseAttrInfo;
import com.beans.PmsSearchParam;
import com.beans.PmsSearchSkuInfo;
import com.beans.PmsSkuAttrValue;
import com.service.AttrService;
import com.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class SearchController {
    @Reference
    SearchService searchService;
    @Reference
    AttrService attrService;


    @RequestMapping("index")
    public String index(){
        return "index";
    }
    /**
     * 三级分类id，关键字，平台属性集合
     * */
    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap map){
        //调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.list(pmsSearchParam);

        //平台属性取出，去重

        //抽取检索结果所包含的平台属性集合
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo:
        pmsSearchSkuInfoList) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        //mysql中查询
        List<PmsBaseAttrInfo> pmsBaseAttrInfos= attrService.getAttrValueListByValueId(valueIdSet);
        map.put("skuLsInfoList",pmsSearchSkuInfoList);
        map.put("attrList",pmsBaseAttrInfos);
        return "list";
    }
}
