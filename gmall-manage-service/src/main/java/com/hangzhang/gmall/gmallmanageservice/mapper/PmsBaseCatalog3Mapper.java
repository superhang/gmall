package com.hangzhang.gmall.gmallmanageservice.mapper;

import com.beans.PmsBaseCatalog3;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsBaseCatalog3Mapper extends Mapper<PmsBaseCatalog3> {
     List<PmsBaseCatalog3> selectBycatalog2_id(String catalog2_id);
}
