define([
  'alias-parser'
], function(aliasParser) {
  'use strict'
  return {
    403: {
      img: aliasParser.parse('@IMG/403.svg'),
      title: '403',
      desc: '抱歉，你无权访问该页面'
    },
    404: {
      img: aliasParser.parse('@IMG/404.svg'),
      title: '404',
      desc: '抱歉，你访问的页面不存在或仍在开发中'
    },
    500: {
      img: aliasParser.parse('@IMG/500.svg'),
      title: '500',
      desc: '抱歉，服务器出错了'
    }
  };
});
