package cn.edu.zjut.cart.web;

import cn.edu.zjut.cart.service.CartService;
import cn.edu.zjut.cart.vo.CartItemVO;
import cn.edu.zjut.cart.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        CartVO vo = this.cartService.getCart();
        model.addAttribute("cart", vo);

        return "cartList";
    }

    /**
     * 添加商品到购物车
     * 为了保证接口幂等性，添加完商品后返回直接重定向到一个添加成功的展示页面，这样用户不断刷新也只会重复展示商品，不会添加
     *
     * @param redirectAttributes 可以通过 session 保存信息并在重定向的时候携带过去
     *                           .addFlashAttribute(): 将数据放在 session 中，可以在页面中取出，但是只能取一次
     *                           .addAttribute(): 将数据放在 url 后面
     */
    @GetMapping(value = "/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num,
                              RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        this.cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);

        return "redirect:http://cart.gulimall.com/addToCartSuccessPage.html";
    }

    /**
     * 跳转到添加成功的展示页面
     */
    @GetMapping("/addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        // 重定向到成功页面，再次查询购物车数据即可
        CartItemVO vo = this.cartService.getCartItem(skuId);
        model.addAttribute("cartItem", vo);

        return "success";
    }

    /**
     * 修改商品选中状态
     * TODO ajax方案
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam(value = "skuId") Long skuId, @RequestParam(value = "checked") Integer checked) {
        this.cartService.checkItem(skuId, checked);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 改变商品数量
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam(value = "skuId") Long skuId, @RequestParam(value = "num") Integer num) {
        this.cartService.changeItemCount(skuId, num);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 删除商品信息
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Integer skuId) {
        this.cartService.deleteIdCartInfo(skuId);

        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * 获取用户选中的购物项
     */
    @GetMapping("/getCheckedItems")
    @ResponseBody
    public List<CartItemVO> getCheckedItems() {
        return this.cartService.getUserCheckedItems();
    }

}
