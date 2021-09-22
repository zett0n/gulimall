package cn.edu.zjut.cart.web;

import cn.edu.zjut.cart.dto.LoginInfoDTO;
import cn.edu.zjut.cart.interceptor.CartInterceptor;
import cn.edu.zjut.cart.service.CartService;
import cn.edu.zjut.cart.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart.html")
    public String cartListPage() {

        LoginInfoDTO userInfoDTO = CartInterceptor.threadLocal.get();
        return "cartList2";
    }

    /**
     * 跳转到添加购物车成功页面
     */
    @GetMapping("/addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,
                                       Model model) {
        //重定向到成功页面。再次查询购物车数据即可
        CartItemVO vo = this.cartService.getCartItem(skuId);
        model.addAttribute("cartItem", vo);
        return "success";
    }

}
