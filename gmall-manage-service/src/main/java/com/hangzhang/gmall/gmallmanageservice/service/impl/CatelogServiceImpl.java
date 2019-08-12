package com.hangzhang.gmall.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.beans.PmsBaseCatalog1;
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
}
