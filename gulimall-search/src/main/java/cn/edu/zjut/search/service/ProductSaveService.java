package cn.edu.zjut.search.service;


import cn.edu.zjut.common.dto.es.SkuEsDTO;

import java.io.IOException;
import java.util.List;

/**
 * @author: zhangshuaiyin
 * @date: 2021/3/11 15:22
 */
public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsDTO> skuEsDTOs) throws IOException;
}
