package com.qiwen.base.service.impl;

import com.qiwen.base.vo.JobAndTriggerVO;
import com.qiwen.base.service.IJobAndTriggerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobAndTriggerServiceImpl implements IJobAndTriggerService {

    private final JdbcTemplate jdbcTemplate;

    public JobAndTriggerServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<JobAndTriggerVO> findAll(Pageable pageable) {
        List<JobAndTriggerVO> jats = jdbcTemplate.query(MYSQL_FIND_ALL_SQL,
                (rs, rowNum) -> {
                    /**
                     qrtz_job_details.JOB_NAME,
                     qrtz_job_details.JOB_GROUP,
                     qrtz_job_details.JOB_CLASS_NAME,
                     qrtz_triggers.TRIGGER_NAME,
                     qrtz_triggers.TRIGGER_GROUP,
                     qrtz_cron_triggers.CRON_EXPRESSION,
                     qrtz_cron_triggers.TIME_ZONE_ID,
                     qrtz_triggers.TRIGGER_STATE
                     */
                    String jobName = rs.getString("JOB_NAME");
                    String jobGroup = rs.getString("JOB_GROUP");
                    String jobClassName = rs.getString("JOB_CLASS_NAME");
                    String triggerName = rs.getString("TRIGGER_NAME");
                    String triggerGroup = rs.getString("TRIGGER_GROUP");
                    String cronExpression = rs.getString("CRON_EXPRESSION");
                    String timeZoneId = rs.getString("TIME_ZONE_ID");
                    String triggerState = rs.getString("TRIGGER_STATE");

                    JobAndTriggerVO jobAndTriggerVO = new JobAndTriggerVO();
                    jobAndTriggerVO.setJOB_NAME(jobName);
                    jobAndTriggerVO.setJOB_GROUP(jobGroup);
                    jobAndTriggerVO.setJOB_CLASS_NAME(jobClassName);
                    jobAndTriggerVO.setTRIGGER_NAME(triggerName);
                    jobAndTriggerVO.setTRIGGER_GROUP(triggerGroup);
                    jobAndTriggerVO.setCRON_EXPRESSION(cronExpression);
                    jobAndTriggerVO.setTIME_ZONE_ID(timeZoneId);
                    jobAndTriggerVO.setTRIGGER_STATE(triggerState);

                    return jobAndTriggerVO;
                }, pageable.getOffset(), pageable.getPageSize());

        Long count = jdbcTemplate.queryForObject(MYSQL_FIND_ALL_COUNT_SQL, Long.class);
        return new PageImpl(jats, pageable, count);
    }
}
