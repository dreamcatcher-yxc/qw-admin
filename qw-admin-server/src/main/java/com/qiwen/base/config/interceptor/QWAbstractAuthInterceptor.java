package com.qiwen.base.config.interceptor;

import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.Role;
import com.qiwen.base.service.IRoleService;
import com.qiwen.base.util.SpringHelper;
import com.qiwen.base.util.SystemUtil;
import com.qiwen.base.vo.LoginUserVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.annotation.*;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 抽象权限拦截公共逻辑
 */
public abstract class QWAbstractAuthInterceptor extends QWAbstractInterceptor {

    private final IRoleService roleService;

    public QWAbstractAuthInterceptor() {
        this.roleService = SpringHelper.getLazyBean(IRoleService.class);
    }

    protected boolean hasShiroAuthcAnnotation (Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod mh = (HandlerMethod) handler;
        HandlerMethod rhm = mh.getResolvedFromHandlerMethod();

        return rhm.getMethodAnnotation(RequiresPermissions.class) != null
                || rhm.getMethodAnnotation(RequiresRoles.class) != null
                || rhm.getMethodAnnotation(RequiresAuthentication.class) != null
                || rhm.getMethodAnnotation(RequiresUser.class) != null
                || rhm.getMethodAnnotation(RequiresGuest.class) != null;
    }

    protected void dealShiroAuthcAnnotaion (HttpServletRequest request, HttpServletResponse response, Object handler) {
        HandlerMethod mh = (HandlerMethod) handler;
        HandlerMethod rhm = mh.getResolvedFromHandlerMethod();
        Subject subject = SecurityUtils.getSubject();

        RequiresAuthentication requiresAuthentication = rhm.getMethodAnnotation(RequiresAuthentication.class);
        if(requiresAuthentication != null) {
            if(!SystemUtil.isLogin(false)) {
                request.setAttribute(
                        QWGlobalExceptionHandler.EXCEPTION_ATTACH_ATTRIBUTE,
                        QWGlobalExceptionHandler.AttachData
                                .builder()
                                .attachData("@RequiresAuthentication")
                                .logAttachData(generateUserAccessDesc(request, "needAuthentication"))
                                .requiresPrintLog(false)
                                .build()
                );
                throw new AuthenticationException("未使用用户名/密码登录");
            }
        }

        RequiresUser requiresUser = rhm.getMethodAnnotation(RequiresUser.class);
        if(requiresUser != null) {
            if(!SystemUtil.isLogin()) {
                request.setAttribute(
                        QWGlobalExceptionHandler.EXCEPTION_ATTACH_ATTRIBUTE,
                        QWGlobalExceptionHandler.AttachData
                                .builder()
                                .attachData("@RequiresUser")
                                .logAttachData(generateUserAccessDesc(request, "needUser"))
                                .requiresPrintLog(false)
                                .build()
                );
                throw new AuthenticationException("未登录");
            }
        }

        RequiresPermissions requiresPermissions = rhm.getMethodAnnotation(RequiresPermissions.class);
        if(requiresPermissions != null) {
            boolean isOk = subject.isPermitted(requiresPermissions.value()[0]);
            if(!isOk) {
                request.setAttribute(
                        QWGlobalExceptionHandler.EXCEPTION_ATTACH_ATTRIBUTE,
                        QWGlobalExceptionHandler.AttachData
                                .builder()
                                .attachData("@RequiresPermissions")
                                .logAttachData(generateUserAccessDesc(
                                        request,
                                        "permissions[" +
                                        StringUtils.join(
                                                requiresPermissions.value(),
                                                requiresPermissions.logical().name())
                                        + "]"
                                ))
                                .requiresPrintLog(false)
                                .build()
                );
                throw new AuthenticationException("您当前无访问该资源权限");
            }
        }

        RequiresRoles requiresRoles = rhm.getMethodAnnotation(RequiresRoles.class);
        if(requiresRoles != null) {
            String[] roles = requiresRoles.value();
            boolean isOk = false;

            if (roles.length == 1) {
                isOk = subject.hasRole(roles[0]);
            } else if (Logical.AND.equals(requiresRoles.logical())) {
                isOk = subject.hasAllRoles(Arrays.asList(roles));
            } else if (Logical.OR.equals(requiresRoles.logical())) {
                // Avoid processing exceptions unnecessarily - "delay" throwing the exception by calling hasRole first
                boolean[] booleans = subject.hasRoles(Arrays.asList(roles));
                for (boolean tb : booleans) {
                    if(tb) {
                        isOk = true;
                    }
                }
            }
            if(!isOk) {
                request.setAttribute(
                        QWGlobalExceptionHandler.EXCEPTION_ATTACH_ATTRIBUTE,
                        QWGlobalExceptionHandler.AttachData
                                .builder()
                                .attachData("@RequiresRoles")
                                .logAttachData(generateUserAccessDesc(
                                        request,
                                        "roles[" +
                                                StringUtils.join(
                                                requiresRoles.value(),
                                                requiresRoles.logical().name())
                                         + "]"
                                ))
                                .requiresPrintLog(false)
                                .build()
                );
                throw new AuthenticationException("您当前无访问该资源的角色");
            }
        }

        RequiresGuest requiresGuest = rhm.getMethodAnnotation(RequiresGuest.class);
        if(requiresGuest != null) {
            boolean isOk = subject.getPrincipals() == null;
            if(!isOk) {
                request.setAttribute(
                        QWGlobalExceptionHandler.EXCEPTION_ATTACH_ATTRIBUTE,
                        QWGlobalExceptionHandler.AttachData
                                .builder()
                                .attachData("@RequiresGuest")
                                .logAttachData(generateUserAccessDesc(request, "need guest"))
                                .requiresPrintLog(false)
                                .build()
                );
                throw new AuthenticationException("您当前无访问该资源权限");
            }
        }
    }

