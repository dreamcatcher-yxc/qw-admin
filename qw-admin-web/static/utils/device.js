define([
  'enquire'
], function(enquireJs) {
  'use strict';
  return function (call) {
    var hanlder = {
      match: function () {
        call && call(true)
      },
      unmatch: function () {
        call && call(false)
      }
    }
    enquireJs.register('only screen and (max-width: 767.99px)', hanlder)
  };
});
