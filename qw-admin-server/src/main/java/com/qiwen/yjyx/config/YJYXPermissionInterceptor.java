package com.qiwen.yjyx.config;

import com.qiwen.base.config.interceptor.QWAbstractAuthInterceptor;
import com.qiwen.base.vo.LoginUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.qiwen.base.util.SystemUtil.currentLoginUser;

/**
 * 新增模块权限拦截器
 */
@Slf4j
@Component
public class YJYXPermissionInterceptor extends QWAbstractAuthInterceptor {

    /**
     * 移动端浏览器访问处理
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    private boolean phoneBrowserProcess(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(hasShiroAuthcAnnotation(handler)) {
            dealShiroAuthcAnnotaion(request, response, handler);
        } else if(hasQWAuthAnnotation(handler)) {
            dealQWDescAnnotaion(request, response, handler, log);
        }
        return true;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return phoneBrowserProcess(request, response, handler);
    }

    @Override
    public String[] getPathPatterns() {
        String[] patterns = { "/yjyx/admin/v1/**" };
        return patterns;
    }
}
