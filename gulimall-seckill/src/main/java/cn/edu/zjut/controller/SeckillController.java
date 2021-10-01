package cn.edu.zjut.controller;

import cn.edu.zjut.common.dto.SeckillSkuRedisDTO;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;


    /**
     * 当前时间可以参与秒杀的商品信息
     */
    @ResponseBody
    @GetMapping("/currentSkus")
    public R getCurrentSeckillSkus() {
        // 获取到当前可以参加秒杀商品的信息
        List<SeckillSkuRedisDTO> seckillSkuRedisDTOs = this.seckillService.getCurrentSeckillSkus();

        return R.ok().put("data", seckillSkuRedisDTOs);
    }


    @ResponseBody
    @GetMapping("/sku/{skuId}")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisDTO seckillSkuRedisDTO = this.seckillService.getSeckillSkuInfo(skuId);

        return R.ok().put("data", seckillSkuRedisDTO);
    }


    @GetMapping("/kill")
    public String kill(@RequestParam("killId") String key, @RequestParam("key") String code,
                       @RequestParam("num") Integer num, Model model) {
        String orderSn;
        try {
            orderSn = this.seckillService.kill(key, code, num);
            model.addAttribute("orderSn", orderSn);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";
    }

}
