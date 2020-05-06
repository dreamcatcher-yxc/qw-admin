package com.qiwen.base.entity;

import com.qiwen.base.config.QWConstant;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = QWConstant.DB_TABLE_PREFIX + "_OPERATE_LOG")
@EntityListeners(AuditingEntityListener.class)
public class OperateLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String olId;

    @Column(nullable = false, length = 500)
    private String operation;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false, length = 500)
    private String operatorName;

    @Column(nullable = false)
    private Date operateTime;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String operateMethod;

    @Column(nullable = false, length = 2000)
    private String operateParam;

    @Column(nullable = false, length = 1)
    private String result;

    @Column
    private String message;
}