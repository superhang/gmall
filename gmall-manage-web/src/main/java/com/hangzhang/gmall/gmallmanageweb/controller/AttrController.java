package com.hangzhang.gmall.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.beans.PmsBaseAttrInfo;
import com.beans.PmsBaseAttrValue;
import com.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class AttrController {
    @Reference
    AttrService attrService;
    @RequestMapping("attrInfoList")
    @ResponseBody
   public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){
       List<PmsBaseAttrInfo> attrInfoList = attrService.attrInfoList(catalog3Id);
       return attrInfoList;
   }
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){

        return attrService.saveAttrInfo(pmsBaseAttrInfo);
    }
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){

        return attrService.getAttrValueList(attrId);
    }
}
