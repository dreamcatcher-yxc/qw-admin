package com.qiwen.base.service;

import com.qiwen.base.entity.Privilege;

import java.util.List;

public interface IAuthService {

    /**
     * 根据 expression 判断当前登陆用户是否具有访问该资源的权限, 根据 needConvertExpression 的情况可以分为如下两种情况:<br/><br/>
     * <p>
     *     如果 needConvertExpression = true, 则以 expression 为权限名称的表达式,类似于 user-add | role-add (1), 此时需要将 (1) 转换为实际的 url 资源模板的形式,
     *     如 user-add <=> /user/view/add, /user/rest/add;role-add <=> /role/view/add,/role/rest/add; 则可以将 (1) 转换为 (/user/view/add|/user/rest/add)|(/role/view/add|/role/rest/add)
     * <p/>
     * <p>
     *     如果 needConvertExpression = false, 则 expression 为已经转换好的表达式, 如: /foo/view/add/*
     * </p>
     * @param expression: 权限表达式
     * @param isUrlExpression: expression 是否需要转换
     * @param privilegeList: 当前用户拥有的权限列表
     * @return
     */
    boolean calculatePermissionExpression(String expression, boolean isUrlExpression, List<Privilege> privilegeList);

}
