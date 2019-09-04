package com.hangzhang.gmall.gmallsearchweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.beans.*;
import com.service.AttrService;
import com.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

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
        map.put("skuLsInfoList",pmsSearchSkuInfoList);
        //mysql中查询属性列表
        List<PmsBaseAttrInfo> pmsBaseAttrInfos= attrService.getAttrValueListByValueId(valueIdSet);
        map.put("attrList",pmsBaseAttrInfos);

        //去除当前url中valueid所在的属性组
        String[] valueId = pmsSearchParam.getValueId();
        if(valueId!=null){

            for (String s : valueId) {
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                while(iterator.hasNext()){
                    PmsBaseAttrInfo next = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = next.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String id = pmsBaseAttrValue.getId();
                            if(s.equals(id)){
                                //删除当前valueId所在的属性组
                                iterator.remove();
                            }
                        }
                    }
            }
        }


        //添加url
        String urlParam = getUrlParam(pmsSearchParam);
        map.put("urlParam",urlParam);


        //添加keyword
        String keyword = pmsSearchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            map.put("keyword",keyword);
        }

        //面包屑
        List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
        if(valueId!=null){
            //当前请求中包含属性的参数，每一个属性参数，都会生成一个面包屑
            for (String id : valueId) {
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //生成面包屑的参数
                pmsSearchCrumb.setValueId(id);
                pmsSearchCrumb.setValueName(id);
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam,id));
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
        }

        map.put("attrValueSelectedList",pmsSearchCrumbs);

        return "list";
    }

    /**
     * 依据传递参数进行url拼接(面包屑)  可变长度传参
     * */
    private String getUrlParam(PmsSearchParam pmsSearchParam,String ...delvalueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();
        String urlParam = "";
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"keyword="+keyword;
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"catalog3Id="+catalog3Id;
        }
        //累计添加的bug
        if(skuAttrValueList!=null){
            for (String valueId : skuAttrValueList) {
                if(!valueId.equals(delvalueId)){
                    urlParam = urlParam+"&valueId="+valueId;
                }
            }

        }
        return urlParam;
    }
}
