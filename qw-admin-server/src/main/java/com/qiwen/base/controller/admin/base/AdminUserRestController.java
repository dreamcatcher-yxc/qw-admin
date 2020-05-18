package com.qiwen.base.controller.admin.base;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.Role;
import com.qiwen.base.entity.User;
import com.qiwen.base.entity.UserRole;
import com.qiwen.base.service.IRoleService;
import com.qiwen.base.service.ISessionManagerService;
import com.qiwen.base.service.IUserService;
import com.qiwen.base.util.Result;
import com.qiwen.base.vo.LoginUserVO;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.session.Session;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.qiwen.base.util.LayUtil.toLayPageJson;
import static com.qiwen.base.util.LayUtil.toPageable;

@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "用户管理", groupName = QWConstant.MODULE_GROUP_NAME + "user-admin")
@RestController
@RequestMapping("/admin/base/user/rest")
public class AdminUserRestController {

    private final IUserService userService;

    private final IRoleService roleService;

    private final ISessionManagerService sessionManagerService;

    private final QWAppConfig appConfig;

    public AdminUserRestController(
            IUserService userService,
            IRoleService roleService,
            ISessionManagerService sessionManagerService,
            QWAppConfig appConfig) {
        this.userService = userService;
        this.roleService = roleService;
        this.sessionManagerService = sessionManagerService;
        this.appConfig = appConfig;
    }

    @Desc(value = "r-用户列表", name = "user-list")
    @GetMapping("/list")
    public String index(String username, int page, int limit) {
        Page<User> result = userService.findByCondition(username, toPageable(page, limit));
        return toLayPageJson(result);
    }

    @Desc(value = "r-用户添加", name = "user-add")
    @PostMapping("/add")
    public String add(User user) {
        user.setStatus(0);
        userService.save(user);
        return Result.ok().json();
    }

    @Desc(value = "r-用户修改", name = "user-edit")
    @PostMapping("/modify")
    public String modify(User user, boolean autoReplaceHeader) {
        User srcUser = userService.findById(user.getId());
        final int srcStatus = srcUser.getStatus();
        final int newStatus = user.getStatus();
        userService.modifyById(user, autoReplaceHeader);
        final Long userId = user.getId();

        // 用户禁用
        if(srcStatus == 1 && newStatus == 0) {
            List<Session> sessions = sessionManagerService.findSessions(session -> {
                Object attr = session.getAttribute(appConfig.getLoginUserKey());
                if (attr instanceof LoginUserVO) {
                    return ((LoginUserVO) attr).getId().equals(userId);
                }
                return false;
            });
            // 该用户已经登录系统, 则踢出
            if(!CollectionUtils.isEmpty(sessions)) {
                sessions.forEach(Session::stop);
                // 主动刷新 session
                sessionManagerService.validateSessions();
                return Result.ok("该用户已禁用, 已踢出系统").json();
            }
            return Result.ok("该用户已禁用").json();
        }
        // 启用
        else if(srcStatus == 0 && newStatus == 1) {
            return Result.ok("该用户已启用").json();
        }

        return Result.ok("修改成功").json();
    }

    @Desc(value = "r-查看用户名是否存在", name = "user-username-exist")
    @PostMapping("/exist/username")
    public String existUsername(String username) {
        User user = userService.findByUsername(username);
        return Result.ok().put("exist", user != null).json();
    }

    @Desc(value = "r-用户删除", name = "user-delete")
    @GetMapping("/delete/{ids}")
    public String delete(@PathVariable String ids) {
        List<String> tl = Arrays.asList(ids.split(","));
        if(CollectionUtils.isEmpty(tl)) {
            return Result.error("参数错误").json();
        }
        List<Long> idList = tl.stream().map(Long::valueOf).collect(Collectors.toList());
        userService.deleteByIds(idList.toArray(new Long[idList.size()]));
        return Result.ok().json();
    }

    @Desc(value = "r-查询用户拥有的角色信息", name = "user-assign-role")
    @GetMapping("/find/roles/{userId}")
    public String findRolesByUserId(@PathVariable Long userId) {
        List<Role> allRoles = roleService.findAll();
        List<Long> rids = roleService.findRoleIdsByUserId(userId);
        return Result.ok()
                    .put("allRoles", allRoles)
                    .put("rids", rids)
                    .json();
    }

    @Desc(value = "r-修改指定用户的角色信息", name = "user-assign-role")
    @PostMapping("/modify/urs")
    public String modifyUserRoleMap(Long userId, String rids) {
        List<UserRole> urs = new ArrayList<>();
        if(!StringUtils.isEmpty(rids)) {
            urs  = Arrays.stream(rids.split("\\s*\\,\\s*"))
                    .map(rid -> {
                        UserRole userRole = new UserRole();
                        userRole.setRoleId(Long.valueOf(rid));
                        userRole.setUserId(userId);
                        return userRole;
                    })
                    .collect(Collectors.toList());
        }
        userService.modifyUserRoles(userId, urs);
        return Result.ok().json();
    }
}
