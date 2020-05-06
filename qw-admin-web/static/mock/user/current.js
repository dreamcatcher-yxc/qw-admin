define([
  'mockjs',
  'alias!@MOCK/extend/index'
], function(Mock, m1) {
  'use strict';
  
  var userDB = Mock.mock({
    'list|2-10': [
      {
        name: '@ADMIN',
        avatar: '@AVATAR',
        address: '@CITY',
        welcome: '@WELCOME',
        timefix: '@TIMEFIX',
        position: '@position'
      }
    ]
  }).list;
  
  Mock.mock('/user/current', 'get', () => {
    return userDB[0]
  });
});

