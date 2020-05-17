/**
 * 角色&权限管理相关 API
 */
define([
    'request',
], function(Request) {
    var $http = Request.$http;
    return {
        /**
         * 查看角色列表
         * @param roleName
         * @param page
         * @param limit
         */
        queryRoles: function(roleName, page, limit) {
            roleName = roleName||'';
            page = page||1;
            limit = limit||10;
            return $http.get('/base/role/rest/list', { params: { name: roleName, page: page, limit: limit } });
        },
        /**
         * 根据 roleId 查询角色信息
         * @param {*} userId 
         */
        queryRole: function(userId) {
            // 
        },
        /**
         * 获取系统可选的角色类型
         */
        queryRoleTypes: function() {
            var roleTypes = [
                { key: 'admin', value: '系统角色' },
                { key: 'mobile', value: '移动角色' }
            ];
            return Promise.resolve({
                sdata: { 
                    types: roleTypes
                }
            });
        },
        /**
         * 新增角色
         * @param {*} form.name 角色名
         * @param {*} form.description 性别 
         */
        addRole: function(form) {
            return $http.post('/base/role/rest/add', form);
        },
        /**
         * 判断角色名是否已经存在
         * @param {*} name 角色名
         */
        isExisted: function(roleName) {
            return $http.post('/base/role/rest/exist/name', { roleName: roleName })
                        .then(function(data) {
                            return !!data.sdata.exist;
                        });
        },
        /**
         * 删除 1 ~ n 条角色记录
         */
        deleteRoles: function(roleIds) {
            return $http.get('/base/role/rest/delete/' + roleIds.join(','));
        },
        /**
         * 修改角色信息
         * @param {*} form.name
         * @param {*} form.description
         */
        editRole: function(form) {
            return $http.post('/base/role/rest/modify', form);
        },
        /**
         * 修改角色对应的权限信息
         * @param {*} form.roleId 角色 ID
         * @param {*} form.pnames 权限名称集合字符串, ',' 分割
         */
        editRps: function(form) {
            return $http.post('/base/role/rest/modify/rps', form);
        },
        /**
         * 根据 roleId 查询角色拥有的信息
         * @param { String|Number } roleId 角色ID
         */
        queryRolePrivileges: function(roleId) {
            return $http.get('/base/role/rest/find/privileges/' + roleId);
        },
        /**
         * 重新生成权限树信息
         */
        privilegeGenerate: function() {
            return $http.get('/base/privilege/rest/generate');
        }
    }
});