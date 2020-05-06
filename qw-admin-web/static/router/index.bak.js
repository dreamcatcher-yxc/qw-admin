define([
  'vue',
  'vue-router',
  'alias!@/store/index',
  'alias!@API/auth/index',
  'http-vue-loader'
], function (Vue, Router, store, AuthAPIS, httpVueLoader) {
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
    routes: [
      {
        path: '/login',
        name: 'login',
        component: load('@PAGE/login/Login'),
        invisible: true
      },
      {
        path: '/',
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
          {
            path: '/form',
            name: '表单页',
            component: PageView,
            icon: 'form',
            children: [
              {
                path: '/form/basic',
                name: '基础表单',
                component: load('@PAGE/form/BasicForm'),
                icon: 'none'
              },
              {
                path: '/form/step',
                name: '分步表单',
                component: load('@PAGE/form/stepForm/StepForm'),
                icon: 'none'
              },
              {
                path: '/form/advanced',
                name: '高级表单',
                component: load('@PAGE/form/advancedForm/AdvancedForm'),
                icon: 'none'
              }
            ]
          },
          {
            path: '/list',
            name: '列表页',
            component: PageView,
            icon: 'table',
            children: [
              {
                path: '/list/query',
                name: '查询表格',
                component: load('@PAGE/list/QueryList'),
                icon: 'none'
              },
              {
                path: '/list/primary',
                name: '标准列表',
                component: load('@PAGE/list/StandardList'),
                icon: 'none'
              },
              {
                path: '/list/card',
                name: '卡片列表',
                component: load('@PAGE/list/CardList'),
                icon: 'none'
              },
              {
                path: '/list/search',
                name: '搜索列表',
                component: load('@PAGE/list/search/SearchLayout'),
                icon: 'none',
                children: [
                  {
                    path: '/list/search/article',
                    name: '文章',
                    component: load('@PAGE/list/search/ArticleList'),
                    icon: 'none'
                  },
                  {
                    path: '/list/search/application',
                    name: '应用',
                    component: load('@PAGE/list/search/ApplicationList'),
                    icon: 'none'
                  },
                  {
                    path: '/list/search/project',
                    name: '项目',
                    component: load('@PAGE/list/search/ProjectList'),
                    icon: 'none'
                  }
                ]
              }
            ]
          },
          {
            path: '/detail',
            name: '详情页',
            icon: 'profile',
            component: RouteView,
            children: [
              {
                path: '/detail/basic',
                name: '基础详情页',
                icon: 'none',
                component: load('@PAGE/detail/BasicDetail')
              },
              {
                path: '/detail/advanced',
                name: '高级详情页',
                icon: 'none',
                component: load('@PAGE/detail/AdvancedDetail')
              }
            ]
          },
          {
            path: '/result',
            name: '结果页',
            icon: 'check-circle-o',
            component: PageView,
            children: [
              {
                path: '/result/success',
                name: '成功',
                icon: 'none',
                component: load('@PAGE/result/Success')
              },
              {
                path: '/result/error',
                name: '失败',
                icon: 'none',
                component: load('@PAGE/result/Error')
              }
            ]
          },
          {
            path: '/exception',
            name: '异常页',
            icon: 'warning',
            component: RouteView,
            children: [
              {
                path: '/exception/404',
                name: '404',
                icon: 'none',
                component: load('@PAGE/exception/404')
              },
              {
                path: '/exception/403',
                name: '403',
                icon: 'none',
                component: load('@PAGE/exception/403')
              },
              {
                path: '/exception/500',
                name: '500',
                icon: 'none',
                component: load('@PAGE/exception/500')
              }
            ]
          },
          {
            path: '/components',
            redirect: '/components/taskcard',
            name: '小组件',
            icon: 'appstore-o',
            component: PageView,
            children: [
              {
                path: '/components/taskcard',
                name: '任务卡片',
                icon: 'none',
                component: load('@PAGE/components/TaskCard')
              },
              {
                path: '/components/palette',
                name: '颜色复选框',
                icon: 'none',
                component: load('@PAGE/components/Palette')
              }
            ]
          },
          // 系统管理
          {
            path: '/system',
            redirect: '/system/user/index',
            name: '系统管理',
            component: PageView,
            children: [
              {
                path: '/system/user/index',
                name: '用户管理',
                component: load('@PAGE/system/user/UserList')
              },
              {
                path: '/system/role/index',
                name: '角色/权限管理',
                component: load('@PAGE/system/role/RoleList')
              },
              {
                path: '/system/task/index',
                name: '作业管理',
                component: load('@PAGE/system/task/TaskList')
              },
              {
                path: '/system/task-logs/index',
                name: '作业日志管理',
                component: load('@PAGE/system/task/TaskLogList')
              },
              {
                path: '/system/operate-logs/index',
                name: '操作日志管理',
                component: load('@PAGE/system/log/OperateLogList')
              }
            ]
          },
          // 个人中心
          {
            path: '/account/settings',
            name: 'settings',
            redirect: '/account/settings/base',
            meta: {
              menuName: '设置'
            },
            component: load('@PAGE/account/settings/Index'),
            children: [
              {
                path: 'base',
                name: 'BaseSettings',
                meta: {
                  menuName: '基础设置'
                },
                component: load('@PAGE/account/settings/BaseSetting')
              },
              {
                path: 'security',
                name: 'SecuritySettings',
                meta: {
                  menuName: '安全设置'
                },
                component: load('@PAGE/account/settings/Security')
              },
              {
                path: 'custom',
                name: 'CustomSettings',
                meta: {
                  menuName: '个性化'
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
          {
            path: '/test',
            redirect: '/test/index',
            name: '测试功能',
            component: PageView,
            children: [
              {
                path: '/test/index',
                name: 'test1',
                icon: 'none',
                component: load('@PAGE/system/index')
              },
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

  //每次路由改变的时候都判断用户状态是否有效, 如果有效, 则正常访问, 否则路由到登录页面.
  router.beforeEach((to, from, next) => {
    // debugger;
    // var logged = store.state.account.logged;
    // 判断权限
    let { name, params, query, path } = to
    let needAuth = checkNeedAuth(path)
    let isLoginPath = path === '/login'

    // 当前已经登录处理逻辑
    const serverLoggedProcess = function () {
      if (!isLoginPath) {
        // 如果有更加细粒度的权限判断, 需要在这里判断
        // 需要判断权限
        // if(needAuth) {
        // } 
        next()
      } else {
        next({ path: '/' })
      }
    }

    // 当前未登录处理逻辑
    const serverNotLoggedProcess = function () {
      if (needAuth) {
        next({ path: '/login', params: { target : name, name, params, query } })
      } else {
        next()
      }
    }

    // 已登录
    AuthAPIS.isLogin().then(() => {
      // 服务器端也是登录状态
      // debugger
        serverLoggedProcess();
        // store.commit('account/setLogged', true)
        // 服务器端未登录
    }).catch(() => {
      serverNotLoggedProcess()
      // store.commit('account/setLogged', false)
    })
  })

  router.afterEach(function (to, from) {
    // ...
  });

  return router;
 
});