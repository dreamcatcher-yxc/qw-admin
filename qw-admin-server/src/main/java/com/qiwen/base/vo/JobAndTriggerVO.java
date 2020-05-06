package com.qiwen.base.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigInteger;

@Data
@ToString
public class JobAndTriggerVO {

    private String JOB_NAME;

    private String JOB_GROUP;

    private String JOB_CLASS_NAME;

    private String TRIGGER_NAME;

    private String TRIGGER_GROUP;

    private BigInteger REPEAT_INTERVAL;

    private BigInteger TIMES_TRIGGERED;

    private String CRON_EXPRESSION;

    private String TIME_ZONE_ID;

    private String TRIGGER_STATE;
}
