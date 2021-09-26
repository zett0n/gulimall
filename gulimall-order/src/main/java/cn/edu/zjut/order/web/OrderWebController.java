package cn.edu.zjut.order.web;

import cn.edu.zjut.common.exception.NoStockException;
import cn.edu.zjut.order.service.OrderService;
import cn.edu.zjut.order.vo.OrderConfirmVO;
import cn.edu.zjut.order.vo.OrderSubmitVO;
import cn.edu.zjut.order.vo.SubmitOrderResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

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
    public String submitOrder(OrderSubmitVO orderSubmitVO, Model model, RedirectAttributes attributes) {
        try {
            SubmitOrderResponseVO vo = this.orderService.submitOrder(orderSubmitVO);
            Integer code = vo.getCode();

            // 下单成功，跳转到支付页面
            if (code == 0) {
                model.addAttribute("order", vo.getOrder());
                return "pay";
            }
            // 下单失败，重新确认
            String msg = "下单失败：";
            switch (code) {
                case 1:
                    msg += "防重令牌校验失败";
                    break;
                case 2:
                    msg += "商品价格发生变化";
                    break;
            }
            attributes.addFlashAttribute("msg", msg);
        } catch (NoStockException e) {
            String msg = e.getMessage();
            // String msg = "下单失败，商品无库存！";
            attributes.addFlashAttribute("msg", msg);
        }
        return "redirect:http://order.gulimall.com/toTrade";
    }
}
