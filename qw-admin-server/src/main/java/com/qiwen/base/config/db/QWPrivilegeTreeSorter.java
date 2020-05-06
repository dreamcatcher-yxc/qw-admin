package com.qiwen.base.config.db;

import com.qiwen.base.entity.Privilege;

import java.util.List;

/**
 * 权限树排序器
 */
public abstract class QWPrivilegeTreeSorter {

    /**
     * 排序方法回调
     * @param currentPrivilege 当前节点
     * @param parentPrivileges 所有父节点
     * @param childPrivileges 所有子节点
     * @param allPrivileges 所有节点
     */
    public abstract void sort(Privilege currentPrivilege, List<Privilege> parentPrivileges, List<Privilege> childPrivileges, List<Privilege> allPrivileges);
}
