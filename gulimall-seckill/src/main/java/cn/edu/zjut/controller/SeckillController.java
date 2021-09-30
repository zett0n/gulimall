package cn.edu.zjut.controller;

import cn.edu.zjut.common.dto.SeckillSkuRedisDTO;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 当前时间可以参与秒杀的商品信息
     */
    @ResponseBody
    @GetMapping("/getCurrentSeckillSkus")
    public R getCurrentSeckillSkus() {
        // this.seckillService.uploadSeckillSkuInDays(3);
        // try {
        //     TimeUnit.SECONDS.sleep(2);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
        // 获取到当前可以参加秒杀商品的信息
        List<SeckillSkuRedisDTO> seckillSkuRedisDTOs = this.seckillService.getCurrentSeckillSkus();

        return R.ok().put("data", seckillSkuRedisDTOs);
    }

    // @ResponseBody
    // @GetMapping(value = "/getSeckillSkuInfo/{skuId}")
    // public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId) {
    //     SeckillSkuRedisTo to = seckillService.getSeckillSkuInfo(skuId);
    //     return R.ok().setData(to);
    // }
    //
    //
    // @GetMapping("/kill")
    // public String kill(@RequestParam("killId") String killId,
    //                    @RequestParam("key")String key,
    //                    @RequestParam("num")Integer num,
    //                    Model model) {
    //     String orderSn= null;
    //     try {
    //         orderSn = seckillService.kill(killId, key, num);
    //         model.addAttribute("orderSn", orderSn);
    //     } catch (InterruptedException e) {
    //         e.printStackTrace();
    //     }
    //     return "success";
    // }
}
