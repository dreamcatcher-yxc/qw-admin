<template>
  <div class="form-row">
    <div class="label">
      <span>{{label}}</span>
    </div>
    <div class="content">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="es6">
  module.exports = {
    name: 'FormRow',
    props: ['label']
  };
</script>

<style scoped>
  .form-row {
    display: flex;
    border-bottom: 1px dashed #e8e8e8;
    margin-bottom: 16px;
  }
  .form-row .label {
    color: rgba(0, 0, 0, 0.85);
    font-size: 14px;
    margin-right: 24px;
    flex: 0 0 auto;
    text-align: right;
  }
  .form-row .label  > span {
    display: inline-block;
    height: 39px;
    line-height: 39px;
  }
  .form-row .label  > span:after {
    content: '：';
  }
  .form-row .content {
    flex: 1 1 0;
  }
  .form-row .content :global .ant-form-item:last-child {
    margin-right: 0;
  }
  .form-row .content :global .ant-form-item {
    margin-bottom: 0px;
  }
</style>
