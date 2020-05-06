/**
 * 用户管理相关 API
 */
define([
    'request',
], function(Request) {
    var $http = Request.$http;
    return {
        /**
         * 查找所有用户
         * @param username
         * @param page
         * @param limit
         */
        queryUsers: function(username, page, limit) {
            username = username||'';
            page = page||1;
            limit = limit||10;
            return $http.get('/base/user/rest/list', { params: { username: username, page: page, limit: limit } });
        },
        /**
         * 根据 userId 查询用户信息
         * @param {*} userId 
         */
        queryUser: function(userId) {
            // ...
        },
        /**
         * 新增用户
         * @param {*} form.username 用户名
         * @param {*} form.nickname 昵称
         * @param {*} form.gender 性别 
         */
        addUser: function(form) {
            return $http.post('/base/user/rest/add', form);
        },
        /**
         * 判断用户名是否已经存在
         * @param {*} username 用户名
         */
        isExisted: function(username) {
            return $http.post('/base/user/rest/exist/username', { username: username })
                        .then(function(data) {
                            return !!data.sdata.exist;
                        });
        },
        /**
         * 删除 1 ~ n 条用户记录
         */
        deleteUsers: function(userIds) {
            return $http.get('/base/user/rest/delete/' + userIds.join(','));
        },
        /**
         * 修改用户信息
         * @param {*} form.username
         * @param {*} form.nickname
         * @param {*} form.gender
         * @param {*} form.phone
         * @param {*} form.email
         * @param {*} form.autoReplaceHeader = true
         */
        editUser: function(form) {
            return $http.post('/base/user/rest/modify', form);
        },
        /**
         * 根据 userId 查询用户角色信息
         * @param { String|Number } userId 用户ID
         */
        queryUserRole: function(userId) {
            return $http.get('/base/user/rest/find/roles/' + userId);
        },
        /**
         * 修改用户角色信息
         * @param {*} form.userId
         * @param {*} form.rids
         */
        editUserRole: function(form) {
            return $http.post('/base/user/rest/modify/urs', form);
        }
    }
});