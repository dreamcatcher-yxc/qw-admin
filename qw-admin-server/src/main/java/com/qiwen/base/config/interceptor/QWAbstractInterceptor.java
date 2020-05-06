package com.qiwen.base.config.interceptor;

import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 自定义 Interceptor 接口类, 所有该接口的子类 Bean 都会被注册为 SpringMVC 拦截器
 */
public abstract class QWAbstractInterceptor extends HandlerInterceptorAdapter {

    /**
     * 返回该拦截器匹配的 url patterns
     * @return
     */
    public abstract String[] getPathPatterns();

    /**
     * 返回该拦截器匹配排除的 url patterns
     * @return
     */
    public String[] getExcludePathPatterns() {
        return new String[]{};
    }

    /**
     * 拦截器顺序, 默认为 0
     * @return
     */
    public int getOrder() {
        return 0;
    }

    public PathMatcher getPathMatcher() {
        return null;
    }

    /**
     * 去除 GET 请求 URL 的查询参数部分
     * @return URL 部分
     */
    protected String removeQueryParams(String url) {
        // 去除查询参数
        int index = url.indexOf("?");

        if(index > 0) {
            url = url.substring(0, index);
        }

        return url;
    }
}
