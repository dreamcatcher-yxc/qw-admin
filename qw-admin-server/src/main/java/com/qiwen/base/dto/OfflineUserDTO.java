package com.qiwen.base.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OfflineUserDTO implements Serializable {

    private static final long serialVersionUID = 2862435828494538995L;

    private String username;

    /**
     * 标识强制下线类型<br>
     * 1：下线之后的下一次登录必须使用用户名/密码登录 <br>
     * x: 暂无更多强制下线需求, 不做定义
     */
    private int offlineType =  1;

    private int count = 1;
}
