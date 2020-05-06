package com.qiwen.base.entity;

import com.qiwen.base.config.QWConstant;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@ToString
@Entity
@Table(name = QWConstant.DB_TABLE_PREFIX + "_PRIVILEGE")
@EntityListeners(AuditingEntityListener.class)
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String urls;

    @Column(nullable = false)
    private String treeId;

    @Column(nullable = false)
    private String treeParentId;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column
    private String description;

    @Column(nullable = false)
    private Integer status = 0;

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
