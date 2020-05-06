<template>
  <a-spin :spinning="spinning" :delay="delayTime">
    <div class="antd-vue-admin-app">
      <router-view></router-view>
    </div>
  </a-spin>
</template>

<script lang="es6">
  var cmp = new Promise((resolve, reject) => {
      require([
        'alias!@UTIL/device',
        'jquery',
        'config'
      ], function(enquireScreen, jQuery, Config) {
          resolve({
            name: 'App',
            computed: {
              spinning () {
                return this.$store.getters['share/spinning'];
              },
              delayTime () {
                return this.$store.state.share.delayTime;
              }
            },
            mounted () {
              let _this = this;
              enquireScreen(isMobile => {
                _this.$store.commit('setting/setDevice', isMobile);
              });
              //返回true表示为pc端打开，返回false表示为手机端打开
              var isPCBrowser = (function() { 
                var userAgentInfo=navigator.userAgent; 
                var Agents = new Array("Android","iPhone","SymbianOS","Windows Phone","iPad","iPod"); 
                var flag = true;

                for(var v = 0; v < Agents.length; v++) { 
                  if(userAgentInfo.indexOf(Agents[v])>0) { 
                    flag=false; 
                    break; 
                  } 
                } 

                return flag; 
              })();
              _this.$store.commit('setting/setDevice', !isPCBrowser);
              // 监听屏幕大小变化
              let $win = jQuery(window);
              $win.on('resize', () => {
                let width = $win.width();
                let height = $win.height();
                this.$store.commit('screen/resize', { width: width, height: height });
              });
              // 监听全局 session 超时事件, 监听到事件之后直接提示用户已经超时，确定跳转至登录页面
              this.$bus.$on(Config.busEvents.SESSION_TIMEOUT, (e) => {
                this.$warning({
                  title: '提示',
                  content: '登录会话超时，请重新登录!',
                  okText: '确认',
                  onOk: () => {
                    this.$router.push({ name: 'login' });
                  }
                });
              });
              // 无授权事件
              this.$bus.$on(Config.busEvents.NO_AUTHORITY, (resp) => {
                this.$notification.error({
                  message: '无权限',
                  description: resp.message,
                  onClick: () => {
                    //..
                  },
                });
              });
            }
          });
      });
  });

  module.exports = cmp;
</script>

<style>
  /* 拖拽控件全局样式 */
  .dragable-ghost{
    border: 1px dashed #aaaaaa;
    opacity: 0.65;
  }
  .dragable-chose{
    border: 1px dashed #aaaaaa;
    opacity: 0.65;
  }
  .dragable-drag{
    border: 1px dashed #aaaaaa;
    opacity: 0.65;
  }
  /*页面切换动画*/
  .page-toggle-enter-active {
    transition: all 0.2s ease-in 0.25s;
  }
  .page-toggle-leave-active {
    transition: all 0.2s ease-out 0s;
  }
  .page-toggle-enter, .page-toggle-leave-to {
    opacity: 0;
    padding: 0px;
  }
</style>