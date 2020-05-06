package com.qiwen.base.service;

import com.qiwen.base.entity.Privilege;

import java.util.List;

public interface IPrivilegeService {

    /**
     * 添加一条新的权限记录.
     * @param privilege
     */
    void save(Privilege privilege);

    /**
     * 根据用户名称查询该用户拥有的所有权限.
     * @param username
     * @return
     */
    List<Privilege> findByUsername(String username);

    /**
     * 根据用户 ID 查询出该用户拥有的所有权限.
     * @param userId
     * @return
     */
    List<Privilege> findByUserId(Long userId);

    /**
     * 查询所有的权限信息.
     * @return
     */
    List<Privilege> findAll();

    /**
     * 根据菜单 ID 查询该菜单包含的所有权限.
     * @param menuId
     * @return
     */
    List<Privilege> findByMenuId(Long menuId);

    /**
     * 根据角色 ID 查询该角色包含的所有权限.
     * @param roleId
     * @return
     */
    List<Privilege> findByRoleId(Long roleId);

    List<String> findNamesByRoleId(Long roleId);

    List<String> findPrivilegeNamesByMenuId(Long menuId);

    void generatePrivilegeInfo(String[] controllerBasePackage) throws ClassNotFoundException;
}
