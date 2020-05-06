package com.qiwen.base.service;

public interface IQuartzService {

    void addJob(String jobClassName, String jobGroupName, String cronExpression);

    void jobPause(String jobClassName, String jobGroupName);

    void jobResume(String jobClassName, String jobGroupName);

    void jobReschedule(String jobClassName, String jobGroupName, String cronExpression);

    void jobDelete(String jobClassName, String jobGroupName);

    void jobTrigger(String jobClassName, String jobGroupName);
}
