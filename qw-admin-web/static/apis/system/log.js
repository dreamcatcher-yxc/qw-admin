/**
 * 系统日志查询相关 API
 */
define([
    'request',
    'jquery'
], function(Request, jQuery) {
    var $http = Request.$http;
    return {
        /**
         * 查找操作日志
         * @param searchForm.operatorName 操作者
         * @param searchForm.operation 内容
         * @param searchForm.operateMethod 方法
         * @param searchForm.ip 客户端地址
         * @param searchForm.result 处理结果
         * @param searchForm.start 开始时间
         * @param searchForm.end 结束时间
         * @param page
         * @param limit
         */
        queryLogs: function(searchForm, page, limit) {
            var params = {};
            jQuery.extend(true, params, searchForm, { page: page||1, limit: limit||10 });
            return $http.get('/base/operate/log/rest/list', { params: params } );
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
            return $http.get('/base/operate/log/rest/delete/' + logIds.join(','));
        }
    }
});