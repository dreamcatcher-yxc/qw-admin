define([
  'mockjs',
  'alias!@MOCK/extend/index'
], function(Mock, m1) {
  'use strict';
  
  var user = Mock.mock({
    name: '@ADMIN',
    avatar: '@AVATAR',
    address: '@CITY',
    welcome: '@WELCOME',
    timefix: '@TIMEFIX',
    position: '@POSITION'
  });

  function checkLogin() {
    var jsonStr = localStorage.getItem('login');
    if(!jsonStr) {
      return false;
    }
    var now = new Date().getTime();
    var session = JSON.parse(jsonStr);
    return session.time - now > 0;
  }

  Mock.mock('/login', 'post', (req) => {
    var result = {};
    var body = JSON.parse(req.body);
    var name = body.name,
        password = body.password;
    
    if (name !== 'admin' || password !== 'admin') {
      result.code = -1;
      result.message = '账户名或密码错误（admin/admin）'
    } else {
      result.code = 0;
      result.message = Mock.mock('@TIMEFIX') + '，欢迎回来'
      result.data = {
        user: user
      };
      localStorage.setItem('login', JSON.stringify({ time: new Date().getTime() + 30 * 60 * 1000 }));
    }
    return result;
  });
  
  Mock.mock('/isLogin', 'post', ({uuid}) => {
    if(checkLogin()) {
      localStorage.setItem('login', JSON.stringify({ time: new Date().getTime() + 30 * 60 * 1000 }));
      return { code : 0 };
    }
    return { code : -1 };
  });

  Mock.mock('/logout', 'post', ({uuid}) => {
    localStorage.removeItem('login');
    return { code : 0 };
  });
});