package com.qiwen.base.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * 记录使用 Quartz 调度的任务的工作日志.
 */
@Data
@ToString
@Entity
@Table(name = "QRTZ_WORK_LOG")
@EntityListeners(AuditingEntityListener.class)
public class WorkLog {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String workLogId;

    // 开始执行时间
    private Date startDate;

    // 结束执行时间
    private Date endDate;

    // 该 Job 名称
    private String name;

    // job 所在组
    private String groupName;

    // 哪个 Job 执行的, 该 Job 的全类名
    private String jobClass;

    @Column(length = 3000)
    private String description;

    // 执行结果,  0: 失败, 1: 成功
    private int status = 1;

    // 创建时间
    @CreatedDate
    private Date createDate;

    // 更新时间
    @LastModifiedDate
    private Date updateDate;
}
