define([
  'jquery'
],function(jQuery) {
    'use strict';

    var $win = jQuery(window);
    var width = $win.width();
    var height = $win.height();

    var module = {
        namespaced: true,
        state: {
          width: width,
          height: height
        },
        getters: {
          screenInfo (state) {
            return {
              width: state.width,
              height: state.height
            };
          }
        },
        mutations: {
          resize (state, screenInfo) {
            state.width = screenInfo.width;
            state.height = screenInfo.height;
          }
        }
    };

    return module;
});