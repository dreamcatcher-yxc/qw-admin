/**
 * 全局共享数据
 */
define(function() {
    'use strict';

    function findMenuMetaDataByKey (menuData, key) {
      for(var i = 0; !!menuData && i < menuData.length; i++) {
        var metaData = menuData[i];
        if(metaData.key === key) {
          return metaData;
        }
        var result = findMenuMetaDataByKey(metaData.children, key);
        if(!!result) {
          return result;
        }
      }
      return null;
    }

    return {
        namespaced: true,
        state: {
          // 当前打开的路由路径集合
          currentLinkList: [],
          // 右侧菜单数据
          menuData: [],
          // 全局 Loading
          spinning: false,
          delayTime: 500
        },
        getters: {
          menuDataSize: function(state) {
            return state.menuData.length;
          },
          /**
           * 根据 key 查询菜单名称
           */
          getMenuNameByMenuKey: function(state) {
            return function(key) {
              return findMenuMetaDataByKey(state.menuData, key);
            }
          },
          spinning: function(state) {
            return state.spinning;
          }
        },
        mutations: {
          setCurrentLinkList: function(state, currentLinkList) {
            state.currentLinkList = [];
            currentLinkList.forEach(function(item) {
              state.currentLinkList.push(item);
            })
          },
          setMenuData: function(state, menuData) {
            state.menuData = [];
            menuData.forEach(item => {
              state.menuData.push(item);
            });
          },
          setSpinning: function(state, spinning) {
            state.spinning = spinning;
          }
        }
    }
});