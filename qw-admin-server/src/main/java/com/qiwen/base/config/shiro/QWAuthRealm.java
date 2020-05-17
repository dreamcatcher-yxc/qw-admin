package com.qiwen.base.config.shiro;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.entity.User;
import com.qiwen.base.service.*;
import com.qiwen.base.util.*;
import com.qiwen.base.util.http.HttpUtil;
import com.qiwen.base.vo.LoginUserVO;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class QWAuthRealm extends QWAbstractAuthorizingRealm {

    public static final String REALM_NAME = "QW-LOGIN-AUTHORIZATION-REALM";

    public static final String LOGIN_RESULT_KEY = "QW_AUTH_REALM_LOGIN_RESULT";

    private final IForceOfflineUserService forceOfflineUserService;

    public QWAuthRealm(IUserService userService,
                       IRoleService roleService,
                       IPrivilegeService privilegeService,
                       IAuthService authService,
                       ILoginLogService loginLogService,
                       IForceOfflineUserService forceOfflineUserService,
                       QWAppConfig appConfig
                       ) {
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
        return token instanceof QWAuthToken;
    }

    protected boolean isCurrentRealmAuth(PrincipalCollection principals) {
        Set<String> realmNames = principals.getRealmNames();
        return !CollectionUtils.isEmpty(realmNames) && realmNames.contains(REALM_NAME);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        QWAuthToken customToken = (QWAuthToken) token;
        String username = customToken.getUsername();
        String validEncryptPwd = PasswordUtil.encryptPassword(String.valueOf(customToken.getPassword()));
        User userPO = userService.findByUsernameAndPassword(username, validEncryptPwd);
        LoginUserVO loginUserVO = ReflectUtil.O2ObySameField(userPO, LoginUserVO.class);
        // 登录校验
        if (loginUserVO == null) {
            throw new UnknownAccountException("用户名或者密码错误");
        }

        // 用户被禁用
        if(userPO.getStatus() == 0) {
            throw new DisabledAccountException("该用户已被禁用");
        }

        // 登录所需角色校验
        if(!hasRoles(loginUserVO.getId(), customToken.getNeedRoles())) {
            throw new UnsupportedTokenException("无权限登录系统");
        }

        // 记住我功能
        boolean rememberMe = customToken.isRememberMe();
        Subject subject = SecurityUtils.getSubject();
        // 访问管理 /admin(系统主页) 页面及所有自路径的时候都带入自动登录的 cookie 信息给服务器.
        if (rememberMe) {
            HttpServletResponse response = WebUtils.getHttpResponse(subject);
            String rememberMeCookieName = customToken.getRememberMeCookieName();
            if(StringUtils.isEmpty(rememberMeCookieName)) {
                LoginUtil.rememberElecUser(response, customToken.getRememberMePath(), username, loginUserVO.getPassword());
            } else {
                LoginUtil.rememberElecUser(response, customToken.getRememberMePath(), username, loginUserVO.getPassword(), rememberMeCookieName);
            }
        }

        // 保存登录信息至 session
        String message = null;
        int maxLoginQuantity = appConfig.getMaxLoginQuantityOfSameUser();
        if(maxLoginQuantity > 0) {
            List<Session> sessions = queryOnlineUserSessionByUserId(loginUserVO.getId());
            int needKillSessionSize = sessions.size() + 1 - maxLoginQuantity;
            // 超出大小, 从最早访问的 session 开始删除
            if(needKillSessionSize > 0) {
                sessions.sort((s1, s2) -> s1.getLastAccessTime().getTime() <= s2.getLastAccessTime().getTime() ? -1 : 1);
                sessions.subList(0, needKillSessionSize).forEach(Session::stop);
                this.sessionManagerService.validateSessions();
            }
        }

        // 如果当前用户之前都强制下线, 则删除
        boolean isOk = forceOfflineUserService.handleOfflineUser(loginUserVO.getUsername(), false);
        if(!isOk) {
            forceOfflineUserService.deleteByUsernames(username);
        }

        // 保存登录信息至 session
        Session session = subject.getSession();
        session.setAttribute(appConfig.getLoginUserKey(), loginUserVO);
        QWBaseAuthPrincipal principal = ReflectUtil.O2ObySameField(loginUserVO, QWBaseAuthPrincipal.class);

        // 保存登录提示信息
        HttpServletRequest request = WebUtils.getHttpRequest(subject);
        request.setAttribute(LOGIN_RESULT_KEY, Result.ok(message));

        // 保存登录日志
        loginUserVO.setAutoLogin(false);
        loginUserVO.setLoginDate(new Date());
        loginUserVO.setLoginType("login");
        String ipAddress = HttpUtil.getIpAddress();
        generateAndSaveLoginLog(username, "login", ipAddress, "用户名/密码登录");

        // 生成授权信息并返回
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(principal, loginUserVO.getPassword(), getName());
        return authenticationInfo;
    }

}