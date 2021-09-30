package cn.edu.zjut.service;

import cn.edu.zjut.common.dto.SeckillSkuRedisDTO;

import java.util.List;

public interface SeckillService {

    void uploadSeckillSkuInDays(Integer days);

    List<SeckillSkuRedisDTO> getCurrentSeckillSkus();

    // SeckillSkuRedisTo getSeckillSkuInfo(Long skuId);
    //
    // String kill(String killId, String key, Integer num) throws InterruptedException;

}
