<template>
  <div class="result">
    <div >
      <a-icon :class="[isSuccess ? 'success' : 'error' ,'icon']" :type="isSuccess ? 'check-circle' : 'close-circle'" />
    </div>
    <div class="title" v-if="title">{{title}}</div>
    <div class="desc" v-if="description">{{description}}</div>
    <div class="content">
      <slot></slot>
    </div>
    <div class="action">
      <slot name="action"></slot>
    </div>
  </div>
</template>

<script lang="es6">
  module.exports = {
    name: 'Result',
    props: ['isSuccess', 'title', 'description']
  };
  //# sourceURL= components/result/Result.vue
</script>

<style scoped>
  .result {
    text-align: center;
    width: 72%;
    margin: 0 auto;
  }
  .result .icon {
    font-size: 72px;
    line-height: 72px;
    margin-bottom: 24px;
  }
  .result .success {
    color: green;
  }
  .result .error {
    color: red;
  }
  .result .title {
    font-size: 24px;
    color: rgba(0, 0, 0, 0.85);
    font-weight: 500;
    line-height: 32px;
    margin-bottom: 16px;
  }
  .result .desc {
    font-size: 14px;
    line-height: 22px;
    color: rgba(0, 0, 0, 0.45);
    margin-bottom: 24px;
  }
  .result .content {
    background: #fafafa;
    padding: 24px 40px;
    border-radius: 2px;
    text-align: left;
  }
  .result .action {
    margin-top: 32px;
  }
</style>
