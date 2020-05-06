<template>
  <div class="style">
    <img :src="img" />
    <div v-if="selected" class="select-item">
      <a-icon type="check" />
    </div>
  </div>
</template>

<script lang="es6">
  module.exports = {
      name: 'StyleItem',
      props: ['selected', 'img']
  };
</script>

<style scoped>
  .style{
    margin-right: 16px;
    position: relative;
    border-radius: 4px;
    cursor: pointer;
  }

  .style .select-item {
      position: absolute;
      top: 0;
      right: 0;
      width: 100%;
      padding-top: 15px;
      padding-left: 24px;
      height: 100%;
      color: #1890ff;
      font-size: 14px;
      font-weight: bold;
  }
</style>