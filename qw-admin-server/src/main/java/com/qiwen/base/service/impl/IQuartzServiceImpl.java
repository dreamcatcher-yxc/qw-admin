package com.qiwen.base.service.impl;

import com.qiwen.base.exception.SystemException;
import com.qiwen.base.service.IQuartzService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IQuartzServiceImpl implements IQuartzService {

    private final Scheduler scheduler;

    public IQuartzServiceImpl(@Qualifier("Scheduler") Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void addJob(String jobClassName, String jobGroupName, String cronExpression) {
        try {
            // 启动调度器
            scheduler.start();
            //构建job信息
            JobDetail jobDetail = JobBuilder
                    .newJob(getClass(jobClassName).getClass())
                    .withIdentity(jobClassName, jobGroupName)
                    .build();
            //表达式调度构建器(即任务执行的时间)
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            //按新的cronExpression表达式构建一个新的trigger
            CronTrigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(jobClassName, jobGroupName)
                    .withSchedule(scheduleBuilder)
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (ClassNotFoundException e) {
            throw new SystemException(String.format("[%s]类不存在, 请检测该类是否存在, 或者是前线程上下文类加载器加载.", jobClassName));
        } catch (IllegalAccessException e) {
            throw new SystemException(String.format("[%s]类无法被访问", jobClassName));
        } catch (InstantiationException e) {
            throw new SystemException(String.format("[%s]类无法被初始化", jobClassName));
        } catch (SchedulerException e) {
            throw new SystemException(String.format("创建任务失败, cause: %s, message: %s", e.getCause(), e.getMessage()));
        }
    }

    public void jobPause(String jobClassName, String jobGroupName) {
        try {
            scheduler.pauseJob(JobKey.jobKey(jobClassName, jobGroupName));
        } catch (SchedulerException e) {
            throw new SystemException("pause job failed...");
        }
    }

    public void jobResume(String jobClassName, String jobGroupName) {
        try {
            scheduler.resumeJob(JobKey.jobKey(jobClassName, jobGroupName));
        } catch (SchedulerException e) {
            throw new SystemException("resuse job failed...");
        }
    }

    public void jobReschedule(String jobClassName, String jobGroupName, String cronExpression) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobClassName, jobGroupName);
            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            throw new SystemException("job reschedule failed...");
        }
    }

    public void jobDelete(String jobClassName, String jobGroupName) {
        try {
            scheduler.pauseTrigger(TriggerKey.triggerKey(jobClassName, jobGroupName));
            scheduler.unscheduleJob(TriggerKey.triggerKey(jobClassName, jobGroupName));
            scheduler.deleteJob(JobKey.jobKey(jobClassName, jobGroupName));
        } catch (SchedulerException e) {
            throw new SystemException("job delete failed...");
        }
    }

    @Override
    public void jobTrigger(String jobClassName, String jobGroupName) {
        try {
            scheduler.triggerJob(JobKey.jobKey(jobClassName, jobGroupName));
        } catch (SchedulerException e) {
            throw new SystemException("job trigger failed...");
        }
    }

    private static Job getClass(String classname) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> clazz = Class.forName(classname);
        return (Job) clazz.newInstance();
    }
}
