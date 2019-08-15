package com.hangzhang.gmall.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.beans.PmsBaseCatalog1;
import com.beans.PmsBaseCatalog2;
import com.beans.PmsBaseCatalog3;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsBaseCatalog1Mapper;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsBaseCatalog2Mapper;
import com.hangzhang.gmall.gmallmanageservice.mapper.PmsBaseCatalog3Mapper;
import com.service.CatelogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class CatelogServiceImpl implements CatelogService {
    @Autowired
    PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;
    @Autowired
    PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;
    @Autowired
    PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatelog1() {
        List<PmsBaseCatalog1> PmsBaseCatalog1List =  pmsBaseCatalog1Mapper.selectAll();
        return PmsBaseCatalog1List;
    }

    @Override
    public List<PmsBaseCatalog2> getCatelog2(String id) {
        PmsBaseCatalog2 p2 = new PmsBaseCatalog2();
        p2.setCatalog1Id(id);
        List<PmsBaseCatalog2> PmsBaseCatalog2List =  pmsBaseCatalog2Mapper.select(p2);
        return PmsBaseCatalog2List;
    }

    @Override
    public List<PmsBaseCatalog3> getCatelog3(String id) {
//        PmsBaseCatalog3 p3 = new PmsBaseCatalog3();
//        p3.setCatalog2Id(id);
//        List<PmsBaseCatalog3> PmsBaseCatalog3List = pmsBaseCatalog3Mapper.select(p3) ;
//        return PmsBaseCatalog3List;
      List<PmsBaseCatalog3> PmsBaseCatalog3List = pmsBaseCatalog3Mapper.selectBycatalog2_id(id) ;
      return PmsBaseCatalog3List;
    }
}
