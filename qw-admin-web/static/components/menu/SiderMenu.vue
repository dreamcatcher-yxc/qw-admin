<template>
  <a-layout-sider 
    width="256px" 
    v-model="collapsed" 
    :class="[theme, 'sider', isMobile ? null : 'shadow']"  
    :style="siderStyle" 
    :collapsible="collapsible" 
    :trigger="null">
    <div :class="['logo', theme]">
      <router-link :to="{ name: 'home' }">
        <!-- <img style="border-radius: 16px;" :src="logoPath"> -->
        <img :src="logoPath">
        <h1>{{systemName}}</h1>
      </router-link>
    </div>
    <div :class="['sider', theme]">
      <i-menu :theme="theme" :collapsed="collapsed" :menu-data="menuData" @select="onSelect"/>
    </div>
  </a-layout-sider>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias-parser',
  'alias!@CMP/menu/Menu'
], function(aliasParser, IMenu, resolve, reject) {
  resolve({
    name: 'SiderMenu',
    components: {
        IMenu : IMenu
    },
    props: {
        collapsible: {
          type: Boolean,
          required: false,
          default: false
        },
        collapsed: {
          type: Boolean,
          required: false,
          default: false
        },
        menuData: {
          type: Array,
          required: true
        },
        theme: {
          type: String,
          required: false,
          default: 'dark'
        }
    },
    data() {
      return {
        logoPath : aliasParser.parse(this.$store.state.setting.logoPath) 
      };
    },
    computed: {
        isMobile () {
            return this.$store.state.setting.isMobile
        },
        systemName () {
            return this.$store.state.setting.systemName
        },
        siderStyle () {
          return {
            zIndex: '110'
          }
        }
    },
    methods: {
        onSelect (obj) {
          this.$emit('menuSelect', obj)
        }
    }
  });
});
</script>

<style scoped>
.shadow {
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
}
.sider {
  z-index: 10;
}
.sider.light {
  background-color: #fff;
}
.sider.dark {
  background-color: #001529;
}
.sider .logo {
  height: 64px;
  position: relative;
  line-height: 64px;
  padding-left: 24px;
  -webkit-transition: all .3s;
  transition: all .3s;
  overflow: hidden;
}
.sider .logo.light {
  background-color: #fff;
}
.sider .logo.light h1 {
  color: #1890ff;
}
.sider .logo.dark {
  background-color: #002140;
}
.sider .logo.dark h1 {
  color: #fff;
}
.sider .logo h1 {
  color: #fff;
  font-size: 20px;
  margin: 0 0 0 12px;
  font-family: "Myriad Pro", "Helvetica Neue", Arial, Helvetica, sans-serif;
  font-weight: 600;
  display: inline-block;
  height: 32px;
  line-height: 32px;
  vertical-align: middle;
}
.sider .logo img {
  width: 32px;
  display: inline-block;
  vertical-align: middle;
}
</style>