package cn.edu.zjut.order.web;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import cn.edu.zjut.order.service.OrderService;
import cn.edu.zjut.order.vo.OrderConfirmVO;
import cn.edu.zjut.order.vo.OrderSubmitVO;
import cn.edu.zjut.order.vo.SubmitOrderResponseVO;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单确认页面
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVO orderConfirmVO = this.orderService.confirmOrder();
        model.addAttribute("confirmOrder", orderConfirmVO);

        return "confirm";
    }

    /**
     * 下单操作
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVO orderSubmitVO, Model model) {
        SubmitOrderResponseVO vo = this.orderService.submitOrder(orderSubmitVO);
        if (vo.getCode() == 0) {
            // 下单成功，跳转到支付页面
            model.addAttribute("order", vo.getOrder());
            return "pay";
        } else {
            // 下单失败，重新确认
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
