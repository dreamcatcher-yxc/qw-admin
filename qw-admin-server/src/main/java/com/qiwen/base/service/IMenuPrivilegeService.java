package com.qiwen.base.service;

import com.qiwen.base.entity.MenuPrivilege;

import java.util.List;

public interface IMenuPrivilegeService {

    void save(MenuPrivilege menuPrivilege);

    /**
     * menuPrivilege 中的 menuId 相同, 相当于保存 menu -> privilege 之间的 1 -> n 的关系.
     * @param menuPrivileges
     */
    void save(List<MenuPrivilege> menuPrivileges);

    void deleteByPrivilegeName(String privilegeName);

    void deleteByMenuId(Long menuId);
}
