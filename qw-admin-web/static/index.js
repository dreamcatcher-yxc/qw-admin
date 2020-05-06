require([
    'vue', 
    'http-vue-loader', 
    'config', 
    'antd',
    'viser-vue',
    'axios',
    'alias!@ROUTE/index',
    'alias!@STORE/index',
    'jquery', 
    // 'alias!@MOCK/index',
    'css!../../assets/libs/antd/antd.min.css'
], function(Vue, HttpVueLoader, Config, AntD, Viser, Axios, router, store, $, __cssText) {

    if(Config.env === 'development') {
        window.Vue = Vue;
        Vue.config.devtools = true;
    }

    Vue.use(HttpVueLoader);
    Vue.use(AntD);
    Vue.use(Viser.default);

    // 全局混入
    Vue.mixin({
        filters: {
            emptySafe : function (value) {
                if(value == null) {
                    return '/';
                }

                if(typeof value === 'string') {
                    if(value.length <= 0) {
                        return '';
                    }
                    return value;
                }

                return value;
            }
        }
    });

    // 自定义插件
    Vue.use({
        install : function (Vue, options) {
            Vue.prototype.jquery = $;
            Vue.prototype.jQuery = $;
            Vue.prototype.$axios = Axios;
            Vue.prototype.$bus = new Vue({});
            Vue.BUS = Vue.prototype.$bus;
        }
    });

    // 全局混入
    Vue.mixin({
        beforeRouteEnter (to, from, next) {
            next(function(vm) {
                var path = vm.$route.path;
                var isMultipage = vm.$store.state.setting.multipage;
                var currentLinkList = vm.$store.state.share.currentLinkList;
                var item = currentLinkList.filter(function(item) {
                    return item.path === path;
                });
                // 多页模式 & 不是首次打开页面 & 当前路径是新打开的 & 当前组件的 __vm_inited__ 字段为 true
                if(isMultipage && item.length > 0 && item[0].isNew && !!vm.__vm_inited__) {
                    vm.reopen&&vm.reopen();
                }
            })
        }
    })

    // 初始化 Vue
    new Vue({
        router: router,
        store: store,
        components: { 
            App : HttpVueLoader.load('@/App.vue')
        }
    }).$mount('#antd-vue-admin-root');
});