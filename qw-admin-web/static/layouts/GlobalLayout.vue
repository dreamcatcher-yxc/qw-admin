<template>
  <a-layout>
    <!-- Mobile菜单 -->
    <template v-if="isMobile">
      <!-- <drawer :open-drawer="collapsed" @change="onDrawerChange">
        <sider-menu :theme="theme" :menu-data="menuData" :collapsed="false" :collapsible="false" @menu-select="onMenuSelect"/>
      </drawer> -->
      <a-drawer
        placement="left"
        :closable="false"
        :visible="collapsed"
        :body-style="mobileSiderMenuStyle"
        @close="onDrawerChange(false)"
      >
        <sider-menu :theme="theme" :menu-data="menuData" :collapsed="false" :collapsible="false" @menu-select="onMenuSelect"/>
      </a-drawer>
    </template>

    <!-- 非 mobile, 侧边菜单 -->
    <template v-else-if="layout === 'side'">
      <sider-menu :theme="theme" :menu-data="menuData" :collapsed="collapsed" :collapsible="true"></sider-menu>
    </template>
    
    <!-- 右侧配置 -->
    <drawer :open-drawer="showSetting" placement="right"  @change="onSettingDrawerChange">
      <template v-slot:handler>
        <div class="setting">
          <a-icon :type="showSetting ? 'close' : 'setting'"></a-icon>
        </div>
      </template>
      <template v-slot:default>
        <setting></setting>
      </template>
    </drawer>

    <!-- 主体内容展示区域 -->
    <a-layout>
      <!-- 顶部菜单 -->
      <div v-show="!headerHided" :class="headerAnimateClasses">
        <global-header :menu-data="menuData" :collapsed.sync="collapsed"></global-header>
      </div>
      <a-layout-content :style="layoutContentStyle">
        <!-- 默认插槽位置 -->
        <slot></slot>
      </a-layout-content>
      <!-- 底部信息展示 -->
      <a-layout-footer style="padding: 0px">
        <global-footer :link-list="linkList" :copyright="copyright"></global-footer>
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/auth/index'
], function(AuthAPIS, resolve, reject) {
  resolve({
    name: 'GlobalLayout',
    components: {
      Setting : load('@CMP/setting/Setting'), 
      SiderMenu : load('@CMP/menu/SiderMenu'), 
      Drawer : load('@CMP/tool/Drawer'), 
      GlobalFooter : load('./GlobalFooter'), 
      GlobalHeader : load('./GlobalHeader')
    },
    data () {
      return {
        collapsed: false,
        menuData: [],
        showSetting: false,
        needHideHeader: false,
        headerHided: false
      }
    },
    computed: {
      screenHeight () {
        return this.$store.state.screen.height;
      },
      mobileSiderMenuStyle () {
        return { 
          padding: 0, 
          height: this.screenHeight + 'px', 
          backgroundColor: this.theme === 'dark' ? '#001529' : '#FFF'
        };
      },
      minHeight () {
        return (this.screenHeight - 64 - 24 - 110) + 'px';
      },
      isMobile () {
        return this.$store.state.setting.isMobile;
      },
      theme () {
        return this.$store.state.setting.theme;
      },
      layout () {
        return this.$store.state.setting.layout;
      },
      linkList () {
        return this.$store.state.setting.footerLinks;
      },
      copyright () {
        return this.$store.state.setting.copyright;
      },
      headerFixed () {
        return this.$store.state.setting.headerFixed;
      },
      layoutContentStyle () {
        let baseStyle = {
          minHeight: this.minHeight, 
          margin: '10px 15px 0'
        };
        if(this.headerFixed) {
          baseStyle.paddingTop = '64px';
        }
        return baseStyle;
      },

      autoHideFixedHeader () {
        let setting = this.$store.state.setting;
        return setting.headerFixed && setting.autoHideFixedHeader;
      },

      headerAnimateClasses () {
        let baseClasses = ['header-animat'];
        if(!this.autoHideFixedHeader) {
          this.headerHided = false;
          return baseClasses;
        }

        if(this.needHideHeader) {
          baseClasses = baseClasses.concat(['showHeader-leave-active', 'showHeader-leave-to']);
          // 需做判断, 否则会重复生成许多不必要的定时任务
          if(!this.headerHided) {
            setTimeout(() => {
              this.headerHided = true;
            }, 250);
          }
        } else {
          baseClasses = baseClasses.concat(['showHeader-enter-active', 'showHeader-enter-to']);
          // 需做判断, 否则会重复生成许多不必要的定时任务
          if(this.headerHided) {
            setTimeout(() => {
              this.headerHided = false;
            }, 250);
          }
        }
        return baseClasses;
      }
    },
    mounted () {
      AuthAPIS.queryMenus()
        .then(newMenuData => {
          this.menuData = [];
          newMenuData.forEach(item => {
            this.menuData.push(item);
          });
          this.$store.commit('share/setMenuData', this.menuData);
        });

      // 监听滚动事件
      window.onscroll = (e) => {
        var e = e || window.event;
        var scrollTop= document.documentElement.scrollTop || document.body.scrollTop;
        if(scrollTop >= 200) {
          this.needHideHeader = true;
        } else {
          this.needHideHeader = false;
        }
      }

      // 当前是手机屏幕尺寸, drawer 默认处于关闭状态, 首次需要在此移除加载层
      if(this.isMobile) {
        this.jQuery('#loading-mask').remove();
      }
    },

    methods: {
      toggleCollapse () {
        this.collapsed = !this.collapsed;
      },
      onDrawerChange (show) {
        this.collapsed = show;
      },
      onMenuSelect () {
        this.toggleCollapse()
      },
      onSettingDrawerChange (val) {
        this.showSetting = val;
      }
    }
  });
});
</script>

<style scoped>
  .setting{
    background-color: #1890ff;
    color: #fff;
    border-radius: 5px 0 0 5px;
    line-height: 40px;
    font-size: 22px;
    width: 40px;
    height: 40px;
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
  }
  .header-animat {
    position: relative;
    z-index: 105
  }
  .showHeader-enter-active {
    -webkit-transition: all .25s ease;
    transition: all .25s ease
  }
  .showHeader-leave-active {
    -webkit-transition: all .5s ease;
    transition: all .5s ease
  }
  .showHeader-leave-to {
    opacity: 0
  }
  .showHeader-enter-to {
    opacity: 1
  }
</style>