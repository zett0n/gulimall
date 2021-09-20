package cn.edu.zjut.product;

import cn.edu.zjut.product.dao.AttrGroupDao;
import cn.edu.zjut.product.dao.SkuSaleAttrValueDao;
import cn.edu.zjut.product.entity.BrandEntity;
import cn.edu.zjut.product.service.BrandService;
import cn.edu.zjut.product.service.CategoryService;
import cn.edu.zjut.product.vo.SkuItemSaleAttrVO;
import cn.edu.zjut.product.vo.SpuItemAttrGroupVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private SkuSaleAttrValueDao SkuSaleAttrValueDao;

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

    @Test
    public void testGetAttrGroupWithAttrsBySpuId() {
        List<SpuItemAttrGroupVO> spuItemAttrGroupVO = this.attrGroupDao.getAttrGroupWithAttrsBySpuId(227L, 225L);

        log.debug("spuItemAttrGroupVO: {}", spuItemAttrGroupVO);
    }

    @Test
    public void testGetSaleAttrsBySpuId() {
        List<SkuItemSaleAttrVO> SkuItemSaleAttrVOs = this.SkuSaleAttrValueDao.getSaleAttrsBySpuId(7L);

        log.debug("SkuItemSaleAttrVO List: {}", SkuItemSaleAttrVOs);
    }

}
