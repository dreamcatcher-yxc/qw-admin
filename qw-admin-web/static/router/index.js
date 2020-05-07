define([
  'vue',
  'vue-router',
  'alias!@API/auth/index',
  'alias!@STORE/index',
  'http-vue-loader',
  'antd',
  'config',
], function (Vue, Router, AuthAPIS, store, httpVueLoader, AntD, Conf) {
  'use strict';

  function load(url) {
    url = url.lastIndexOf('.vue') !== (url.length - 4) ? (url + '.vue') : url;
    return httpVueLoader.load(url);
  }

  var originalPush = Router.prototype.push;

  Router.prototype.push = function (location) {
    return originalPush.call(this, location).catch(err => err);
  };

  Vue.use(Router);

  var MenuView = load('@LAYOUT/MenuView'),
    PageView = load('@LAYOUT/PageView'),
    RouteView = load('@LAYOUT/RouteView');

  var router = new Router({
    mode: Conf.routerMode,
    routes: [
      {
        path: '/',
        name: 'contextHome',
        redirect: '/index'
      },
      {
        path: '/login',
        name: 'login',
        component: load('@PAGE/login/Login'),
        invisible: true
      },
      {
        path: '/index',
        name: 'home',
        component: MenuView,
        redirect: '/system',
        meta: {
          menuName: '主页'
        },
        children: [
          {
            path: '/dashboard',
            name: '仪表板',
            component: RouteView,
            icon: 'dashboard',
            children: [
              {
                path: '/dashboard/workplace',
                name: '工作台',
                component: load('@PAGE/dashboard/WorkPlace'),
                icon: 'none'
              },
              {
                path: '/dashboard/analysis',
                name: '分析页',
                component: load('@PAGE/dashboard/Analysis'),
                icon: 'none'
              }
            ]
          },
          // 系统管理
          {
            path: '/system',
            redirect: '/system/user/index',
            name: 'system-admin',
            meta: {
              menuName: '系统管理'
            },
            component: PageView,
            children: [
              {
                path: '/system/user/index',
                name: 'user-admin',
                meta: {
                  menuName: '用户管理'
                },
                component: load('@PAGE/system/user/UserList')
              },
              {
                path: '/system/role/index',
                name: 'role-privilege-admin',
                meta: {
                  menuName: '角色/权限管理'
                },
                component: load('@PAGE/system/role/RoleList')
              },
              {
                path: '/system/task/index',
                name: 'task-admin',
                meta: {
                  menuName: '作业管理'
                },
                component: load('@PAGE/system/task/TaskList')
              },
              {
                path: '/system/online-user/index',
                name: 'online-user-admin',
                meta: {
                  menuName: '在线用户管理'
                },
                component: load('@PAGE/system/online/OnlineUserList')
              },
              {
                path: '/system/task-logs/index',
                name: 'task-log-admin',
                meta: {
                  menuName: '任务日志管理'
                },
                component: load('@PAGE/system/task/TaskLogList')
              },
              {
                path: '/system/operate-logs/index',
                name: 'operation-log-admin',
                meta: {
                  menuName: '操作日志管理'
                },
                component: load('@PAGE/system/log/OperateLogList')
              },
              {
                path: '/system/login-logs/index',
                name: 'login-log-admin',
                meta: {
                  menuName: '登录日志管理'
                },
                component: load('@PAGE/system/log/LoginLogList')
              }
            ]
          },
          {
            path: '/test',
            redirect: '/test/test1',
            name: 'test-function',
            component: PageView,
            children: [
              {
                path: 'test1',
                name: 'test1',
                component: load('@PAGE/test/test1')
              },
              {
                path: 'test2',
                name: 'test2',
                component: load('@PAGE/test/test2')
              }
            ]
          },
          // 个人中心
          {
            path: '/account/settings',
            name: 'settings',
            redirect: '/account/settings/base',
            meta: {
              menuName: '设置',
              icon: 'setting'
            },
            component: load('@PAGE/account/settings/Index'),
            children: [
              {
                path: 'base',
                name: 'BaseSettings',
                meta: {
                  menuName: '基础设置',
                  icon: 'laptop'
                },
                component: load('@PAGE/account/settings/BaseSetting')
              },
              {
                path: 'security',
                name: 'SecuritySettings',
                meta: {
                  menuName: '安全设置',
                  icon: 'safety-certificate'
                },
                component: load('@PAGE/account/settings/Security')
              },
              {
                path: 'custom',
                name: 'CustomSettings',
                meta: {
                  menuName: '个性化',
                  icon: 'setting'
                },
                component: load('@PAGE/account/settings/Custom')
              },
              {
                path: 'binding',
                name: 'BindingSettings',
                meta: {
                  menuName: '账户绑定'
                },
                component: load('@PAGE/account/settings/Binding')
              },
              {
                path: 'notification',
                name: 'NotificationSettings',
                meta: {
                  menuName: '新消息通知'
                },
                component: load('@PAGE/account/settings/Notification')
              }
            ]
          },
          // 异常页面
          {
            path: '/exception',
            name: '异常页',
            component: RouteView,
            children: [
              {
                path: '/exception/404',
                name: '404',
                component: load('@PAGE/exception/404')
              },
              {
                path: '/exception/403',
                name: '403',
                component: load('@PAGE/exception/403')
              },
              {
                path: '/exception/500',
                name: '500',
                component: load('@PAGE/exception/500')
              }
            ]
          }
        ]
      }
    ]
  });

  // 检查当前访问的路径名称是否需要检验权限
  function checkNeedAuth(path) {
    return path !== '/login';
  }

  // var pattern = /^\/(test|system|account\/settings)(\/[a-zA-Z0-9\-\_]+)+$/
  // 是否需要判断是否在 menuData 中存在
  function isRequiredMatchMenu(path) {
    var patterns = [
      /^\/(test|system)(\/[a-zA-Z0-9\-\_]+)+$/
    ];
    for(var i = 0; i < patterns.length; i++) {
      if(patterns[i].test(path)) {
        return true;
      }
      return false;
    }
  }

  //每次路由改变的时候都判断用户状态是否有效, 如果有效, 则正常访问, 否则路由到登录页面.
  router.beforeEach((to, from, next) => {
    if (to.matched.length === 0) {
      if(to.name === '404') {
        next();
      } else {
        next({ name: '404' });
      }
      return;
    } 

    // 判断权限
    var name = to.name;
    var params = to.params;
    var query = to.query;
    var path = to.path;
    var needAuth = checkNeedAuth(path)
    var isLoginPath = path === '/login'

    // 当前已经登录处理逻辑
    function serverLoggedProcess() {
      if (!isLoginPath) {
        if(isRequiredMatchMenu(path)) {
          var menuData = store.getters['share/menuDataSize'] > 0 ? store.getters['share/getMenuNameByMenuKey'](path) : true;
          if(!menuData) {
            next({ name: '403' });
            return;
          }
        }
        next();
        return;
      }
      next({ name: 'home' });
    }

    // 当前未登录处理逻辑
    function serverNotLoggedProcess() {
      if (needAuth) {
        AuthAPIS.tryAutoLoginByCookie()
          .then(function(resp) {
            if(resp.sdata.flag === 1) {
              AntD.message.success('自动登录');
              next({ name: 'home' });
            } else {
              return Promise.reject();
            }
          })
          .catch(function() {
            next({ path: '/login', params: { target : name, name, params, query } });
          });
      } else {
        next();
      }
    }

    // 已登录
    store.commit('share/setSpinning', true);
    AuthAPIS.isLogin().then(() => {
      // 服务器端也是登录状态
      // debugger
      serverLoggedProcess();
    }).catch(() => {
      serverNotLoggedProcess()
    }).finally(() => {
      store.commit('share/setSpinning', false);
    });
  })

  router.afterEach(function (to, from) {
    // ...
  });

  return router;
});