package com.qiwen.base.config.shiro;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础授权信息
 * @author yangxiuchu
 */
@Data
public class QWBaseAuthPrincipal implements Serializable {

    private static final long serialVersionUID = -4211932879263830182L;

    private Long id;

    private String username;

}
