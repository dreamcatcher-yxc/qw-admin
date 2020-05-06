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
@Table(name = QWConstant.DB_TABLE_PREFIX + "_FILE_MAP")
@EntityListeners(AuditingEntityListener.class)
public class FileMap {
    // 自增主键
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long Id;

    // 业务主键
    @Column(unique = true, nullable = false)
    private String fileId;

    // 文件名称, 不一定和实际保存的文件的名称一致
    @Column(nullable = false)
    private String name;

    // 文件真实名称
    @Column(nullable = false)
    private String realName;

    // 文件保存的实际路径
    @Column(nullable = false)
    private String path;

    // 文件类型
    @Column(nullable = false)
    private String type;

    // 该文件的 md5 码
    @Column(nullable = false)
    private String md5;

    // 如果上传的文件以前已经上传过, 则 isLink 的 1, 此时 path 表示真实文件所存放的 fileId,
    // 此时根据 FileId 查询到相应列, 再查询出 path 即可找到真实存在的文件.
    @Column(nullable = false)
    private int isLink = 0;

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
