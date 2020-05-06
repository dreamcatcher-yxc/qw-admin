<template>
  <a-card class="task-item" type="inner">
    {{content}}
  </a-card>
</template>

<script lang="es6">
  module.exports = {
    name: 'TaskItem',
    props: ['content']
  };
</script>

<style scoped>
  .task-item {
    margin-bottom: 16px;
    box-shadow: 0 1px 1px rgba(27, 31, 35, 0.1);
    border-radius: 6px;
    color: #24292e;
  }
  .task-item:hover {
    cursor: move;
    box-shadow: 0 1px 1px rgba(27, 31, 35, 0.15);
    border-radius: 6px;
  }
</style>
