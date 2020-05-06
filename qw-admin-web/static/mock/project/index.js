define([
  'mockjs',
  'alias!@MOCK/extend/index'
], function(Mock, m1) {
  'use strict';
  
  var projectArr = Mock.mock({
    'list|6': [
      {
        logo: '@LOGO',
        desc: '@SAYING'
      }
    ]
  }).list;
  
  Mock.mock('/project', 'get', function() {
    return projectArr;
  });
});


