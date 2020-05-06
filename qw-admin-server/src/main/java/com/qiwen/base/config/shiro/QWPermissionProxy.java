package com.qiwen.base.config.shiro;

import com.qiwen.base.entity.Privilege;
import com.qiwen.base.service.IAuthService;
import com.qiwen.base.util.SpringHelper;
import org.apache.shiro.authz.Permission;

import java.util.List;

public class QWPermissionProxy implements Permission {

    private final IAuthService authService;

    private final List<Privilege> privileges;

    public QWPermissionProxy(IAuthService authService, List<Privilege> privileges) {
        this.authService = authService;
        this.privileges = privileges;
    }

    @Override
    public boolean implies(Permission p) {
        if(!(p instanceof QWExpressionPermission)) {
            return false;
        }
        QWExpressionPermission expressionPermission = (QWExpressionPermission) p;
        return this.authService.calculatePermissionExpression(
                    expressionPermission.getTicket(),
                    expressionPermission.isNeedCovertExpression(),
                    this.privileges
                );
    }
}