    /**
     * 生成用户访问描述信息
     * @param request
     * @return
     */
    protected String generateUserAccessDesc(HttpServletRequest request, String attach) {
        StringBuilder accessDesc = new StringBuilder();
        LoginUserVO loginUserVO = SystemUtil.currentLoginUser();
        if(loginUserVO != null) {
            accessDesc.append(loginUserVO.getUsername());
        } else {
            accessDesc.append("guest");
        }
        accessDesc.append(" => " + request.getRequestURI())
                .append(" => ")
                .append(attach);
        return accessDesc.toString();
    }

    protected void dealQWDescAnnotaion(HttpServletRequest request, HttpServletResponse response, Object handler, Logger logger) {
        String permissionExpression = getPermissionKey(handler);
        if(Objects.isNull(permissionExpression)) {
            return;
        }
        if(SystemUtil.currentLoginUser() == null) {
            request.setAttribute(
                    QWGlobalExceptionHandler.EXCEPTION_ATTACH_ATTRIBUTE,
                    QWGlobalExceptionHandler.AttachData
                            .builder()
                            .attachData("@RequiresUser")
                            .logAttachData(generateUserAccessDesc(request, "needUser"))
                            .requiresPrintLog(false)
                            .build()
            );
            throw new AuthenticationException("未登录");
        }
        if (!SecurityUtils.getSubject().isPermitted(permissionExpression)) {
            if(logger != null) {
                logger.warn("您当前无访问该资源权限");
            }
            request.setAttribute(
                    QWGlobalExceptionHandler.EXCEPTION_ATTACH_ATTRIBUTE,
                    QWGlobalExceptionHandler.AttachData
                            .builder()
                            .attachData("@RequiresPermissions")
                            .logAttachData(generateUserAccessDesc(request, "desc[" + permissionExpression + "]"))
                            .requiresPrintLog(false)
                            .build()
            );
            throw new AuthenticationException("权限不足, 请联系管理员");
        }
    }

    /**
     * 判断被访问的方法是否需要被拦截.
     * @param handler
     * @return
     */
    protected boolean hasQWAuthAnnotation(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod mh = (HandlerMethod) handler;
        HandlerMethod rhm = mh.getResolvedFromHandlerMethod();

        // 类上没有 Desc 注解或者 needIntercept = false 则说明该类下的所有被 url 映射的方法访问都不需要被拦截.
        Desc classDesc = rhm.getBeanType().getAnnotation(Desc.class);
        if (classDesc == null || !classDesc.requiredPermission()) {
            return false;
        }

        // 方法没有 Desc 注解或者 needIntercept = false 不需要被拦截.
        Desc methodDesc = rhm.getMethodAnnotation(Desc.class);
        if (methodDesc == null || StringUtils.isEmpty(methodDesc.name()) || !methodDesc.requiredPermission()) {
            return false;
        }
        return true;
    }

    protected String getPermissionKey(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return null;
        }
        HandlerMethod mh = (HandlerMethod) handler;
        Desc methodDesc = mh.getResolvedFromHandlerMethod().getMethodAnnotation(Desc.class);
        return methodDesc.name();
    }

    /**
     * 判断指定用户是否拥有指定类型的角色
     * @param userId 用户ID
     * @param roleTypes 角色类型集合
     * @return 有: true, 无: false
     */
    protected boolean hasRoles(Long userId, String... roleTypes) {
        List<Role> roles = roleService.findByUserId(userId);

        if(CollectionUtils.isEmpty(roles)) {
            return false;
        }

        Stream<String> roleTypesStream = Arrays.stream(roleTypes);
        return roles.stream()
                .anyMatch(role -> roleTypesStream.anyMatch(roleType ->
                                roleType.equals(role.getType())
                        )
                );
    }

    @Override
    public int getOrder() {
        return super.getOrder();
    }
}
