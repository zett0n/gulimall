// package cn.edu.zjut.order.web;
//
// import cn.edu.zjut.order.service.OrderService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseBody;
//
// @Controller
// public class PayController {
//     @Autowired
//     private OrderService orderService;
//
//     @ResponseBody
//     @GetMapping(value = "/aliPayOrder", produces = "text/html")
//     public String aliPayOrder(@RequestParam("orderSn") String orderSn) {
//         // System.out.println("接收到订单信息orderSn：" + orderSn);
//         // PayVo payVo = this.orderService.getOrderPay(orderSn);
//         // String pay = alipayTemplate.pay(payVo);
//         // return pay;
//         return "pay.";
//     }
//
// }
