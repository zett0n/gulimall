package cn.edu.zjut.search.service.impl;

import cn.edu.zjut.common.dto.es.SkuEsDTO;
import cn.edu.zjut.search.config.ElasticSearchConfig;
import cn.edu.zjut.search.constant.EsConstant;
import cn.edu.zjut.search.service.ProductSaveService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: zhangshuaiyin
 * @date: 2021/3/11 15:22
 */
@Slf4j
@Service("productSaveService")
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient esRestClient;

    @Override
    public boolean productStatusUp(List<SkuEsDTO> skuEsDTOs) throws IOException {

        // 1.在 Es 中建立索引，建立映射关系（classpath: new_product_mapping.txt）

        // 2. 在 ES 中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsDTO skuEsDTO : skuEsDTOs) {
            //构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);

            indexRequest.id(skuEsDTO.getSkuId().toString());

            String jsonString = JSON.toJSONString(skuEsDTO);
            indexRequest.source(jsonString, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }

        BulkResponse bulk = this.esRestClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);

        // TODO 如果有错误，需要处理
        boolean hasFailures = bulk.hasFailures();

        List<String> collect = Arrays.stream(bulk.getItems())
                .map(BulkItemResponse::getId)
                .collect(Collectors.toList());

        log.info("商品上架完成：{}", collect);

        return hasFailures;
    }
}
