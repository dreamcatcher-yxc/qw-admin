package com.qiwen.base.service;

import com.qiwen.base.entity.WorkLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface IWorkLogService {

    void save(WorkLog workLog);

    void deleteByWorkLogIds(String... ids);

    Page<WorkLog> findAll(Date startDate, Date endDate, String jobClass, Pageable pageable);

    WorkLog findById(String id);
}
