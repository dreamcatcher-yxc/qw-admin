package com.qiwen.base.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class OperateLogVO {

    private Long id;

    private String olId;

    private String operation;

    private Long operatorId;

    private String operatorName;

    private Date operateTime;

    private Integer duration;

    private String ip;

    private String operateMethod;

    private Map<String, Object> operateParam;

    private String result;

    private String message;
}