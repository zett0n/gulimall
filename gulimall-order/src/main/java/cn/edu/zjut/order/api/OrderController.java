package cn.edu.zjut.order.api;

import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.order.entity.OrderEntity;
import cn.edu.zjut.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 订单
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:14:57
 */
@RestController
@RequestMapping("order/order")
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * @param OrderSn 订单号
     * @return 订单信息
     */
    @GetMapping("/infoByOrderSn/{OrderSn}")
    public R infoByOrderSn(@PathVariable("OrderSn") String OrderSn) {
        try {
            OrderEntity order = this.orderService.getOrderByOrderSn(OrderSn);
            return R.ok().put("order", order);
        } catch (Exception e) {
            return R.error(EmBizError.UNKNOWN_EXCEPTION);
        }
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = this.orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        OrderEntity order = this.orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody OrderEntity order) {
        this.orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody OrderEntity order) {
        this.orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        this.orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
