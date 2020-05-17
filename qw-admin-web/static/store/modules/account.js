define([
    'date-fns',
    'alias-parser',
    'alias!@API/auth/index'
], function(DateFns, Parser, AuthAPI) {
    'use strict';

    var header = Parser.parse('@IMG/wq_header.jpg');

    return {
        namespaced: true,
        state: {
          logged: false,
          user: {
            timefix : DateFns.format(new Date(), 'YYYY年MM月DD日'),
            avatar : header,
            username: '',
            name : 'xiuchu.yang',
            welcome : '您好',
            position : 'xxx',
            canBeAutoLogin: false
          }
        },
        getters: {
          canBeAutoLogin (state) {
            return state.user.canBeAutoLogin;
          },
          username (state) {
            return state.user.username;
          },
          avatar (state) {
            return state.user.avatar;
          }
        },
        actions: {
          fetchUserInfo(context) {
            AuthAPI.fetchUserInfo()
              .then(data => {
                if(!!data.sdata.userInfo) {
                  context.commit('setUser', data.sdata.userInfo);
                }
              });
          }
        },
        mutations: {
          setLogged: function(state, logged) {
            state.logged = logged;
          },
          setUser: function(state, user) {
            //...
            state.user.username = user.username;
            state.user.name = user.name||user.nickname||user.username;
            state.user.avatar = user.header || user.avatar;
            state.user.canBeAutoLogin = !!user.canBeAutoLogin;
          }
        }
    }
});