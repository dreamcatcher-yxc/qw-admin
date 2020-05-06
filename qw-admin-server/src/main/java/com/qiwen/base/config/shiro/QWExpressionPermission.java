package com.qiwen.base.config.shiro;

import lombok.Data;
import org.apache.shiro.authz.Permission;

/**
 * 此 permission 用于
 */
@Data
public class QWExpressionPermission implements Permission {

    private String ticket;

    private boolean needCovertExpression;

    public QWExpressionPermission(String ticket, boolean needCovertExpression) {
        this.ticket = ticket;
        this.needCovertExpression = needCovertExpression;
    }

    @Override
    public boolean implies(Permission p) {
        return false;
    }
}
