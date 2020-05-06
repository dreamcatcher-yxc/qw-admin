package com.qiwen.base.config.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public class DBBackJob implements Job {

    public static final String NAME = "数据库备份任务";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("数据库备份任务执行...");
    }
}
