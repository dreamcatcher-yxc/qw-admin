define([
    'vue',
    'config'
], function(Vue, Conf) {
    'use strict';
    var Bus = Vue.BUS;
    return {
        closePages (closePageKeys) {
            Bus.$emit(Conf.busEvents.CLOSE_PAGE, closePageKeys);
        }
    };
});