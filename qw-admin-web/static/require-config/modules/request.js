define([
    'config',
    'axios',
    'qs',
    'vue',
    'lodash'
], function(Config, Axios, QS, Vue, _) {
    var $http = Axios.create({
        baseURL: Config.serverBaseUrl,
        timeout: 60 * 1000,
        headers: { 'content-type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
        showLoading: true,
    })

    $http.interceptors.request.use(function (config) {
        if(config.method === 'post' || config.method === 'get') {
            config.data = QS.stringify(config.data||{})
        }
        return config;
    }, function (error) {
        return Promise.reject(error)
    });

    function serverResponseDataHandler(resp) {
        var sdata = resp.data
        resp.code = sdata.code;

        if(Array.isArray(sdata.data)) {
            resp.code = sdata.code;
            resp.sdata = sdata.data;
            resp.scount = sdata.count;
            return resp;
        } 
        
        resp.message = sdata.message;
        resp.sdata = sdata.body;

        if(sdata.code !== 1) {
            if(Vue.prototype.$bus && resp.code === 403 && resp.sdata) {
                var errorCode = resp.sdata.error_code;
                // 未登录
                if(_.includes(['403_1', '403_2'], errorCode)) {
                    Vue.prototype.$bus.$emit(Config.busEvents.SESSION_TIMEOUT);
                } 
                // 无权限
                else if(_.includes(['403_3', '403_4', '403_5'], errorCode)) {
                    Vue.prototype.$bus.$emit(Config.busEvents.NO_AUTHORITY, resp);
                }
            }
            return Promise.reject(resp);
        } 

        return resp;
    }

    $http.interceptors.response.use(
        serverResponseDataHandler, 
        function (error) {
            return Promise.reject(error);
        }
    );

    return {
        '$http': $http,
        'serverResponseDataHandler': serverResponseDataHandler 
    };
});