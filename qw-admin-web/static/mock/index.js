define([
  'mockjs',
  'alias!@MOCK/user/current',
  'alias!@MOCK/project/index',
  'alias!@MOCK/user/login',
  'alias!@MOCK/workplace/index'
], function(Mock, m1, m2, m3, m4) {
  'use strict';
  // 设置全局延时
  Mock.setup({
    timeout: '300-600'
  });
});

