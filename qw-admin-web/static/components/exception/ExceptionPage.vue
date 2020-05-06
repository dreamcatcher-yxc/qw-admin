<template>
  <div class="exception">
    <div class="img">
      <img :src="config[type].img" />
      <!--<div class="ele" :style="{backgroundImage: `url(${config[type].img})`}"/>-->
    </div>
    <div class="content">
      <h1>{{config[type].title}}</h1>
      <div class="desc">{{config[type].desc}}</div>
      <div class="action">
        <a-button type="primary" @click="backHome">返回首页</a-button>
      </div>
    </div>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@CMP/exception/typeConfig',
  'alias!@UTIL/sysUtil',
], function(Config, SysUtil, resolve, reject) {
  resolve({
    name: 'ExceptionPage',
    props: ['type'],
    data () {
      return {
        config: Config
      }
    },
    methods: {
      backHome () {
        this.$router.push({ name: 'home' });
        // 关闭当前页
        SysUtil.closePages([this.$route.path]);
      }
    }
  });
});
</script>

<style scoped>
  .exception {
    min-height: 500px;
    height: 80%;
    align-items: center;
    text-align: center;
    margin-top: 150px;
  }
  .exception .img {
    display: inline-block;
    padding-right: 52px;
    zoom: 1;
  }
  .exception .img img {
    height: 360px;
    max-width: 430px;
  }
  .exception .content {
    display: inline-block;
    flex: auto;
  }
  .exception .content h1 {
    color: #434e59;
    font-size: 72px;
    font-weight: 600;
    line-height: 72px;
    margin-bottom: 24px;
  }
  .exception .content .desc {
    color: rgba(0, 0, 0, 0.45);
    font-size: 20px;
    line-height: 28px;
    margin-bottom: 16px;
  }
</style>
