package cn.edu.zjut.product;

import cn.edu.zjut.product.entity.BrandEntity;
import cn.edu.zjut.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        // brandEntity.setName("xiaomi小米");
        // brandService.save(brandEntity);
        // System.out.println("ok...");

        brandEntity.setBrandId(1L);
        brandEntity.setDescript("miui12.5plus");
        brandService.updateById(brandEntity);
    }

}
