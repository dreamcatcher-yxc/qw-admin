package com.qiwen.base.service;

import com.qiwen.base.vo.JobAndTriggerVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IJobAndTriggerService {

    String MYSQL_FIND_ALL_SQL = "SELECT" +
            " QRTZ_JOB_DETAILS.JOB_NAME," +
            " QRTZ_JOB_DETAILS.JOB_GROUP," +
            " QRTZ_JOB_DETAILS.JOB_CLASS_NAME," +
            " QRTZ_TRIGGERS.TRIGGER_NAME," +
            " QRTZ_TRIGGERS.TRIGGER_GROUP," +
            " QRTZ_CRON_TRIGGERS.CRON_EXPRESSION," +
            " QRTZ_CRON_TRIGGERS.TIME_ZONE_ID," +
            " QRTZ_TRIGGERS.TRIGGER_STATE" +
            " FROM" +
            " QRTZ_JOB_DETAILS" +
            " JOIN QRTZ_TRIGGERS" +
            " JOIN QRTZ_CRON_TRIGGERS ON QRTZ_JOB_DETAILS.JOB_NAME = QRTZ_TRIGGERS.JOB_NAME" +
            " AND QRTZ_TRIGGERS.TRIGGER_NAME = QRTZ_CRON_TRIGGERS.TRIGGER_NAME" +
            " AND QRTZ_TRIGGERS.TRIGGER_GROUP = QRTZ_CRON_TRIGGERS.TRIGGER_GROUP" +
            " LIMIT ?,?";

    String MYSQL_FIND_ALL_COUNT_SQL = "SELECT COUNT(*) AS COUNT" +
            " FROM" +
            " QRTZ_JOB_DETAILS" +
            " JOIN QRTZ_TRIGGERS" +
            " JOIN QRTZ_CRON_TRIGGERS ON QRTZ_JOB_DETAILS.JOB_NAME = QRTZ_TRIGGERS.JOB_NAME" +
            " AND QRTZ_TRIGGERS.TRIGGER_NAME = QRTZ_CRON_TRIGGERS.TRIGGER_NAME" +
            " AND QRTZ_TRIGGERS.TRIGGER_GROUP = QRTZ_CRON_TRIGGERS.TRIGGER_GROUP";

    Page<JobAndTriggerVO> findAll(Pageable pageable);
}
