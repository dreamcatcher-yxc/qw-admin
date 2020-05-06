define([
    'request',
], function(Request) {
    var $http = Request.$http;
    return {
        /**
         * 登录
         * @param {*} data 
         */
        login: function(data) {
            var username = data.username;
            var password = data.password;
            var checkCode = data.checkCode;
            var remember = data.remember;
            return $http.post('/rest/login', { username, password, checkCode, remember})
        },
        /**
         * 尝试根据 Cookie 信息自动登录
         */
        tryAutoLoginByCookie: function() {
            return $http.post('/rest/auto-login');
        },
        /**
         * 验证是否登录
         */
        isLogin: function() {
            return $http.get('/rest/check-login')
        },
        /**
         * 注销当前登录状态
         */
        logout: function() {
            return $http.get('/rest/logout')
        },
        /**
         * 查询菜单, 并且转换为 IMenu 支持的格式
         */
        queryMenus: function() {
            return $http.get('/rest/menus')
                .then(data => {
                    // 转换为 IMenu 支持的格式
                    var recursionHandler = (newParentMenuData, newMenus, srcMenus) => {
                        var srcMenus = srcMenus||[];
                        if(srcMenus.length <= 0) {
                            return;
                        }
                        for(var i = 0; i < srcMenus.length; i++) {
                            var newMenuData = { parent: newParentMenuData };
                            var srcMenuData = srcMenus[i];
                            newMenuData.name = srcMenuData.name;
                            newMenuData.icon = srcMenuData.icon||'dashboard';
                            newMenuData.path = srcMenuData.url||(++key);
                            newMenuData.key = newMenuData.path;
                            newMenuData.invisible = false;
                            newMenus.push(newMenuData);

                            var srcMenuDataChildren = srcMenuData.subMenus;
                            
                            if(Array.isArray(srcMenuDataChildren) && srcMenuDataChildren.length > 0) {
                                newMenuData.children = [];
                                recursionHandler(newMenuData, newMenuData.children, srcMenuDataChildren);
                            } 
                            // 无子元素，但是有父元素
                            else if(!!newParentMenuData) {
                                newMenuData.icon = null;
                            }
                        }
                    }

                    var srcMenus = data.sdata.menus||[];
                    var newMenus = [];
                    var key = 0;
                    
                    recursionHandler(null, newMenus, srcMenus);

                    return newMenus;
                });
        },
        /**
         * 从服务端拉取授权信息
         */
        validateAuth: function(keys) {
            return $http.post('/rest/auth', { keys: keys });
        },
        /**
         * 查询当前登录用户的信息
         */
        fetchUserInfo: function() {
            return $http.get('/rest/user-info')
        },
        /**
         * 删除自动登录信息
         */
        cleanAutoLoginInfo: function() {
            return $http.get('/rest/forget');
        }
    }
    
});