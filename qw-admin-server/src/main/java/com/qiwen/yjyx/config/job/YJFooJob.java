package com.qiwen.yjyx.config.job;

import com.qiwen.base.entity.WorkLog;
import com.qiwen.base.service.IWorkLogService;
import com.qiwen.base.util.SpringHelper;
import com.qiwen.yjyx.config.YJYXAppConfig;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

@Slf4j
public class YJFooJob implements Job {

    public static final String NAME = "测试任务";

    private final YJYXAppConfig appConfig;

    private final IWorkLogService workLogService;

    public YJFooJob() {
        // 注入 bean
        this.appConfig = SpringHelper.getRealBean(YJYXAppConfig.class);
        this.workLogService = SpringHelper.getRealBean(IWorkLogService.class);

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Date startDate = new Date();
        // do something()
        Date endDate = new Date();

        WorkLog workLog = new WorkLog();
        workLog.setStartDate(startDate);
        workLog.setEndDate(endDate);
        workLog.setDescription("这是一段描述信息");
        JobDetail jobDetail = context.getJobDetail();
        String jobName = jobDetail.getKey().getName();
        String jobGroupName = jobDetail.getKey().getGroup();
        workLog.setGroupName(jobGroupName);
        workLog.setName(jobName);
        workLog.setJobClass(jobDetail.getJobClass().getName());
        workLog.setStatus(1);
        workLogService.save(workLog);
    }
}
