package com.qiwen.base.config.log;

import com.qiwen.base.entity.OperateLog;
import com.qiwen.base.service.IOperateLogService;
import com.qiwen.base.util.SpringHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

@Slf4j
public class LogTaskFactory {

    private static IOperateLogService operateLogService = SpringHelper.getRealBean(IOperateLogService.class);

    public static TimerTask bussinessLog(final OperateLog logInfo) {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    operateLogService.save(logInfo);
                } catch (Exception e) {
                    log.error("创建业务日志异常!", e);
                }
            }
        };
    }

}
