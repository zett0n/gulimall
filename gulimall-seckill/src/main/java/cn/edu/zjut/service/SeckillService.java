package cn.edu.zjut.service;

import cn.edu.zjut.common.dto.SeckillSkuRedisDTO;

import java.util.List;

public interface SeckillService {

    void uploadSeckillSkuInDays(Integer days);

    List<SeckillSkuRedisDTO> getCurrentSeckillSkus();

    SeckillSkuRedisDTO getSeckillSkuInfo(Long skuId);

    String kill(String key, String code, Integer num) throws InterruptedException;

}
