define([
  'mockjs',
  'alias!@MOCK/common/index'
], function(Mock, Common) {
  'use strict';
  
  var logos = Common.logos,
      sayings = Common.sayings,
      positions = Common.positions,
      avatars = Common.avatars,
      admins = Common.admins,
      Random = Mock.Random;

  Random.extend({
    admin () {
      return this.pick(admins)
    },
    welcome () {
      return this.pick(['休息一会儿吧', '准备吃什么呢', '要不要午休一下', '我猜你可能累了'])
    },
    timefix () {
      const time = new Date()
      const hour = time.getHours()
      return hour < 9 ? '早上好' : (hour <= 11 ? '上午好' : (hour <= 13 ? '中午好' : (hour <= 20 ? '下午好' : '晚上好')))
    },
    avatar () {
      return this.pick(avatars)
    },
    position () {
      return this.pick(positions)
    },
    saying () {
      return this.pick(sayings)
    },
    logo () {
      return this.pick(logos)
    }
  });
});

