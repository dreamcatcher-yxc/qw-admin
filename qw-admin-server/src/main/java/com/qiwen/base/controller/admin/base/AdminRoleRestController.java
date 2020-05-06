package com.qiwen.base.controller.admin.base;

import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.*;
import com.qiwen.base.service.IPrivilegeService;
import com.qiwen.base.service.IRoleService;
import com.qiwen.base.util.LayUtil;
import com.qiwen.base.util.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "角色管理", groupName = QWConstant.MODULE_GROUP_NAME + "role-admin")
@RestController
@RequestMapping("/admin/base/role/rest")
public class AdminRoleRestController {

    private final IRoleService roleService;

    private final IPrivilegeService privilegeService;

    public AdminRoleRestController(IRoleService roleService, IPrivilegeService privilegeService) {
        this.roleService = roleService;
        this.privilegeService = privilegeService;
    }

    @Desc(value = "r-角色列表", name = "role-list")
    @GetMapping("/list")
    public String index(String name, int page, int limit) {
        Pageable pageable = LayUtil.toPageable(page, limit);
        Page<Role> all = roleService.findByCondition(name, pageable);
        return LayUtil.toLayPageJson(all);
    }

    @Desc(value = "r-添加角色", name = "role-add")
    @PostMapping("/add")
    public String add(Role role) {
        roleService.save(role);
        return Result.ok().json();
    }

    @Desc(value = "r-修改角色", name = "role-edit")
    @PostMapping("modify")
    public String modify(Role role) {
        roleService.modifyById(role);
        return Result.ok().json();
    }

    @Desc(value = "r-删除角色", name = "role-delete")
    @GetMapping("/delete/{ids}")
    public String delete(@PathVariable String ids) {
        List<String> tl = Arrays.asList(ids.split(","));
        if(CollectionUtils.isEmpty(tl)) {
            return Result.error("参数错误").json();
        }
        List<Long> idList = tl.stream().map(Long::valueOf).collect(Collectors.toList());
        roleService.deleteByIds(idList.toArray(new Long[idList.size()]));
        return Result.ok().json();
    }

    @Desc(value = "r-查询角色拥有的权限信息", name = "role-assign-privilege")
    @GetMapping("/find/privileges/{roleId}")
    public String findPrivilegesByRoleId(@PathVariable Long roleId) {
        List<Privilege> allPrivileges = privilegeService.findAll();
        List<String> pnames = privilegeService.findNamesByRoleId(roleId);
        return Result.ok()
                .put("allPrivileges", allPrivileges)
                .put("pnames", pnames)
                .json();
    }

    @Desc(value = "r-判断角色名称是否已经存在", name = "role-name-exist")
    @PostMapping("/exist/name")
    @ResponseBody
    public String existName(String roleName) {
        return Result.ok()
                .put("exist", roleService.findByName(roleName) != null)
                .json();
    }

    @Desc(value = "r-修改角色的权限信息", name = "role-assign-privilege")
    @PostMapping("/modify/rps")
    public String modifyRolePrivilegeMap(Long roleId, String pnames) {
        List<RolePrivilege> rps = new ArrayList<>();
        if(!StringUtils.isEmpty(pnames)) {
            rps = Arrays.stream(pnames.split("\\s*\\,\\s*"))
                    .map(pname -> {
                        RolePrivilege rolePrivilege = new RolePrivilege();
                        rolePrivilege.setPrivilegeName(pname);
                        rolePrivilege.setRoleId(roleId);
                        return rolePrivilege;
                    })
                    .collect(Collectors.toList());
        }
        roleService.modifyRolePrivileges(roleId, rps);
        return Result.ok().json();
    }
}
