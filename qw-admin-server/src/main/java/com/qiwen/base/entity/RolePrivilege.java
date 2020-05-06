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
@Table(name = QWConstant.DB_TABLE_PREFIX + "_ROLE_PRIVILEGE")
@EntityListeners(AuditingEntityListener.class)
public class RolePrivilege {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "ROLE_ID")
    private Long roleId;

    /**
     * privilege 会根据注解由系统自动生成, 不能保证 ID 一直，这里 [角色-权限] 关系使用 [角色 ID-权限 name]对应的方式确定,
     * 需要保证不同权限必定名称不同。
     * */
    @Column(nullable = false, name = "PRIVILEGE_NAME")
    private String privilegeName;

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
