package cn.edu.zjut.product.web;

import cn.edu.zjut.product.entity.CategoryEntity;
import cn.edu.zjut.product.service.CategoryService;
import cn.edu.zjut.product.vo.Catalog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html", "/index"})
    public String indexPage(Model model) {
        List<CategoryEntity> categoryEntities = this.categoryService.getRootCategories();
        model.addAttribute("categories", categoryEntities);

        return "index";
    }

    /**
     * 二级、三级分类数据
     */
    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String, List<Catalog2VO>> getCatalogJSON() {
        // return this.categoryService.getCatalogJSONFromDB();
        // return this.categoryService.getCatalogJSONFromRedis();
        // return this.categoryService.getCatalogJSONFromRedisWithLocalLock();
        return this.categoryService.getCatalogJSONFromRedisWithRedisLock();
    }
}
