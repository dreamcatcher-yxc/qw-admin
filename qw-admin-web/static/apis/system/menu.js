/**
 * 菜单管理相关 API
 */
define([
    'request',
    'jquery'
], function(Request, jQuery) {
    var $http = Request.$http;
    return {
        /**
         * 查询菜单树
         */
        queryMenus: function() {
            return $http.get('/base/menu/rest/list');
        },

        /**
         * 根据 menuId 查询菜单详情
         * @param {String} menuId 
         */
        queryMenu: function(menuId) {
            return $http.get('/base/menu/rest/find/' + menuId);
        },

        /**
         * 修改菜单节点信息
         * @param {String} formData.id ID
         * @param {String} formData.name 名称
         * @param {String} formData.url 资源路径(可以为空字符串)
         * @param {String} formData.icon 图标(可以为空字符串)
         * @param {String} formData.description 描述信息
         */
        editMenu: function(formData) {
            return $http.post('/base/menu/rest/modify', formData);
        },

        /**
         * 修改菜单节点名称
         * @param {String} formData.id ID
         * @param {String} formData.name 名称
         */
        editMenuName: function(formData) {
            return $http.post('/base/menu/rest/rename', formData);
        },

        /**
         * 修改菜单对应权限
         * @param {String} formData.menuId menuId
         * @param {String} formData.privilegeNames 权限名称集合字符串, ',' 分割 
         */
        editMenuPrivileges: function(formData) {
            return $http.post('/base/menu/rest/add-map', formData);
        },

        /**
         * 菜单节点移动
         * @param moveMenuNodes
         */
        adjustMenuNodes: function (moveMenuNodes) {
            return $http.post('/base/menu/rest/move', { moves: moveMenuNodes });
        },

        /**
         * 添加新的菜单节点
         * @param {String} formData.treeId
         * @param {String} formData.treeParentId
         * @param {String} formData.orderIndex
         * @param {String} formData.name
         * @param {String} formData.url
         * @param {String} formData.description
         * @param {String} formData.isLeaf
         */
        addMenuNode: function(formData) {
            return $http.post('/base/menu/rest/add', formData);
        },

        /**
         * 删除1 ~ n 个菜单节点
         * @param {String} formData.ids 需要删除的节信息
         * @param {String} formData.adjusts 需要更新的节点信息集合
         */
        deleteMenuNodes: function(formData) {
            return $http.post('/base/menu/rest/delete', formData);
        }
    }
});