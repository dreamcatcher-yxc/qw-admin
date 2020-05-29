/**
 * 缓存管理相关 API
 */
define([
    'request'
], function(Request) {
    var $http = Request.$http;
    return {
        /**
         * 查找所有缓存元数据
         */
        queryMetas: function() {
            return $http.get('/base/cache/rest/list');
        },
        /**
         * 清除缓存记录
         * @param cacheNames 缓存名称数组
         */
        clearCaches: function(cacheNames) {
            return $http.get('/base/cache/rest/clear/' + cacheNames.join(','));
        }
    }
});