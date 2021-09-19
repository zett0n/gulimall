package cn.edu.zjut.search.service.impl;

import cn.edu.zjut.common.dto.es.SkuEsDTO;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.search.config.ElasticSearchConfig;
import cn.edu.zjut.search.constant.EsConstant;
import cn.edu.zjut.search.feign.ProductFeignService;
import cn.edu.zjut.search.service.SearchService;
import cn.edu.zjut.search.vo.AttrResponseVO;
import cn.edu.zjut.search.vo.SearchParam;
import cn.edu.zjut.search.vo.SearchResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient esRestClient;

    @Resource
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        // 动态构建出查询需要的DSL语句
        SearchResult result = null;

        // 1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            // 2、执行检索请求
            SearchResponse response = this.esRestClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            // 3、分析响应数据，封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 准备检索请求
     * 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        // 检索请求构建
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /*
         * 查询：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
         */

        // 1. 构建 bool-query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 1.1 bool-must 模糊匹配
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 1.2.1 bool-filter catalogId 按照三级分类id查询
        if (null != param.getCatalog3Id()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 1.2.2 bool-filter brandId 按照品牌id查询
        if (null != param.getBrandId() && param.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.2.3 bool-filter attrs 按照指定的属性查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            param.getAttrs().forEach(item -> {
                //attrs=1_5寸:8寸&2_16G:8G
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

                //attrs=1_5寸:8寸
                String[] s = item.split("_");
                String attrId = s[0]; // 检索的属性id
                String[] attrValues = s[1].split(":");//这个属性检索用的值
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                // 每一个属性都要生成一个 nested 查询
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            });

        }
        // 1.2.4 bool-filter hasStock 按照是否有库存查询
        if (null != param.getHasStock()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        // 1.2.5 skuPrice bool-filter 按照价格区间查询
        if (StringUtils.isNotEmpty(param.getSkuPrice())) {
            //skuPrice形式为：1_500或_500或500_
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] price = param.getSkuPrice().split("_");
            if (price.length == 2) {
                rangeQueryBuilder.gte(price[0]).lte(price[1]);
            } else if (price.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQueryBuilder.lte(price[1]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQueryBuilder.gte(price[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        // 封装所有的查询条件
        searchSourceBuilder.query(boolQueryBuilder);

        /*
         * 排序，分页，高亮
         */

        // 2.1 排序  形式为sort=hotScore_asc/desc
        if (StringUtils.isNotEmpty(param.getSort())) {
            String sort = param.getSort();
            // sort=hotScore_asc/desc
            String[] sortFields = sort.split("_");

            SortOrder sortOrder = "asc".equalsIgnoreCase(sortFields[1]) ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(sortFields[0], sortOrder);
        }

        // 2.2 分页 from = (pageNum - 1) * pageSize
        searchSourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);

        // 2.3 高亮
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");

            searchSourceBuilder.highlighter(highlightBuilder);
        }

        // System.out.println("构建的DSL语句" + searchSourceBuilder);

        /*
         * 聚合分析
         */

        // 1. 按照品牌进行聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);

        // 1.1 品牌的子聚合-品牌名聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        // 1.2 品牌的子聚合-品牌图片聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);

        // 2. 按照分类信息进行聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);

        // 3. 按照属性信息进行聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // 3.1 按照属性ID进行聚合
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_agg.subAggregation(attr_id_agg);
        // 3.1.1 在每个属性ID下，按照属性名进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 3.1.2 在每个属性ID下，按照属性值进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        searchSourceBuilder.aggregation(attr_agg);

        log.debug("构建的DSL语句 {}", searchSourceBuilder);

        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }


    /**
     * 构建结果数据
     * 模糊匹配，过滤（按照属性、分类、品牌，价格区间，库存），完成排序、分页、高亮,聚合分析功能
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {

        SearchResult result = new SearchResult();

        //1、返回的所有查询到的商品
        SearchHits hits = response.getHits();

        List<SkuEsDTO> esModels = new ArrayList<>();
        //遍历所有商品信息
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsDTO esModel = JSON.parseObject(sourceAsString, SkuEsDTO.class);

                //判断是否按关键字检索，若是就显示高亮，否则不显示
                if (StringUtils.isNotEmpty(param.getKeyword())) {
                    //拿到高亮信息显示标题
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String skuTitleValue = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(skuTitleValue);
                }
                esModels.add(esModel);
            }
        }
        result.setProduct(esModels);

        //2、当前商品涉及到的所有属性信息
        List<SearchResult.AttrVO> attrVOs = new ArrayList<>();
        //获取属性信息的聚合
        ParsedNested attrsAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVO attrVO = new SearchResult.AttrVO();
            //1、得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVO.setAttrId(attrId);

            //2、得到属性的名字
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVO.setAttrName(attrName);

            //3、得到属性的所有值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().
                    map(MultiBucketsAggregation.Bucket::getKeyAsString)
                    .collect(Collectors.toList());
            attrVO.setAttrValue(attrValues);

            attrVOs.add(attrVO);
        }

        result.setAttrs(attrVOs);

        //3、当前商品涉及到的所有品牌信息
        List<SearchResult.BrandVO> brandVOs = new ArrayList<>();
        //获取到品牌的聚合
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVO brandVO = new SearchResult.BrandVO();

            //1、得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVO.setBrandId(brandId);

            //2、得到品牌的名字
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVO.setBrandName(brandName);

            //3、得到品牌的图片
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVO.setBrandImg(brandImg);

            brandVOs.add(brandVO);
        }
        result.setBrands(brandVOs);

        //4、当前商品涉及到的所有分类信息
        //获取到分类的聚合
        List<SearchResult.CatalogVO> catalogVOs = new ArrayList<>();
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVO catalogVO = new SearchResult.CatalogVO();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVO.setCatalogId(Long.parseLong(keyAsString));

            //得到分类名
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVO.setCatalogName(catalogName);
            catalogVOs.add(catalogVO);
        }
        result.setCatalogs(catalogVOs);

        //===============以上可以从聚合信息中获取====================//

        //5、分页信息-页码
        result.setPageNum(param.getPageNum());
        //5、1分页信息、总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);

        //5、2分页信息-总页码-计算
        int totalPages = (int) total % EsConstant.PRODUCT_PAGE_SIZE == 0 ?
                (int) total / EsConstant.PRODUCT_PAGE_SIZE : ((int) total / EsConstant.PRODUCT_PAGE_SIZE + 1);
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        //6、构建面包屑导航
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVO> collect = param.getAttrs().stream().map(attr -> {
                //1、分析每一个attrs传过来的参数值
                SearchResult.NavVO navVO = new SearchResult.NavVO();
                String[] s = attr.split("_");
                navVO.setNavValue(s[1]);
                R r = this.productFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVO data = r.getData("attr", new TypeReference<AttrResponseVO>() {
                    });
                    navVO.setNavName(data.getAttrName());
                } else {
                    navVO.setNavName(s[0]);
                }

                //2、取消了这个面包屑以后，我们要跳转到哪个地方，将请求的地址url里面的当前置空
                //拿到所有的查询条件，去掉当前
                String encode;
                try {
                    encode = URLEncoder.encode(attr, "UTF-8");
                    encode.replace("+", "%20");  //浏览器对空格的编码和Java不一样，差异化处理
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = param.get_queryString().replace("&attrs=" + attr, "");
                navVO.setLink("http://search.gulimall.com/list.html?" + replace);

                return navVO;
            }).collect(Collectors.toList());
            result.setNavs(collect);
        }
        return result;
    }

}
