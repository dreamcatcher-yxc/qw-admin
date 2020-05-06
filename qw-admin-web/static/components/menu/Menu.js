/**
 * 该插件可根据菜单配置自动生成 ANTD menu组件
 * menuData示例：
 * [
 *  {
 *    title: '菜单标题',
 *    icon: '菜单图标',
 *    path: '菜单路由',
 *    invisible: 'boolean, 是否不可见',
 *    children: [子菜单配置]
 *  },
 *  {
 *    title: '菜单标题',
 *    icon: '菜单图标',
 *    path: '菜单路由',
 *    invisible: 'boolean, 是否不可见',
 *    children: [子菜单配置]
 *  }
 * ]
 **/
define([
  'antd'
], function(antd) {
  'use strict';
  var Menu = antd.Menu,
        Icon = antd.Icon;
  var Item = Menu.Item,
      SubMenu = Menu.SubMenu;

  // 默认菜单图标数组，如果菜单没配置图标，则会设置从该数组随机取一个图标配置
  var iconArr = ['dashboard', 'user', 'form', 'setting', 'message', 'safety', 'bell', 'delete', 'code-o', 'poweroff', 'eye-o', 'hourglass']

  // console.log('函数式组件...');

  return {
      name: 'IMenu',
      props: {
        menuData: {
          type: Array,
          required: true
        },
        theme: {
          type: String,
          required: false,
          default: 'dark'
        },
        mode: {
          type: String,
          required: false,
          default: 'inline'
        },
        collapsed: {
          type: Boolean,
          required: false,
          default: false
        }
      },
      data: function() {
        return {
          openKeys: [],
          selectedKeys: [],
          cachedOpenKeys: []
        }
      },
      computed: {
        rootSubmenuKeys: function(vm) {
          let keys = []
          vm.menuData.forEach(item => {
            keys.push(item.path)
          });
          return keys;
        }
      },
      created: function() {
        this.updateMenu();
      },
      mounted: function() {
        this.jQuery('#loading-mask').remove();
      },
      watch: {
        collapsed: function(val) {
          if (val) {
            this.cachedOpenKeys = this.openKeys;
            this.openKeys = [];
          } else {
            this.openKeys = this.cachedOpenKeys;
          }
        },
        '$route': function () {
          this.updateMenu()
        }
      },
      methods: {
        renderIcon: function (h, icon) {
          return icon === 'none' ? null
            : h(
              Icon,
              {
                props: {type: icon !== undefined ? icon : iconArr[Math.floor((Math.random() * iconArr.length))]}
              })
        },
        renderMenuItem: function (h, menu, pIndex, index) {
          var contents = [h('span', [menu.name])];
          if(menu.icon) {
            contents.splice(0, 0, this.renderIcon(h, menu.icon));
          }
          return h(
            Item,
            {
              key: menu.path ? menu.path : 'item_' + pIndex + '_' + index
            },
            [
              h(
                'a',
                {attrs: {'qwhref': '#' + menu.path}},
                contents
              )
            ]
          )
        },
        renderSubMenu: function (h, menu, pIndex, index) {
          var this2_ = this;
          var subItem = [h('span',
            {slot: 'title'},
            [
              this.renderIcon(h, menu.icon),
              h('span', [menu.name])
            ]
          )]
          var itemArr = [];
          var pIndex_ = pIndex + '_' + index;
          menu.children.forEach(function (item, i) {
            itemArr.push(this2_.renderItem(h, item, pIndex_, i));
          })
          return h(
            SubMenu,
            {key: menu.path ? menu.path : 'submenu_' + pIndex + '_' + index},
            subItem.concat(itemArr)
          )
        },
        renderItem: function (h, menu, pIndex, index) {
          if (!menu.invisible) {
            return menu.children ? this.renderSubMenu(h, menu, pIndex, index) : this.renderMenuItem(h, menu, pIndex, index);
          }
        },
        renderMenu: function (h, menuTree) {
          // debugger;
          var this2_ = this
          var menuArr = []
          menuTree.forEach(function (menu, i) {
            menuArr.push(this2_.renderItem(h, menu, '0', i));
          })
          return menuArr
        },
        onOpenChange (openKeys) {
          const latestOpenKey = openKeys.find(key => this.openKeys.indexOf(key) === -1)
          if (this.rootSubmenuKeys.indexOf(latestOpenKey) === -1) {
            this.openKeys = openKeys;
          } else {
            this.openKeys = latestOpenKey ? [latestOpenKey] : [];
          }
        },
        findMenuMetaDataByPath (path, menuData) {
          for(var i = 0; !!menuData && i < menuData.length; i++) {
            var metaData = menuData[i];
            if(metaData.path === path) {
              return metaData;
            }
            var result = this.findMenuMetaDataByPath(path, metaData.children);
            if(!!result) {
              return result;
            }
          }
          return null;
        },
        updateMenu () {
          var routes = this.$route.matched.concat();
          this.selectedKeys = [routes.pop().path];
          var selectKeyMetaData = this.findMenuMetaDataByPath(this.selectedKeys[0], this.menuData);
          if(!selectKeyMetaData) {
            return;
          }
          let openKeys = [];
          while(!!selectKeyMetaData && !!selectKeyMetaData.parent) {
            openKeys.push(selectKeyMetaData.parent.path);
            selectKeyMetaData = selectKeyMetaData.parent;
          }
          // routes.forEach((item) => {
          //   openKeys.push(item.path);
          // })
          this.collapsed || this.mode === 'horizontal' ? this.cachedOpenKeys = openKeys : this.openKeys = openKeys;
        }
      },
      render: function(h) {
        return h(
          Menu,
          {
            props: {
              theme: this.$props.theme,
              mode: this.$props.mode,
              openKeys: this.openKeys,
              selectedKeys: this.selectedKeys
            },
            on: {
              openChange: this.onOpenChange,
              select: (obj) => {
                this.selectedKeys = obj.selectedKeys;
                this.$router.push({ path: obj.key });
                this.$emit('select', obj);
              }
            }
          }, this.renderMenu(h, this.menuData)
        )
      }
    }
});

