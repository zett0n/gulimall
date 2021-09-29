package cn.edu.zjut.order.interceptor;

import cn.edu.zjut.common.vo.MemberResponseVO;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static cn.edu.zjut.common.constant.AuthServerConstant.LOGIN_USER;

@Component
public class OrderInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVO> loginUser = new ThreadLocal<>();

    /**
     * 拦截未登录的请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 库存服务的 listener 收到来自 rabbitmq broker 的请求后通过 feign 调用本微服务接口，不会含有登录信息，因此需要放行
        String requestURI = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean match1 = matcher.match("/order/order/infoByOrderSn/**", requestURI);
        boolean match2 = matcher.match("/payed/**", requestURI);
        if (match1 || match2) {
            return true;
        }
        
        HttpSession session = request.getSession();
        MemberResponseVO memberResponseVO = (MemberResponseVO) session.getAttribute(LOGIN_USER);
        if (memberResponseVO != null) {
            loginUser.set(memberResponseVO);
            return true;
        } else {
            session.setAttribute("msg", "请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        loginUser.remove();
    }
}
