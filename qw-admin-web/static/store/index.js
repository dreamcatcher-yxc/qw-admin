define([
    'vue',
    'vuex',
    'alias!@STORE/modules/account',
    'alias!@STORE/modules/setting',
    'alias!@STORE/modules/screen',
    'alias!@STORE/modules/share'
], function(Vue, Vuex, account, setting, screen, share) {
    'use strict';
    
    Vue.use(Vuex)

    var store = new Vuex.Store({
        modules: {
            account: account,
            setting: setting,
            screen: screen,
            share: share
        }
    });

    // var $win = $(window);

    // $win.resize(function() {
    //     store.commit('screen/update', {
    //         width: $win.width(),
    //         height: $win.height() 
    //     });
    // });

    return store;
});