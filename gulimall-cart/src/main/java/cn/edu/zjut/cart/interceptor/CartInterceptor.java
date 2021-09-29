package cn.edu.zjut.cart.interceptor;

import cn.edu.zjut.cart.dto.LoginInfoDTO;
import cn.edu.zjut.common.vo.MemberResponseVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

import static cn.edu.zjut.common.constant.AuthServerConstant.LOGIN_USER;
import static cn.edu.zjut.common.constant.CartConstant.VISITOR_COOKIE_KEY;
import static cn.edu.zjut.common.constant.CartConstant.VISITOR_COOKIE_TIMEOUT;

/**
 * 拦截器根据 session 和 cookie 中的信息封装登录信息（包含用户、游客）
 * 登录信息中一定包含游客信息，如果用户登录则会加入用户信息
 * 登录信息存入 threadLocal 中，方便全局获取
 * <p>
 * 方法调用顺序：preHandle -> Controller -> 业务 -> 返回 ModelAndView -> postHandle -> DispatcherServlet 视图渲染 -> afterCompletion
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    // 要使用 @Value 的前提是整个类交给容器管理 @Component
    @Value("${server.servlet.session.cookie.domain}")
    private String domain;

    public static ThreadLocal<LoginInfoDTO> threadLocal = new ThreadLocal<>();


    /**
     * 调用时间：Controller方法处理之前
     * 执行顺序：链式 Intercepter 情况下，Intercepter 按照声明的顺序一个接一个执行
     * 若返回 false，则中断执行，注意：不会进入 afterCompletion
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Cookie[] cookies = request.getCookies();

        LoginInfoDTO loginInfoDTO = new LoginInfoDTO();
        MemberResponseVO member;
        String visitorId = null;

        // 尝试从 cookie 中获取游客信息
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(VISITOR_COOKIE_KEY)) {
                    visitorId = cookie.getValue();
                    loginInfoDTO.setVisitorId(visitorId);
                    break;
                }
            }
        }

        // 如果未获取到，说明第一次访问购物车，分配游客信息
        if (visitorId == null) {
            visitorId = UUID.randomUUID().toString();
            loginInfoDTO.setVisitorId(visitorId);
            // 标识初次访问
            loginInfoDTO.setFirstVisit(true);
        }

        // 如果用户已登录，记录登录信息
        member = (MemberResponseVO) session.getAttribute(LOGIN_USER);
        if (member != null) {
            loginInfoDTO.setUserId(member.getId());
        }

        threadLocal.set(loginInfoDTO);

        // 这里拦截器更像是过滤器，最后全部通过
        return true;
    }


    /**
     * 调用前提：preHandle返回true
     * 调用时间：Controller方法处理完之后，DispatcherServlet 进行视图的渲染之前，也就是说在这个方法中你可以对 ModelAndView 进行操作
     * 执行顺序：链式 Intercepter 情况下，Intercepter 按照声明的顺序倒着执行。
     * 备注：postHandle 虽然 post 打头，但 post、get 方法都能处理
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LoginInfoDTO loginInfoDTO = threadLocal.get();

        // 如果初次访问，生成 cookie 将分配的游客信息传回浏览器
        if (loginInfoDTO.getFirstVisit()) {
            loginInfoDTO.setFirstVisit(false);

            Cookie cookie = new Cookie(VISITOR_COOKIE_KEY, loginInfoDTO.getVisitorId());
            cookie.setDomain(this.domain);
            cookie.setMaxAge(VISITOR_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }


    /**
     * 调用前提：preHandle返回true
     * 调用时间：DispatcherServlet进行视图的渲染之后
     * 多用于清理资源
     * 在该方法中 addCookie 无效
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocal.remove();
    }
}
                