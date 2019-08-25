package com.hangzhang.gmall.gmallsearchservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.beans.PmsSearchParam;
import com.beans.PmsSearchSkuInfo;
import com.beans.PmsSkuAttrValue;
import com.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    JestClient jestClient;
    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
        String dslStr = getSearchDsl(pmsSearchParam);
//        System.out.println(dslStr);
        Search build = new Search.Builder(dslStr).addIndex("gmallpms").addType("pmsSkuInfo").build();
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
            //查询结构解析
            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit :
                    hits) {
                //total等等
                PmsSearchSkuInfo source = hit.source;
                pmsSearchSkuInfos.add(source);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(pmsSearchSkuInfos.size());
        return pmsSearchSkuInfos;
    }

    private String getSearchDsl(PmsSearchParam pmsSearchParam) {
        List<PmsSkuAttrValue> skuAttrValueList = pmsSearchParam.getSkuAttrValueList();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();

        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //query
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter
        if (!StringUtils.isBlank(catalog3Id)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
//            System.out.println("---"+catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (skuAttrValueList != null) {//判断是否有平台属性
            for (PmsSkuAttrValue pmsSkuAttrValue :
                    skuAttrValueList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", pmsSkuAttrValue.getValueId());
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }


        //must
        if (!StringUtils.isBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);
        //from 分页查询
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight 高亮
        searchSourceBuilder.highlight(null);
        //sort
        searchSourceBuilder.sort("id", SortOrder.DESC);
        String dslStr = searchSourceBuilder.toString();


        return dslStr;

    }
}
