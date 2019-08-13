package com.service;

import com.beans.PmsBaseCatalog1;
import com.beans.PmsBaseCatalog2;
import com.beans.PmsBaseCatalog3;

import java.util.List;

public interface CatelogService {
    List<PmsBaseCatalog1> getCatelog1();

    List<PmsBaseCatalog2> getCatelog2(String id);
    List<PmsBaseCatalog3> getCatelog3(String id);
}
