package com.qiwen.base.config.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public class FileMapBackJob implements Job {

    public static final String NAME = "文件备份任务";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("文件备份任务执行...");
    }
}
