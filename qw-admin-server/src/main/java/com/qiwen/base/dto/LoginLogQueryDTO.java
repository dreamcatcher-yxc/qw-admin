package com.qiwen.base.dto;

import lombok.Data;

import java.util.Date;

@Data
public class LoginLogQueryDTO {

    private String loginName;

    private Date startDate;

    private Date endDate;

}
