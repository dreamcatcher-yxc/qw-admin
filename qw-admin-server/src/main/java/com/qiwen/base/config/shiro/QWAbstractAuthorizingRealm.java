package com.qiwen.base.config.shiro;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.entity.Privilege;
import com.qiwen.base.entity.Role;
import com.qiwen.base.entity.User;
import com.qiwen.base.service.*;
import com.qiwen.base.vo.LoginLogVO;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static com.qiwen.base.util.SystemUtil.isLogin;

public abstract class QWAbstractAuthorizingRealm extends AuthorizingRealm {

    protected final IUserService userService;

    protected final IRoleService roleService;

    protected final IPrivilegeService privilegeService;

    protected final IAuthService authService;

    protected final ILoginLogService loginLogService;

    protected final QWAppConfig appConfig;

    public QWAbstractAuthorizingRealm(IUserService userService,
                                      IRoleService roleService,
                                      IPrivilegeService privilegeService,
                                      IAuthService authService,
                                      ILoginLogService loginLogService,
                                      QWAppConfig appConfig) {
        this.userService = userService;
        this.roleService = roleService;
        this.privilegeService = privilegeService;
        this.authService = authService;
        this.loginLogService = loginLogService;
        this.appConfig = appConfig;
    }

    protected abstract boolean isCurrentRealmAuth(PrincipalCollection principals);

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 当前的认证信息不是此作用域创建的
        if(!isCurrentRealmAuth(principals)) {
            return null;
        }

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        QWBaseAuthPrincipal principal = principals.oneByType(QWBaseAuthPrincipal.class);
        String username = principal.getUsername();
        User user = userService.findByUsername(username);
        Long userId = user.getId();
        List<Role> roles = roleService.findByUserId(userId);
        List<Privilege> privileges = privilegeService.findByUserId(userId);

        for (Role role : roles) {
            authorizationInfo.addRole(role.getName());
        }

        QWPermissionProxy permissionProxy = new QWPermissionProxy(authService, privileges);
        authorizationInfo.addObjectPermissions(Arrays.asList(permissionProxy));
        return authorizationInfo;
    }

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

    /**
     * CredentialsMatcher 都返回 true, 校验在 doGetAuthenticationInfo 里面已经完成
     * @return
     */
    @Override
    public  CredentialsMatcher getCredentialsMatcher() {
        return (token, info) -> true;
    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        super.onLogout(principals);
        if(isCurrentRealmAuth(principals) && isLogin()) {
            Subject subject = SecurityUtils.getSubject();
            Session session = subject.getSession();
            loginLogService.updateLogoutInfo((String) session.getId(), new Date(), "logout-api");
            session.removeAttribute(appConfig.getLoginUserKey());
        }
    }

    /**
     * 生成并且保存登录日志信息
     */
    protected void generateAndSaveLoginLog(String loginName, String loginType, String ip, String description) {
        Subject subject = SecurityUtils.getSubject();
        HttpServletRequest request = WebUtils.getHttpRequest(subject);

        // 解析 User-Agent 信息
        String userAgentStr = request.getHeader("User-Agent");
        StringBuilder osType = new StringBuilder();
        StringBuilder browserType = new StringBuilder();

        if(StringUtils.isNotEmpty(userAgentStr)) {
            UserAgent agent = UserAgent.parseUserAgentString(userAgentStr);
            OperatingSystem operatingSystem = agent.getOperatingSystem();
            if(operatingSystem != null) {
                osType.append(operatingSystem.getDeviceType())
                        .append("|")
                        .append(operatingSystem.getName());
            }

            Browser browser = agent.getBrowser();
            if(browser != null) {
                browserType.append(browser.getName())
                        .append("|")
                        .append(browser.getVersion(userAgentStr));
            }
        }

        if (osType.length() <= 0) {
            osType.append("unknow");
        }

        if (browserType.length() <= 0) {
            browserType.append("unknow");
        }

        LoginLogVO loginLogVO = new LoginLogVO();
        loginLogVO.setLoginName(loginName);
        loginLogVO.setIp(ip);
        loginLogVO.setLoginDate(new Date());
        loginLogVO.setLoginType(loginType);
        loginLogVO.setOsType(osType.toString());
        loginLogVO.setBrowserType(browserType.toString());
        loginLogVO.setSessionId((String) subject.getSession().getId());
        loginLogVO.setDescription(description);

        loginLogService.save(loginLogVO);
    }

}
