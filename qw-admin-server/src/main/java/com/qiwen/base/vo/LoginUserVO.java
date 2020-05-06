package com.qiwen.base.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础授权信息
 * @author yangxiuchu
 */
@Data
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = -6024698499707649610L;

    private Long id;

    private String username;

    private String nickname;

    private String password;

    private String header;

    private String gender;

    private String email;

    private String phone;

    private boolean isAutoLogin = false;

    private String ext;

    /**
     * 登录时间
     */
    private Date loginDate;

    /**
     * 登录类型
     */
    private String loginType;

    /**
     * 最近访问时间
     */
    private Date lastAccessDate;

    /**
     * IP 地址
     */
    private String ip;

    /**
     * 登录地点
     */
    private String loginRegion;

    /**
     * 对应的 sessionId
     */
    private String sessionId;

    @Override
    public String toString() {
        return this.nickname + "(" + this.username + ")";
    }
}
