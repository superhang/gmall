package com.hangzhang.gmall.gmallmanageservice.mapper;

import com.beans.PmsBaseSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
@org.apache.ibatis.annotations.Mapper
public interface PmsBaseSaleAttrMapper extends Mapper<PmsBaseSaleAttr> {
    List<PmsBaseSaleAttr> SELECTALL();
}
