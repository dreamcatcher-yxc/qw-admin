/**
 * 登录日志查询相关 API
 */
define([
    'request',
    'jquery'
], function(Request, jQuery) {
    var $http = Request.$http;
    return {
        /**
         * 查找操作日志
         * @param searchForm.loginName 登录名
         * @param searchForm.startDate 开始日期
         * @param searchForm.endDate 结束日期
         * @param page
         * @param limit
         */
        queryLogs: function(searchForm, page, limit) {
            var params = {};
            jQuery.extend(true, params, searchForm, { page: page||1, limit: limit||10 });
            return $http.get('/base/login-log/rest/list', { params: params } );
        },
        /**
         * 根据 logId 查询日志详细信息
         * @param {*} logId 
         */
        queryLog: function(logId) {
            // ...
        },
        /**
         * 删除 1 ~ n 条日志记录
         */
        deleteLogs: function(logIds) {
            return $http.get('/base/login-log/rest/delete/' + logIds.join(','));
        }
    }
});