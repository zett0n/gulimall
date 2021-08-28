package cn.edu.zjut.product;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cn.edu.zjut.product.entity.BrandEntity;
import cn.edu.zjut.product.service.BrandService;
import cn.edu.zjut.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testBrandService() {
        BrandEntity brandEntity = new BrandEntity();
        // brandEntity.setName("xiaomi小米");
        // brandService.save(brandEntity);
        // System.out.println("ok...");

        brandEntity.setBrandId(1L);
        brandEntity.setDescript("miui12.5plus");
        this.brandService.updateById(brandEntity);
    }

    @Test
    public void testFindCatelogPath() {
        Long[] catelogPath = this.categoryService.findCatelogPath(225L);
        log.debug("catelogPath: {}", Arrays.asList(catelogPath));
    }

}
