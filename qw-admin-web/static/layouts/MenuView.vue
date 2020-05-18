<template>
  <global-layout>
    <context-menu :item-list="menuItemList" :visible.sync="menuVisible" @select="onMenuSelect"></context-menu>
    <div style="margin-left: -10px;">
      <a-tabs
        @contextmenu.native="e => onContextmenu(e)"
        v-if="multipage"
        :active-key="activePage"
        style="margin-top: -8px; margin-bottom: 8px"
        :hide-add="true"
        type="editable-card"
        @change="changePage"
        @edit="editPage">
        <a-tab-pane :id="page.fullPath" :key="page.fullPath" v-for="page in pageList">
          <template slot="tab">
            <a-icon :type="getMenuIcon(page.fullPath, page.meta.icon||'')" style="margin-right: 0; font-size: 16px;"></a-icon>
            <span :pagekey="page.fullPath" v-text="getMenuName(page.fullPath, page)"></span>
          </template>  
        </a-tab-pane>
      </a-tabs>
    </div>
    <transition name="page-toggle">
      <template v-if="multipage">
        <keep-alive>
          <router-view></router-view>
        </keep-alive>
      </template>
      <template v-else>
        <router-view></router-view>
      </template>    
    </transition>
  </global-layout>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'config'
], function(Conf, resolve, reject) {
  resolve({
    name: 'MenuView',
    components: {
      ContextMenu : load('@CMP/menu/ContextMenu'), 
      GlobalLayout : load('./GlobalLayout')
    },
    data () {
      return {
        pageList: [],
        linkList: [],
        activePage: '',
        menuVisible: false,
        menuItemList: [
          { key: '1', icon: 'arrow-left', text: '关闭左侧' },
          { key: '2', icon: 'arrow-right', text: '关闭右侧' },
          { key: '3', icon: 'close', text: '关闭其它' }
        ]
      }
    },
    watch: {
      '$route': function (newRoute, oldRoute) {
        this.activePage = newRoute.fullPath;
        let oldLinkList = this.linkList.map(item => item);

        if (!this.multipage) {
          this.linkList = [newRoute.fullPath];
          this.pageList = [newRoute];
        } else if (this.linkList.indexOf(newRoute.fullPath) < 0) {
          this.linkList.push(newRoute.fullPath);
          this.pageList.push(newRoute);
        }
        
        var currentLinkList = this.linkList.map(path => {
          return {
            path: path,
            isNew: oldLinkList.indexOf(path) < 0
          };
        });
        this.$store.commit('share/setCurrentLinkList', currentLinkList);
      },
      'activePage': function (key) {
        this.$router.push(key);
      },
      'multipage': function (newVal, oldVal) {
        if (!newVal) {
          this.linkList = [this.$route.fullPath];
          this.pageList = [this.$route];
        }
      }
    },
    computed: {
      multipage () {
        return this.$store.state.setting.multipage;
      }
    },
    created () {
      this.pageList.push(this.$route);
      this.linkList.push(this.$route.fullPath);
      this.activePage = this.$route.fullPath;
    },
    mounted () {
      this.init()
    },
    methods: {
      init() {
        this.$bus.$off(Conf.CLOSE_PAGE);
        this.$bus.$on(Conf.busEvents.CLOSE_PAGE, (closePageKeys) => {
          if(Array.isArray(closePageKeys)) {
            closePageKeys.forEach(key => this.remove(key));
          }
        });
      },

      getMenuName (key, routeInfo) {
        let menuData = this.$store.getters['share/getMenuNameByMenuKey'](key);
        if(!!menuData) {
          return menuData.name;
        } 
        if(routeInfo.meta.menuName) {
          return routeInfo.meta.menuName;
        }
        return routeInfo.name;
      },

      getMenuIcon (key, defaultIcon) {
        let menuData = this.$store.getters['share/getMenuNameByMenuKey'](key);
        if(!!menuData) {
          let icon = null;
          do {
            icon = menuData.icon;
            menuData = menuData.parent;
          } while(!icon && !!menuData);
          return icon;
        }
        return defaultIcon;
      },

      changePage (key) {
        this.activePage = key;
      },

      editPage (key, action) {
        this[action](key);
      },

      remove (key) {
        if (this.pageList.length === 1) {
          this.$message.warning('这是最后一页，不能再关闭了啦');
          return;
        }
        this.pageList = this.pageList.filter(item => item.fullPath !== key);
        let index = this.linkList.indexOf(key);
        this.linkList = this.linkList.filter(item => item !== key);
        index = index >= this.linkList.length ? this.linkList.length - 1 : index;
        this.activePage = this.linkList[index];
      },

      onContextmenu (e) {
        let $ele = this.jQuery(e.target);
        // 选择了某一个 tab
        if($ele.hasClass('ant-tabs-tab') || $ele.parents('div.ant-tabs-tab:first').length > 0) {
          // 有 pagekey
          const pagekey = $ele.attr('pagekey') || $ele.find('span[pagekey]').attr('pagekey');
          if (pagekey !== null) {
            e.preventDefault();
            this.menuVisible = true;
          }
        }
      },

      onMenuSelect (key, target) {
        let $ele = this.jQuery(target);
        let pageKey = $ele.attr('pagekey') || $ele.find('span[pagekey]').attr('pagekey');
        switch (key) {
          case '1':
            this.closeLeft(pageKey);
            break;
          case '2':
            this.closeRight(pageKey);
            break;
          case '3':
            this.closeOthers(pageKey);
            break
          default:
            break;
        }
      },

      closeOthers (pageKey) {
        let index = this.linkList.indexOf(pageKey);
        this.linkList = this.linkList.slice(index, index + 1);
        this.pageList = this.pageList.slice(index, index + 1);
        this.activePage = this.linkList[0];
      },

      closeLeft (pageKey) {
        let index = this.linkList.indexOf(pageKey);
        this.linkList = this.linkList.slice(index);
        this.pageList = this.pageList.slice(index);
        if (this.linkList.indexOf(this.activePage) < 0) {
          this.activePage = this.linkList[0];
        }
      },

      closeRight (pageKey) {
        let index = this.linkList.indexOf(pageKey);
        this.linkList = this.linkList.slice(0, index + 1);
        this.pageList = this.pageList.slice(0, index + 1);
        if (this.linkList.indexOf(this.activePage < 0)) {
          this.activePage = this.linkList[this.linkList.length - 1];
        }
      }
    }
  });
});
</script>

<style scoped>
</style>