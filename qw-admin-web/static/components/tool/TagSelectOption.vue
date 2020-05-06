<template>
  <a-checkable-tag @change="$emit('click')" class="tag-default" v-model="checked">
    <slot></slot>
  </a-checkable-tag>
</template>

<script lang="es6">
  module.exports = {
    name: 'TagSelectOption',
    props: {
      size: {
        type: String,
        required: false,
        default: 'default'
      }
    },
    data () {
      return {
        checked: false,
        isTagSelectOption: true
      }
    }
  };
</script>

<style scoped>
  .tag-default {
    font-size: 14px;
    padding: 0 8px;
    height: auto;
    margin-right: 24px;
  }
</style>
