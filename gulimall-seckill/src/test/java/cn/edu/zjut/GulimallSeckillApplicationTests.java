package cn.edu.zjut;

import cn.edu.zjut.common.dto.SeckillSkuRedisDTO;
import cn.edu.zjut.service.SeckillService;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallSeckillApplicationTests {

    @Autowired
    private SeckillService seckillService;

    @Test
    public void testJSONParse() {
        String str = "{\n" +
                // "  \"endTime\": 1633535999000,\n" +
                // "  \"id\": 2,\n" +
                // "  \"promotionSessionId\": 2,\n" +
                // "  \"randomCode\": \"dc658cb0b0a44749adfaedcc06006435\",\n" +
                // "  \"seckillCount\": 2,\n" +
                // "  \"seckillLimit\": 2,\n" +
                // "  \"seckillPrice\": 2,\n" +
                // "  \"seckillSort\": 2,\n" +
                // "  \"skuId\": 17,\n" +
                // "  \"skuInfoDTO\": {\n" +
                // "    \"brandId\": 4,\n" +
                // "    \"catalogId\": 225,\n" +
                // "    \"price\": 4000.0,\n" +
                // "    \"saleCount\": 0,\n" +
                // "    \"skuDefaultImg\": \"https://zett0n-gulimall.oss-cn-hangzhou.aliyuncs.com/2021-08-29/b269c39e-1be8-4dc1-8d63-944455d12937_23d9fbb256ea5d4a.jpg\",\n" +
                // "    \"skuId\": 17,\n" +
                // "    \"skuName\": \"华为 HUAWEI Mate 30 Pro 白色 4GB\",\n" +
                // "    \"skuSubtitle\": \"现货抢购\",\n" +
                // "    \"skuTitle\": \"华为 HUAWEI Mate 30 Pro 白色 4GB\",\n" +
                // "    \"spuId\": 7\n" +
                // "  },\n" +
                // "  \"startTime\": 1633162111000\n" +
                "}";
        System.out.println(JSON.parseObject(str, SeckillSkuRedisDTO.class));
    }

    @Test
    public void testSeckillService() {
        this.seckillService.uploadSeckillSkuInDays(3);
        System.out.println(this.seckillService.getSeckillSkuInfo(16L));
    }

}
