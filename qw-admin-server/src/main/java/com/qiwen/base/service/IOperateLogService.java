package com.qiwen.base.service;

import com.qiwen.base.entity.OperateLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface IOperateLogService {

    void save(OperateLog operatorLog);

    void delete(Long... ids);

    OperateLog findById(Long id);

    Page<OperateLog> findAll(OperateLog operateLog, Date start, Date end, Pageable pageable);
}
