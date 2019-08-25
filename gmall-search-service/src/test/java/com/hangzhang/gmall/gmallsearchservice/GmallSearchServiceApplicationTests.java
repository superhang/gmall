package com.hangzhang.gmall.gmallsearchservice;

import com.alibaba.dubbo.config.annotation.Reference;
import com.beans.PmsSearchSkuInfo;
import com.beans.PmsSkuInfo;
import com.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {
    @Reference
    SkuService skuService;
    @Autowired
    JestClient jestClient;
    @Test
    public void put() throws IOException{
        //查询mysql数据
        List<PmsSkuInfo> pmsSkuInfoList =skuService.getAllSku();
        //转为es数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo:
                pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }
        //导入es  数据  库名 表名 主键
        for (PmsSearchSkuInfo pmsSearchSkuInfo:
                pmsSearchSkuInfos) {
            Index build = new Index.Builder(pmsSearchSkuInfo).index("gmallpms").type("pmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(build);
        }
    }
    @Test
    public void contextLoads() throws IOException {
        //用api执行复杂查询
        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": {\n" +
                "        \"term\": {\n" +
                "          \"skuAttrValueList.valueId\": \"39\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"match\": {\n" +
                "      \"skuName\": \"黑鲨\"\n" +
                "           }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "    \n" +
                "  }\n" +
                "}";
        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //query
            //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            //filter
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id","61");
        boolQueryBuilder.filter(termQueryBuilder);
            //must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","黑鲨");
        boolQueryBuilder.must(matchQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        //from 分页查询
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight 高亮
        searchSourceBuilder.highlight(null);

        String dslStr = searchSourceBuilder.toString();

        Search build = new Search.Builder(dslStr).addIndex("gmallpms").addType("pmsSkuInfo").build();
        SearchResult execute = jestClient.execute(build);
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        //查询结构解析
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit:
        hits) {
            //total等等
            PmsSearchSkuInfo source = hit.source;
            pmsSearchSkuInfos.add(source);
        }
        System.out.println(pmsSearchSkuInfos.size());
    }

}
