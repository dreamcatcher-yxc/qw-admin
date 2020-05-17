package com.qiwen.base.config.shiro;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.entity.User;
import com.qiwen.base.service.*;
import com.qiwen.base.util.*;
import com.qiwen.base.util.http.HttpUtil;
import com.qiwen.base.vo.LoginUserVO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class QWAutoLoginRealm extends QWAbstractAuthorizingRealm {

    public static final String REALM_NAME = "QW-REMEMBER-ME-AUTHORIZATION-REALM";

    /**
     * 登录结果标识, 存储在 request 中
     */
    public static final String LOGIN_RESULT_KEY = "QW_AUTO_AUTH_REALM_LOGIN_RESULT";

    private final IForceOfflineUserService forceOfflineUserService;

    public QWAutoLoginRealm(IUserService userService,
                            IRoleService roleService,
                            IPrivilegeService privilegeService,
                            IAuthService authService,
                            ILoginLogService loginLogService,
                            IForceOfflineUserService forceOfflineUserService,
                            QWAppConfig appConfig) {

        super(userService,
                roleService,
                privilegeService,
                authService,
                loginLogService,
                SpringHelper.getLazyBean(ISessionManagerService.class),
                appConfig);
        this.forceOfflineUserService = forceOfflineUserService;
    }

    @Override
    public String getName() {
        return REALM_NAME;
    }

    /**
     * token 必须是 QWAuthToken 类型
     * @param token
     * @return
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof QWAutoLoginToken;
    }

    protected boolean isCurrentRealmAuth(PrincipalCollection principals) {
        Set<String> realmNames = principals.getRealmNames();
        return !CollectionUtils.isEmpty(realmNames) && realmNames.contains(REALM_NAME);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String message;
        int flag;
        Result result = Result.ok();
        // 设置提示信息
        Subject subject = SecurityUtils.getSubject();
        HttpServletRequest request = WebUtils.getHttpRequest(subject);

        if(SystemUtil.isLogin()) {
            message = "已登录";
            flag = 4;
            request.setAttribute(LOGIN_RESULT_KEY, Result.ok(message).put("flag", flag));
            throw new ConcurrentAccessException(message);
        }

        QWAutoLoginToken customToken = (QWAutoLoginToken) token;
        LoginUtil.RememberInfo rememberInfo = ((QWAutoLoginToken) token).getRememberInfo();
        LoginUserVO loginUserVO;
        QWBaseAuthPrincipal principal;

        if (rememberInfo != null && !rememberInfo.isExpired()) {
            String username = rememberInfo.getUsername();
            String encryptionPassword = rememberInfo.getPassword();
            User userPO = userService.findByUsernameAndPassword(username, encryptionPassword);
            loginUserVO = ReflectUtil.O2ObySameField(userPO, LoginUserVO.class);

            if(loginUserVO == null) {
                message = "登录信息错误";
                flag = 3;
                request.setAttribute(LOGIN_RESULT_KEY, Result.ok(message).put("flag", flag));
                throw new UnknownAccountException(message);
            }

            // 用户已禁用
            if(userPO.getStatus() == 0) {
                message = "该账户已被禁用";
                flag = 3;
                request.setAttribute(LOGIN_RESULT_KEY, Result.ok(message).put("flag", flag));
                throw new DisabledAccountException(message);
            }

            if(!hasRoles(loginUserVO.getId(), customToken.getNeedRoles())) {
                message = "无权限";
                flag = 3;
                request.setAttribute(LOGIN_RESULT_KEY, Result.ok(message).put("flag", flag));
                throw new AccountException(message);
            }

            // 检测是否超过允许登录的最大数量
            int maxLoginQuantity = appConfig.getMaxLoginQuantityOfSameUser();
            if(maxLoginQuantity > 0) {
                List<Session> sessions = queryOnlineUserSessionByUserId(loginUserVO.getId());
                int needKillSessionSize = sessions.size() + 1 - maxLoginQuantity;
                // 超出大小, 从最早访问的 session 开始删除
                if(needKillSessionSize > 0) {
                    message = "已在其他设备登录";
                    flag = 2;
                    request.setAttribute(LOGIN_RESULT_KEY, Result.ok(message).put("flag", flag));
                    throw new ExcessiveAttemptsException("已在其他设备登录");
                }
            }

            // 保存登录信息
            message = "自动登录成功";
            flag = 1;
            request.setAttribute(LOGIN_RESULT_KEY, Result.ok(message).put("flag", flag));
            principal = ReflectUtil.O2ObySameField(loginUserVO, QWBaseAuthPrincipal.class);

            // 如果当前用户之前被强制下线, 则下次自动登录之前必须是用户名/密码登录
            boolean isOk = forceOfflineUserService.handleOfflineUser(loginUserVO.getUsername(), false);
            if(!isOk) {
                message = "请先进行一次实名登录";
                flag = 3;
                request.setAttribute(LOGIN_RESULT_KEY, Result.ok(message).put("flag", flag));
                throw new UnknownAccountException(message);
            }

            Session session = subject.getSession();
            session.setAttribute(appConfig.getLoginUserKey(), loginUserVO);
        } else {
            message = "无自动登录信息";
            flag = 3;
            request.setAttribute(LOGIN_RESULT_KEY, Result.ok(message).put("flag", flag));
            throw new UnknownAccountException(message);
        }

        // 保存登录日志
        loginUserVO.setAutoLogin(true);
        loginUserVO.setLoginDate(new Date());
        loginUserVO.setLoginType("auto-login");
        String ipAddress = HttpUtil.getIpAddress();
        generateAndSaveLoginLog(loginUserVO.getUsername(), "auto-login", ipAddress, "自动登录");

        // 生成授权信息并返回
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(principal, loginUserVO.getPassword(), getName());
        return authenticationInfo;
    }
}