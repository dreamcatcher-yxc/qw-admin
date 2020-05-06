package com.qiwen.base.config.shiro;

import com.qiwen.base.util.LoginUtil;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 自定义 token, 标识时自动登录使用的 Token
 * @author yangxiuchu
 */
public class QWAutoLoginToken extends UsernamePasswordToken {

    /**
     * 指定登录用户必须拥有的角色
     */
    private String[] needRoles;

    /**
     * 客户端传递的 Cookie 信息
     */
    private LoginUtil.RememberInfo rememberInfo;

    public QWAutoLoginToken(LoginUtil.RememberInfo rememberInfo, String[] needRoles) {
        this.rememberInfo = rememberInfo;
        this.needRoles = needRoles;
    }

    public String[] getNeedRoles() {
        return needRoles;
    }

    public void setNeedRoles(String[] needRoles) {
        this.needRoles = needRoles;
    }

    public LoginUtil.RememberInfo getRememberInfo() {
        return rememberInfo;
    }

    public void setRememberInfo(LoginUtil.RememberInfo rememberInfo) {
        this.rememberInfo = rememberInfo;
    }
}
