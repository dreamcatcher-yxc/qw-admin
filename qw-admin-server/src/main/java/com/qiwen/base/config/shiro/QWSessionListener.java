package com.qiwen.base.config.shiro;

import com.qiwen.base.service.ILoginLogService;
import com.qiwen.base.util.SpringHelper;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class QWSessionListener extends SessionListenerAdapter {

    private ILoginLogService loginLogService;

    public QWSessionListener(ILoginLogService loginLogService) {
        this.loginLogService = SpringHelper.getLazyBean(ILoginLogService.class);
    }

    @Override
    public void onExpiration(Session session) {
        loginLogService.updateLogoutInfo((String) session.getId(), new Date(), "session-expiration");
    }

    @Override
    public void onStop(Session session) {
        loginLogService.updateLogoutInfo((String) session.getId(), new Date(), "session-stop");
    }
}
