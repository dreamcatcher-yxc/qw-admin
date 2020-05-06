package com.qiwen.base.entity;

import com.qiwen.base.config.QWConstant;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@Entity
@Table(name = QWConstant.DB_TABLE_PREFIX + "_USER")
@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable {

    private static final long serialVersionUID = -851521874994199448L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private String nickname;

    @Column(nullable = false)
    private String password = "123456";

    private String header;

    @Column(nullable = false)
    private String gender = "m";

    private String email;

    private String phone;

    // 状态: 0-禁用, 1-有效
    private Integer status;

    // 扩展字段, 存储内容应该符合 JSON 格式要求
    @Column(columnDefinition = "TEXT(10000)")
    private String ext;

    // 创建者
    @CreatedBy
    private String createBy;

    // 创建时间
    @CreatedDate
    private Date createDate;

    @LastModifiedBy
    private String updateBy;

    @LastModifiedBy
    private Date updateDate;
}
