package com.qiwen.base.config.shiro;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义权限解析器
 * @author yangxiuchu
 */
public class QWPermissionResolver implements PermissionResolver {

    private final String qwPermissionRegex = "[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*";

    private final Pattern qwPermissionPattern = Pattern.compile(qwPermissionRegex);

    private boolean isQwExpression(String expression) {
        expression = expression.replaceAll("\\s*", "");
        if(expression.matches(qwPermissionRegex)) {
            return true;
        }
        Matcher matcher = qwPermissionPattern.matcher(expression);
        boolean canBeFind = matcher.find();
        if(canBeFind) {
            String group = matcher.group();
            char ch = expression.charAt(group.length());
            if(ch == '&' || ch == '|' || ch == '(' || ch == ')') {
                return true;
            }
        }
        return false;
    }

    @Override
    public Permission resolvePermission(String permissionString) {
        if(isQwExpression(permissionString)) {
            return new QWExpressionPermission(permissionString, false);
        }
        return new WildcardPermission(permissionString);
    }
}
