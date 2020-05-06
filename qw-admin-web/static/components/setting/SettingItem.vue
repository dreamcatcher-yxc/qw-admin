<template>
  <div class="setting-item">
    <h3 class="title">{{title}}</h3>
    <slot></slot>
  </div>
</template>

<script lang="es6">
  module.exports = {
      name: 'SettingItem',
      props: ['title']
  };
</script>

<style scoped>
  .setting-item {
    margin-bottom: 24px;
  }
  
  .setting-item .title {
      font-size: 14px;
      color: rgba(0,0,0,.85);
      line-height: 22px;
      margin-bottom: 12px;
  }
</style>