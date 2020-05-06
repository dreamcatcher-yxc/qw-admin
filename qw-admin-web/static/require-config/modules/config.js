define([], function () {
    var process = {
        // 当前运行环境
        ENV : window.__env__
    };
    var env = '';

    switch (process.ENV) {
        case 'dev':
        case 'test':
            env = 'development';
            break;
        case 'prd':
            env = 'production';
            break;
    }

    return {
        env: env,
        version: window.__version__ || '1.0',
        timeout: window.__timeout__,
        serverBaseUrl: window.__server_base_url__ || '/admin',
        routerMode: window.__router_mode__ || 'hash',
        alias : {
            '@': window.__app__,
            '@R': window.__app__ + '/require-config',
            '@CMP': window.__app__ + '/components',
            '@IMG': window.__app__ + '/img',
            '@UTIL': window.__app__ + '/utils',
            '@PAGE': window.__app__ + '/pages',
            '@MOCK': window.__app__ + '/mock',
            '@ROUTE': window.__app__ + '/router',
            '@STORE': window.__app__ + '/store',
            '@ASSET': window.__app__ + '/assets',
            '@LAYOUT': window.__app__ + '/layouts',
            '@API': window.__app__ + '/apis',
            '@SERVER_IMGS_BASE_URL' : '/imgs/view/find'
        },
        busEvents: {
            // 会话超时事件
            SESSION_TIMEOUT: 'SESSION_TIMEOUT',
            // 访问资源返回无权限异常
            NO_AUTHORITY: 'NO_AUTHORITY',
            // 多标签模式下关闭指定标签事件
            CLOSE_PAGE: 'CLOSE_PAGE_TAB'
        },
        isDev: function() {
            return this.env === 'development';
        },
        isPrd: function() {
            return this.env === 'production';
        },
        format2ServerUrl: function(url) {
            var serverBaseUrl = this.serverBaseUrl;
            let len1 = serverBaseUrl.length;
            let prefixUrl = serverBaseUrl[len1 - 1] === '/' ? serverBaseUrl.substring(0, len1 - 1) : serverBaseUrl;
            let subfixUrl = url[0] === '/' ? url : '/' + url;
            return prefixUrl + subfixUrl;
        }
    };
});