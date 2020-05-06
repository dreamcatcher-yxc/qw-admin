package com.qiwen.base.controller.admin.my;

import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.User;
import com.qiwen.base.service.IUserService;
import com.qiwen.base.util.LoginUtil;
import com.qiwen.base.util.Result;
import com.qiwen.base.util.SystemUtil;
import com.qiwen.base.vo.LoginUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户的基本信息, 只要是系统存在的用户都可以使用.
 */
@Slf4j
@Desc(logGroupName = "个人中心")
@RestController
@RequestMapping("/admin/my/personal/info/rest")
public class PersonalInfoRestController {

    private final IUserService userService;

    public PersonalInfoRestController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * 重新加载当前登录用户信息
     */
    private void reloadCurrentLoginUserInfo() {
        LoginUserVO currentLoginUser = SystemUtil.currentLoginUser();
        User realUser = userService.findById(currentLoginUser.getId());
        // 修改存储在 session 中的用户信息
        currentLoginUser.setHeader(realUser.getHeader());
        currentLoginUser.setNickname(realUser.getNickname());
        currentLoginUser.setGender(realUser.getGender());
        currentLoginUser.setEmail(realUser.getEmail());
        currentLoginUser.setPhone(realUser.getPhone());
        // 修改存储在认证角色中的用户信息
//        PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
//        QWBaseAuthPrincipal qwBaseAuthPrincipal = principals.oneByType(QWBaseAuthPrincipal.class);
    }

    @RequiresUser
    @Desc(log = "修改基础信息")
    @PostMapping("/modify")
    public String index(User user) {
        LoginUserVO currentLoginUser = SystemUtil.currentLoginUser();
        user.setId(currentLoginUser.getId());
        userService.modifyPersonalInfoById(user, false);
        reloadCurrentLoginUserInfo();
        return Result.ok().json();
    }

    @RequiresUser
    @Desc(log = "修改密码")
    @PostMapping("/modify/password")
    public String resetPassword(HttpSession session, HttpServletResponse response, String srcPassword, String newPassword) {
        userService.modifyPassword(SystemUtil.getUserID(), srcPassword, newPassword);
        session.invalidate();
        LoginUtil.forgetElecUser(response, "/main");
        return Result.ok().json();
    }

    @RequiresUser
    @Desc(log = "修改头像")
    @PostMapping("/modify/header")
    public String upload(User user) {
        user.setId(SystemUtil.getUserID());
        userService.modifyPersonalInfoById(user, true);
        reloadCurrentLoginUserInfo();
        return Result.ok().json();
    }

}
