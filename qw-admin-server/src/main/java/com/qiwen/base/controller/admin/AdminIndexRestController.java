package com.qiwen.base.controller.admin;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.config.shiro.*;
import com.qiwen.base.entity.Privilege;
import com.qiwen.base.service.IMenuService;
import com.qiwen.base.service.IPrivilegeService;
import com.qiwen.base.util.*;
import com.qiwen.base.vo.LoginUserVO;
import com.qiwen.base.vo.MenuVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.qiwen.base.util.SystemUtil.currentLoginUser;
import static com.qiwen.base.util.SystemUtil.isLogin;

@Slf4j
@RestController
@RequestMapping("/admin/rest")
public class AdminIndexRestController {

    private final IPrivilegeService privilegeService;

    private final IMenuService menuService;

    private final QWAppConfig appConfig;

    @Value("${spring.profiles.active}")
    private String profile = "prd";

    public AdminIndexRestController(IPrivilegeService privilegeService, IMenuService menuService, QWAppConfig appConfig) {
        this.privilegeService = privilegeService;
        this.menuService = menuService;
        this.appConfig = appConfig;
    }

    /**
     * 如果管理系统使用了记住我的功能, 该操作会将自动记住我保存在客户端的 cookie 信息去除.
     * @param response
     * @return
     */
    @GetMapping("/forget")
    @ResponseBody
    public String forgetRememberMe(HttpServletResponse response) {
        LoginUtil.forgetElecUser(response, "/admin/rest");
        return Result.ok().json();
    }

    @GetMapping("/env")
    public String env() {
        return Result.ok().put("ENV", profile).json();
    }

    @GetMapping(value = "/check-login", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String checkLogin() {
        return isLogin() ? Result.ok().json() : Result.error().json();
    }

    @RequiresUser
    @GetMapping(value = "/menus", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getMenus() {
        List<MenuVO> menus = menuService.findAllByUserId(currentLoginUser().getId());
        // 过滤掉 MenuVO 中的资源路径
        return Result.ok().put("menus", menus).json();
    }

    /**
     * 根据一组权限名称批量查询当前登录用户是否具有相应权限.
     * @return
     */
    @RequiresUser
    @PostMapping("/auth")
    public String auth(String keys) {
        LoginUserVO user = SystemUtil.currentLoginUser();
        Result ok = Result.ok();
        List<Privilege> pris = privilegeService.findByUserId(user.getId());
        List<String> aks = GsonUtil.fromJsonToClass(keys, List.class, String.class);
        final Subject subject = SecurityUtils.getSubject();
        aks.stream()
                .forEach(key ->
                    ok.put(
                            key, subject.isPermitted(new QWExpressionPermission(key, true))
                    )
                );

        return ok.json();
    }

    /**
     * 读取当前登录用户的基本信息, 包含如下信息:
     * <ul>
     *     <li>username: 登录名</li>
     *     <li>nickname: 昵称</li>
     *     <li>header: 用户头像地址</li>
     *     <li>gender: 性别</li>
     *     <li>canBeAutoLogin: 能够自动登录</li>
     *     <li>ext: 用户扩展信息, 为 null 或者 JSON 格式字符串</li>
     * </ul>
     * @return
     */
    @RequiresUser
    @GetMapping(value = "/user-info", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getUserInfo(HttpServletRequest request) {
        LoginUserVO loginUser = SystemUtil.currentLoginUser();
        Map<String, Object> userMapData = ReflectUtil.bean2Map(loginUser, true, "username", "nickname", "header", "gender", "ext", "phone", "email");
        // 判断是否能够自动登录
        LoginUtil.RememberInfo rememberInfo = LoginUtil.decipherElecUserLogonCookie(request);
        userMapData.put("canBeAutoLogin", rememberInfo != null && !rememberInfo.isExpired());
        String header = (String) userMapData.get("header");

        if(!StringUtils.isEmpty(header)) {
            userMapData.replace("header", appConfig.getImgViaBasePath() + header);
        }

        return Result.ok()
                .put("userInfo", userMapData)
                .json();
    }

    /**
     * 尝试自定登录
     * @return
     */
    @RequiresGuest
    @PostMapping(value = "/auto-login", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String autoLogin(HttpServletRequest request) {
        // 尝试根据客户端传递来的 cookie 信息进行自动登陆.
        LoginUtil.RememberInfo rememberInfo =  LoginUtil.decipherElecUserLogonCookie(request);
        String[] needRoles = { "admin" };
        QWAutoLoginToken autoLoginToken = new QWAutoLoginToken(rememberInfo, needRoles);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(autoLoginToken);
        }
        catch (UnknownAccountException e) {
            log.info("{}", e.getMessage());
        }
        Result res = (Result) request.getAttribute(QWAutoLoginRealm.LOGIN_RESULT_KEY);
        return res.json();
    }

    /**
     * 生成验证码图片
     *
     * @param request
     * @param response
     */
    @RequestMapping("/check-code")
    public void checkCode(HttpServletRequest request, HttpServletResponse response) {
        try {
            ValidateImageUtil.generateImage(request, response, true);
        } catch (IOException e) {
            log.error("cause: {}, message: {}", e.getCause(), e.getMessage());
        }
    }

    /**
     * 处理用户登陆请求.
     *
     * @param username
     * @param password
     * @param remember
     * @param request
     * @return
     */
    @RequiresGuest
    @PostMapping("/login")
    public String login(String username,
                        String password,
                        boolean remember,
                        HttpServletRequest request) {
        // 检查验证码
        boolean checkNumberIsOk = LoginUtil.checkValidateNum(request, "checkCode", true);

        if (!checkNumberIsOk) {
            return Result.error("验证码错误").json();
        }

        // 登录校验
        String[] needRoles = { "admin" };
        String rememberMePath = "/admin/rest";
        QWAuthToken authToken = new QWAuthToken(username, password, remember, rememberMePath, needRoles);
        Subject subject = SecurityUtils.getSubject();
        subject.login(authToken);

        // 获取登录成功校验信息并返回
        Result res = (Result) request.getAttribute(QWAuthRealm.LOGIN_RESULT_KEY);
        return res.json();
    }

    /**
     * 处理用户注销请求, 直接将当前会话的 session 失效.
     * @return
     */
    @RequiresUser
    @GetMapping("/logout")
    public String logout() {
        try {
            SecurityUtils.getSubject().logout();
            return Result.ok("注销成功").json();
        } catch (Exception e) {
            return Result.error("注销失败").json();
        }
    }
}
