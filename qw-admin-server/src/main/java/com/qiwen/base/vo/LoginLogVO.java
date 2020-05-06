package com.qiwen.base.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志
 */
@Data
public class LoginLogVO implements Serializable {

    private static final long serialVersionUID = 3652612847653618398L;

    private Long id;

    private String loginName;

    private String ip;

    private String loginRegion;

    private Date loginDate;

    private String loginType;

    private Date logoutDate;

    private String logoutType;

    private String osType;

    private String browserType;

    private String sessionId;

    private String description;

    private String createBy;

    private Date createDate;

    private String updateBy;

    private Date updateDate;
}
