package com.qiwen.base.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class OnlineUserVO implements Serializable {

    private static final long serialVersionUID = -3684420974251754789L;

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

    /**
     * 是否是当前登录用户
     */
    private boolean isCurrentUser;
}
