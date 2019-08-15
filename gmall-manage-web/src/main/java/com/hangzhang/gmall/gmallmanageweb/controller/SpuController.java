package com.hangzhang.gmall.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.beans.PmsProductImage;
import com.beans.PmsProductInfo;
import com.beans.PmsProductSaleAttr;
import com.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import utils.PmsUploadUtil;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {
    @Reference
    SpuService spuService;


    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spulist (String catalog3Id){
        return spuService.spulist(catalog3Id);
    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    //与前端参数对应，进行了修改
    public String saveSpuInfo (@RequestBody PmsProductInfo pmsProductInfo){

        return spuService.saveSpuInfo(pmsProductInfo);
    }
    @RequestMapping("fileUpload")
    @ResponseBody
    //保存图片  类型转换
    public String fileUpload (@RequestParam("file")  MultipartFile multipartFile){
        //将上传文件上传到分布式文件存储系统 fastdfs
        String imgurl =  PmsUploadUtil.uploadImage(multipartFile);
        //将图片存储路径返回给前端
        return imgurl;
    }
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList (String spuId){
        return spuService.spuSaleAttrList(spuId);
    }
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList (String spuId){
        return spuService.spuImageList(spuId);
    }



}
