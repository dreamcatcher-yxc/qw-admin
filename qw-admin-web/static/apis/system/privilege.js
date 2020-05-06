/**
 * 权限管理相关 API
 */
define([
    'request'
], function(Request) {
    var $http = Request.$http;
    return {
        /**
         * 查询所有的权限树
         */
        queryPrivileges: function() {
            return $http.get('/base/privilege/rest/list');
        },

        /**
         * 根据菜单节点查询其对应的所有权限信息
         * @param {String} menuId 菜单节点
         */
        queryNamesByMenuId: function(menuId) {
            return $http.get('/base/menu/rest/pnames/' + menuId);
        }
    }
});