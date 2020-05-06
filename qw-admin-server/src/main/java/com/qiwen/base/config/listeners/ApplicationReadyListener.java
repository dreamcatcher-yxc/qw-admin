package com.qiwen.base.config.listeners;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.config.QWConstant;
import com.qiwen.base.entity.*;
import com.qiwen.base.service.IPrivilegeService;
import com.qiwen.base.service.IRoleService;
import com.qiwen.base.service.IUserService;
import com.qiwen.base.util.PackageUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(QWConstant.STARTUP_LISTENER_ORDER)
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    private QWAppConfig appConfig;

    private IUserService userService;

    private IRoleService roleService;

    private IPrivilegeService privilegeService;

    /**
     * 创建文件目录
     * @param path
     */
    private void createDirOnNotPresent(String path, String logName) {
        File file = new File(path);
        boolean res = file.exists();
        log.info("【{}】{}...", logName, res ? "已创建" : "未创建");
        if (!res) {
            res = file.mkdirs();
            if(res) {
                log.info("{}({})创建成功...", file.getAbsolutePath(), logName);
            } else {
                log.error("{}({})创建失败...", file.getAbsolutePath(), logName);
            }
        }
    }

    /**
     * 初始化系统基础权限配置信息
     */
    @SneakyThrows
    private void initSystemPermissionConfiguration() {
        User tu = userService.findByUsername(appConfig.getSuperUsername());

        // 超级管理员已经存在，首先将超级管理员信息删除.
        if(tu != null) {
            userService.deleteByIds(tu.getId());
        }

        //创建超级用户
        User user = new User();
        user.setUsername(appConfig.getSuperUsername());
        user.setNickname("超级管理员");
        user.setPassword(appConfig.getSuperUserPwd());
        user.setStatus(1);
        userService.save(user);

        // 生成角色信息
        Role role = roleService.findByName("administrator");

        if(role == null) {
            role = new Role();
            role.setName("Administrator");
            role.setDescription("包含系统所有权限");
            role.setType("admin");
            roleService.save(role);
        }

        // 重新生成权限信息
        privilegeService.generatePrivilegeInfo(appConfig.getPrivilegeScanPackages().toArray(new String[]{}));
        // 建立用户角色关联
        UserRole ur = new UserRole();
        ur.setRoleId(role.getId());
        ur.setUserId(user.getId());
        // 建立角色权限关联
        List<Privilege> privileges = privilegeService.findAll();
        long roleId = role.getId();

        List<RolePrivilege> rps = privileges.stream()
                .map(p -> {
                    RolePrivilege rp = new RolePrivilege();
                    rp.setRoleId(roleId);
                    rp.setPrivilegeName(p.getName());
                    return rp;
                }).collect(Collectors.toList());

        roleService.modifyRolePrivileges(roleId, rps);
        userService.modifyUserRoles(user.getId(), Arrays.asList(ur));
    }

    private void initJobMetaData() {
        List<String> classNames = new ArrayList<>();
        this.appConfig.getQuartzJobScanPackages()
                .forEach(sp -> classNames.addAll(PackageUtil.getClassName(sp)));

        if (!classNames.isEmpty()) {
            classNames.forEach(className -> {
                Map<String, Object> map = new HashMap<>();
                map.put("className", className);
                try {
                    Class<?> clazz = Class.forName(className);
                    Field nameField = clazz.getDeclaredField("NAME");
                    map.put("name", nameField.get(null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                QWConstant.JOB_CLASS_NAMES.add(map);
            });
        }
    }

    /**
     * 初始化基础系统数据。
     * @throws ClassNotFoundException
     */
    private void initSystemBaseInfo() throws ClassNotFoundException {
        // 上传文件存储路径
        createDirOnNotPresent(appConfig.getFileSaveDir(), "文件持久存储目录");
        // 上传文件缓存目录
        createDirOnNotPresent(appConfig.getFileTempDir(), "文件暂存存储目录");
        // 基本配置路径
        createDirOnNotPresent(appConfig.getConfigBaseDir(), "外部配置文件存储目录");
        // 需要生成超级管理员账户信息
        if (appConfig.isGenerateSuperUserInfo())  {
            initSystemPermissionConfiguration();
        }
        // 初始化任务数组
        initJobMetaData();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        ConfigurableApplicationContext context = applicationReadyEvent.getApplicationContext();
        userService = context.getBean(IUserService.class);
        roleService = context.getBean(IRoleService.class);
        privilegeService = context.getBean(IPrivilegeService.class);
        appConfig = context.getBean(QWAppConfig.class);

        try {
            initSystemBaseInfo();
        } catch (ClassNotFoundException e) {
            log.error("系统初始化异常, cause: {}, message: {}", e.getCause(), e.getMessage());
        }
    }
}