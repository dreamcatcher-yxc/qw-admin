package com.qiwen.base.entity;

import com.qiwen.base.config.QWConstant;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志
 */
@Data
@Entity
@Table(name = QWConstant.DB_TABLE_PREFIX + "_LOGIN_LOG")
@EntityListeners(AuditingEntityListener.class)
public class LoginLog implements Serializable {

    private static final long serialVersionUID = 74999746190433246L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
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

    @CreatedBy
    private String createBy;

    @CreatedDate
    private Date createDate;

    @LastModifiedBy
    private String updateBy;

    @LastModifiedBy
    private Date updateDate;
}
