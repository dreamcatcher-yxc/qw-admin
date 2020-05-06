define([
  'mockjs',
  'alias!@MOCK/common/activityData',
  'alias!@MOCK/common/index'
], function(Mock, AD, Common) {
  'use strict';

  // console.log('mockjs.....');
  var activities = AD.activities,
      templates = AD.templates,
      teams = Common.teams;

  activities.forEach(function(item) {
    item.template = templates[item.activity.type](item.user, item.activity);
  });
  
  Mock.mock('/work/activity', 'get', function() {
    return activities;
  });
  
  Mock.mock('/work/team', 'get', function() {
    return teams;
  });
});