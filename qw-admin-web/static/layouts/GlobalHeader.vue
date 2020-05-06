<template>
  <a-layout-header :class="layoutClasses">
    <div :class="['global-header-wide', layout]">
      <router-link v-if="isMobile || layout === 'head'" to="/" :class="['logo', isMobile ? null : 'pc', theme]">
        <img style="width: 32px;" :src="logoPath" />
        <h1 v-if="!isMobile">{{systemName}}</h1>
      </router-link>

      <a-divider v-if="isMobile" type="vertical"></a-divider>
      <a-icon v-if="layout === 'side'" class="trigger" :type="collapsed ? 'menu-unfold' : 'menu-fold'" @click="toggleCollapse"></a-icon>

      <div v-if="layout === 'head'" class="global-header-menu">
        <i-menu style="height: 64px; line-height: 64px;" :theme="theme" mode="horizontal" :menu-data="menuData" @select="onSelect"/>
      </div>

      <div :class="['global-header-right', theme]">
          <header-search class="header-item"></header-search>
          <a-tooltip class="header-item" title="帮助文档" placement="bottom" >
            <a>
              <a-icon type="question-circle-o"></a-icon>
            </a>
          </a-tooltip>
          <header-notice class="header-item"></header-notice>
          <header-avatar class="header-item"></header-avatar>
      </div>
    </div>
  </a-layout-header>
</template>

<script lang="es6">
  const cmp = new Promise((resolve, reject) => {
    require([
        'alias-parser',
        'alias!@CMP/menu/Menu'
    ], function(aliasParser, IMenu) {
        resolve({
          name: 'GlobalHeader',
          components: {
            IMenu : IMenu, 
            HeaderAvatar : load('./HeaderAvatar'), 
            HeaderNotice : load('./HeaderNotice'), 
            HeaderSearch : load('./HeaderSearch')
          },
          props: ['collapsed', 'menuData'],
          data() {
            return {
              logoPath : aliasParser.parse(this.$store.state.setting.logoPath)
            };
          },
          computed: {
            isMobile () {
              return this.$store.state.setting.isMobile;
            },
            layout () {
              return this.$store.state.setting.layout;
            },
            theme () {
              return this.layout === 'side' ? 'light' : this.$store.state.setting.theme;
            },
            systemName () {
              return this.$store.state.setting.systemName;
            },
            headerFixed () {
              return this.$store.state.setting.headerFixed;
            },
            layoutClasses () {
              let base = [this.theme, 'global-header'];
              
              if(!this.headerFixed) {
                return base;
              }

              if(this.layout === 'head') {
                base.push('ant-header-top-fixed');
                return base;
              }

              base.push('ant-header-fixed');

              if(this.collapsed) {
                base.push('ant-header-side-closed');
              } else {
                base.push('ant-header-side-opened');
              }
              
              return base;
            },
          },
          methods: {
            toggleCollapse () {
              this.$emit('update:collapsed', !this.collapsed);
            },
            onSelect (obj) {
              this.$emit('menuSelect', obj);
            }
          }
        });
    });
  });

  module.exports = cmp;
</script>

<style scoped>
.trigger {
  font-size: 20px;
  line-height: 64px;
  padding: 0 24px;
  cursor: pointer;
  transition: color .3s;
}
.trigger:hover {
  color: #1890ff;
}
.header-item {
  padding: 0 12px;
  display: inline-block;
  height: 100%;
  cursor: pointer;
  vertical-align: middle;
}
.header-item i {
  font-size: 16px;
  color: rgba(0, 0, 0, 0.65);
}
.global-header {
  padding: 0 12px 0 0;
  -webkit-box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: relative;
}
.ant-header-fixed {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - 256px);
  -webkit-transition: width .2s;
  transition: width .2s;
}
.ant-header-top-fixed {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: 100%;
  -webkit-transition: width .2s;
  transition: width .2s;
}
.ant-header-side-opend {
  width: calc(100% - 256px);
}
.ant-header-side-closed {
  width: calc(100% - 80px);
}
.global-header.light {
  background: #fff;
}
.global-header.dark {
  background: #001529;
}
.global-header .global-header-wide.head {
  max-width: 1400px;
  margin: auto;
}
.global-header .global-header-wide .logo {
  height: 64px;
  line-height: 58px;
  vertical-align: top;
  display: inline-block;
  padding: 0 12px 0 24px;
  cursor: pointer;
  font-size: 20px;
}
.global-header .global-header-wide .logo.pc {
  padding: 0 12px 0 0;
}
.global-header .global-header-wide .logo img {
  display: inline-block;
  vertical-align: middle;
}
.global-header .global-header-wide .logo h1 {
  display: inline-block;
  font-size: 16px;
}
.global-header .global-header-wide .logo.dark h1 {
  color: #fff;
}
.global-header .global-header-wide .global-header-menu {
  display: inline-block;
}
.global-header .global-header-wide .global-header-right {
  float: right;
}
.global-header .global-header-wide .global-header-right.dark {
  color: #fff;
}
.global-header .global-header-wide .global-header-right.dark i {
  color: #fff;
}
</style>