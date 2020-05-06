/**
 * 在线用户管理相关 API
 */
define([
    'request',
    'jquery'
], function(Request, jQuery) {
    var $http = Request.$http;
    return {
        /**
         * 查找所有在线用户
         * @param searchForm.username 用户名
         * @param page
         * @param limit
         */
        queryOnlineUsers: function(searchForm, page, limit) {
            var params = {};
            jQuery.extend(true, params, searchForm, { page: page||1, limit: limit||10 });
            page = page||1;
            limit = limit||10;
            return $http.get('/base/online-user/rest/list', { params: params });
        },
        
        /**
         * 删除 1 ~ n 条在线用户记录
         */
        deleteOnlineUsers: function(sessionIds) {
            return $http.get('/base/online-user/rest/delete/' + sessionIds.join(','));
        }
    }
});