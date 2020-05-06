/*
 * 包含所有移动端网页所有自定义配置，requirejs 相关配置。
 */
require.config({
    baseUrl: window.__app__ + '/require-config/modules/',
    waitSeconds : window.__timeout__,
    urlArgs: "browser_spa_version=" + window.__version__,
    paths : {
        // 自定义插件
        'es6' : '../plugins/es6-plugin',
        'css' : '../plugins/css-plugin',
        'server' : '../plugins/server-plugin',
        'condition' : '../plugins/condition-plugin',
        'alias' : '../plugins/alias-plugin',
        'async': '../plugins/async',
        'font': '../plugins/font',
        'goog': '../plugins/goog',
        'image': '../plugins/image',
        'json': '../plugins/json',
        'noext': '../plugins/noext',
        'mdown': '../plugins/mdown',
        'propertyParser' : '../plugins/propertyParser',
        'markdownConverter' : '../plugins/Markdown.Converter',
        // 自定义模块
        'es6-parser' : 'es6-to-es5',
        'alias-parser' : 'alias-parser',
        'html-parser' : 'html-parser',
        'config' : 'config',
        'http-vue-loader' : 'http-vue-loader',
        'validator' : 'validator',
        'async-validator' : 'async-validator',
        'mixins' : 'mixins',
        'source-map' : 'source-map',
        'async-watcher' : 'async-watcher',
        'sortablejs' : 'sortable.min',
        'mixins' : 'mixins',
        // 服务端 http 请求发起工具
        'request' : 'request',
        // 图表
        'viser-vue' : '../../assets/libs/viser/viser-vue.min',
        // Antd 组件
        'antd' : '../../assets/libs/antd/antd.min',
        // vue-cropper 组件
        'vue-cropper' : '../../assets/libs/vue-cropper/index',
        // 图表
        'antv-data-set' : '../../assets/libs/antd/data-set.min',
        // Vue 可拖拽组件
        'vuedraggable' : '../../assets/libs/vuedraggable/vuedraggable.umd.min',
        // lottile(SVG 动画库)
        'lottie' : '../../assets/libs/lottie/lottie.min',
        // queryString
        'qs': '../../assets/libs/qs/qs',
        // CDN 资源
        // 日期转换工具库
        'date-fns' : '../../assets/libs/date-fns/date_fns.min',
        'jquery' : '../../assets/libs/jquery/jquery.min',
        'vue' : window.__env__ === 'dev' ? '../../assets/libs/vue/vue' : '../../assets/libs/vue/vue.min',
        'browser' : '../../assets/libs/polyfill/polyfill.min',
        'babel' : '../../assets/libs/babel/babel.min',
        'moment' : '../../assets/libs/moment/moment.min',
        'vuex' : '../../assets/libs/vue/vuex.min',
        'vue-router' : '../../assets/libs/vue/vue-router.min',
        'enquire' : '../../assets/libs/enquire/enquire.min',
        'mockjs' : '../../assets/libs/mock/mock-min',
        // axios
        'axios' : '../../assets/libs/axios/axios.min',
        // lodash
        'lodash': '../../assets/libs/lodash/lodash.min',
        // zTree
        'zTree': '../../assets/libs/ztree/js/jquery.ztree.all.min'
    },

    shim : {
        'QRCode' : {
            exports : 'qrcode.min.js'
        }
    }
});

