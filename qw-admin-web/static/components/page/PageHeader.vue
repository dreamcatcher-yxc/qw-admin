<template>
  <div class="page-header">
    <div :class="['page-header-wide', layout]">
      <!-- 当前页面位置导航 -->
      <div class="breadcrumb">
        <a-breadcrumb>
          <a-breadcrumb-item :key="item.path" v-for="(item, index) in breadcrumb">
            <span v-if="index === 0">
              <a href="javascript:void(0)">{{ item | menuName }}</a>
            </span>
            <span v-else>{{ item | menuName }}</span>
          </a-breadcrumb-item>
        </a-breadcrumb>
      </div>
      <div class="detail">
        <!-- 头像 -->
        <!-- <div class="animate-wrapper" ref="animateWrapper"></div>-->
        <div v-if="avatar" class="avatar">
          <a-avatar :src="avatar"></a-avatar>
        </div>
         
        <div class="main">
          <div class="row">
            <img v-if="logo" :src="logo" class="logo" />
            <h1 v-if="title" class="title">{{title}}</h1>
            <div class="action"><slot name="action"></slot></div>
          </div>
          <div class="row">
            <div v-if="this.$slots.content" class="content"><slot name="content"></slot></div>
            <div v-if="this.$slots.extra" class="extra"><slot name="extra"></slot></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="es6">
  module.exports = new Promise((resolve, reject) => {
    require([
      'lottie',
      'async-watcher',
      'mixins'
    ], function(lottie, watcher, mxs) {
      resolve({
        name: 'PageHeader',
        mixins: [ mxs.CommonMixin ],
        props: {
          title: {
            type: String,
            required: false
          },
          breadcrumb: {
            type: Array,
            required: false
          },
          logo: {
            type: String,
            required: false
          },
          avatar: {
            type: String,
            required: false
          }
        },
        computed: {
          layout () {
            return this.$store.state.setting.layout;
          }
        },
        mounted () {
          // var self = this;
          // watcher(function() {
          //   if(!!self.$refs.animateWrapper) {
          //     lottie.loadAnimation({
          //       container: self.$refs.animateWrapper, // the dom element that will contain the animation
          //       renderer: 'svg',
          //       loop: true,
          //       autoplay: true,
          //       path: `/static/assets/animations/walk-cycle.json` // the path to the animation json
          //     });
          //     return true;
          //   }
          //   return false;
          // });
        }
      });
    });
  });
</script>

<style scoped>
  .page-header {
    background: #fff;
    padding: 16px 32px 0;
    border-bottom: 1px solid #e8e8e8;
  }
  .page-header .page-header-wide.head {
    margin: auto;
    max-width: 1400px;
  }
  .page-header .page-header-wide .breadcrumb {
    margin-bottom: 16px;
  }
  .page-header .page-header-wide .detail {
    display: flex;
  }
  .page-header .page-header-wide .detail .row {
    display: flex;
    width: 100%;
  }
  .page-header .page-header-wide .detail .avatar {
    flex: 0 1 72px;
    margin: 0 24px 8px 0;
  }
  .page-header .page-header-wide .detail .avatar  > span {
    display: block;
    width: 72px;
    height: 72px;
    border: 1px solid #EEE;
    border-radius: 72px;
  }
  .page-header .page-header-wide .detail .main {
    width: 100%;
    flex: 0 1 auto;
  }
  .page-header .page-header-wide .detail .main .title {
    flex: auto;
    font-size: 20px;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.85);
    margin-bottom: 16px;
  }
  .page-header .page-header-wide .detail .main .logo {
    width: 28px;
    height: 28px;
    border-radius: 4px;
    margin-right: 16px;
  }
  .page-header .page-header-wide .detail .main .content {
    margin-bottom: 0px;
    flex: auto;
  }
  .page-header .page-header-wide .detail .main .extra {
    flex: 0 1 auto;
    margin-left: 88px;
    min-width: 500px;
    text-align: right;
  }
  .page-header .page-header-wide .detail .main .action {
    margin-left: 56px;
    min-width: 266px;
    flex: 0 1 auto;
    text-align: right;
  }
  .animate-wrapper {
    width: 100px; 
    height: 100px; 
    /* border: 1px solid red;  */
    border-radius: 50px;
  }
</style>
