package com.qiwen.base.config.shiro;

import org.apache.shiro.authc.RememberMeAuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 自定义 token, 增加登录用户需要拥有的角色功能校验
 * @author yangxiuchu
 */
public class QWAuthToken extends UsernamePasswordToken implements RememberMeAuthenticationToken {

    /**
     * 指定登录用户必须拥有的角色
     */
    private String[] needRoles;

    /**
     * 记住我功能的 CookieID, 不用设置采用 LoginUtil 的默认配置
     */
    private String rememberMeCookieName;

    /**
     * 记住我 cookie 主机路径
     */
    private String rememberMePath;

    public QWAuthToken(String username, String password, boolean rememberMe, String rememberMePath, String[] needRoles) {
        super(username, password, rememberMe);
        this.needRoles = needRoles;
        this.rememberMePath = rememberMePath;
    }

    public String getRememberMeCookieName() {
        return rememberMeCookieName;
    }

    public void setRememberMeCookieName(String rememberMeCookieName) {
        this.rememberMeCookieName = rememberMeCookieName;
    }

    public String[] getNeedRoles() {
        return needRoles;
    }

    public String getRememberMePath() {
        return rememberMePath;
    }

    public void setNeedRoles(String[] needRoles) {
        this.needRoles = needRoles;
    }

    public void setRememberMePath(String rememberMePath) {
        this.rememberMePath = rememberMePath;
    }
}
