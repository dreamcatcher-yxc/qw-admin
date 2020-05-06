define(function() {
    'use strict'

    return {
        namespaced: true,
        state: {
            isMobile: false,
            theme: 'dark',
            layout: 'side',
            systemName: 'qw-admin',
            logoPath: '@IMG/vue-antd-logo2.png',
            copyright: 'qw-admin',
            // footerLinks: [
            //     {link: 'https://pro.loacg.com/', name: 'Pro 首页'},
            //     {link: 'https://gitee.com/dreamcatcher_yxc/require-vue-admin.git', icon: 'github'},
            //     {link: 'https://www.antdv.com', name: 'Ant Design Vue'}
            // ],
            footerLinks: [
                {name: '备案号:XXXXXX'},
            ],
            multipage: true,
            headerFixed: false,
            autoHideFixedHeader: false
        },
        mutations: {
            setDevice: function(state, isMobile) {
                state.isMobile = isMobile
            },
            
            setTheme: function(state, theme) {
                state.theme = theme
            },
            
            setLayout: function(state, layout) {
                state.layout = layout
            },

            setMultipage: function(state, multipage) {
                state.multipage = multipage
            },

            setHeaderFixed: function(state, headerFixed) {
                state.headerFixed = headerFixed;
            },

            setAutoHideFixedHeader: function(state, autoHideFixedHeader) {
                if(!state.headerFixed) {
                    state.autoHideFixedHeader = false;
                } else {
                    state.autoHideFixedHeader = autoHideFixedHeader;
                }
            }
        }
    }
});