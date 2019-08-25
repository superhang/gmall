package com.hangzhang.gmall.gmallsearchweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.beans.PmsSearchParam;
import com.beans.PmsSearchSkuInfo;
import com.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.awt.*;
import java.util.List;

@Controller
public class SearchController {
    @Reference
    SearchService searchService;
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
        map.put("skuLsInfoList",pmsSearchSkuInfoList);
        return "list";
    }
}
