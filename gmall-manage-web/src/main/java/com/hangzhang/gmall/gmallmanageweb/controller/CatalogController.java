package com.hangzhang.gmall.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.beans.PmsBaseCatalog1;
import com.beans.PmsBaseCatalog2;
import com.beans.PmsBaseCatalog3;
import com.service.CatelogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
//跨域问题
@CrossOrigin
public class CatalogController {

    @Reference
    CatelogService catelogService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){
        List<PmsBaseCatalog1> catalog1List =  catelogService.getCatelog1();
        return  catalog1List;
    }
    @RequestMapping("getCatalog2")
    @ResponseBody
    //查看前端请求接口，参数格式问题
    public List<PmsBaseCatalog2> getCatalog2( String catalog1Id){
        List<PmsBaseCatalog2> catalog2List =  catelogService.getCatelog2(catalog1Id);
        return  catalog2List;
    }
    @RequestMapping("getCatalog3")
    @ResponseBody
    //查看前端请求接口，参数格式问题
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id){
        List<PmsBaseCatalog3> catalog3List =  catelogService.getCatelog3(catalog2Id);
        return  catalog3List;
    }
}
