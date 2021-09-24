package cn.edu.zjut.product.web;

import cn.edu.zjut.product.service.SkuInfoService;
import cn.edu.zjut.product.vo.SkuItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
@Slf4j
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = this.skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVO);

        return "item";
        // return "myitem";
    }
}
