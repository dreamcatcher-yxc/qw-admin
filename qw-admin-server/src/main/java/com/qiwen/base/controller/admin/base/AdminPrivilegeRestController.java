package com.qiwen.base.controller.admin.base;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.Privilege;
import com.qiwen.base.service.IPrivilegeService;
import com.qiwen.base.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "权限管理", groupName = QWConstant.MODULE_GROUP_NAME + "privilege-admin")
@RestController
@RequestMapping("/admin/base/privilege/rest")
public class AdminPrivilegeRestController {

    private final IPrivilegeService privilegeService;

    private final QWAppConfig appConfig;

    public AdminPrivilegeRestController(IPrivilegeService privilegeService, QWAppConfig appConfig) {
        this.privilegeService = privilegeService;
        this.appConfig = appConfig;
    }

    @Desc(value = "r-生成权限树", name = "privilege-generate")
    @GetMapping("/generate")
    public String generatePrivileges() {
        try {
            privilegeService.generatePrivilegeInfo(appConfig.getPrivilegeScanPackages().toArray(new String[]{}));
            return Result.ok().json();
        } catch (ClassNotFoundException e) {
            log.error("生成权限信息失败, cause: {}, message: {}", e.getCause(), e.getMessage());
            return Result.error().json();
        }
    }

    @Desc(value = "r-权限列表", name = "privilege-list")
    @GetMapping("/list")
    public String list() {
        List<Privilege> privileges = privilegeService.findAll();
        return Result.ok().put("privileges", privileges).json();
    }
}
