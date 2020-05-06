package com.qiwen.base.config.interceptor;

import com.qiwen.base.vo.LoginUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.qiwen.base.util.SystemUtil.currentLoginUser;

/**
 * 权限拦截器.
 * -> 未登录用户只能访问特定的资源.
 * -> 已经登录的用户访问系统资源的时候需要根据用户的角色分配情况
 *
 * @author yangxiuchu
 */
@Slf4j
@Component
public class BasePermissionInterceptor extends QWAbstractAuthInterceptor {

    /**
     * PC 浏览器访问处理
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    private boolean pcBrowserProcess(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LoginUserVO user = currentLoginUser();
//        if(user != null && !hasRoles(user.getId(), "admin")) {
//            request.setAttribute(
//                    QWGlobalExceptionHandler.EXCEPTION_ATTACH_ATTRIBUTE,
//                    QWGlobalExceptionHandler.AttachData
//                            .builder()
//                            .attachData("@RequiresRoles")
//                            .logAttachData(generateUserAccessDesc(request, "need role type is admin"))
//                            .requiresPrintLog(false)
//                            .build()
//            );
//            throw new AuthenticationException("权限不足!");
//        }
        if(hasShiroAuthcAnnotation(handler)) {
            dealShiroAuthcAnnotaion(request, response, handler);
        } else if(hasQWAuthAnnotation(handler)) {
            dealQWDescAnnotaion(request, response, handler, log);
        }
        return true;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 访问管理接口
        return pcBrowserProcess(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public String[] getPathPatterns() {
        String[] patterns = { "/admin/**" };
        return patterns;
    }
}
