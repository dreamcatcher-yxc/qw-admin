<template>
  <div style="display: flex">
    <task-group class="task-group" title="ToDo" group="task">
      <task-item :key="index" v-for="(item, index) in todoList" :content="item"></task-item>
    </task-group>
    <task-group class="task-group" title="In Progress" group="task">
      <task-item :key="index" v-for="(item, index) in inproList" :content="item"></task-item>
    </task-group>
    <task-group class="task-group" title="Done" group="task">
      <task-item :key="index" v-for="(item, index) in doneList" :content="item"></task-item>
    </task-group>
  </div>
</template>

<script lang="es6">
let todoList = ['任务一', '任务二', '任务三', '任务四', '任务五', '任务六'];
let inproList = ['任务七', '任务八', '任务九', '任务十', '任务十一', '任务十二'];
let doneList = ['任务十三', '任务十四', '任务十五', '任务十六', '任务十七', '任务十八'];
module.exports = {
  name: 'TaskCard',
  components: {
    TaskItem: load('@CMP/task/TaskItem'), 
    TaskGroup: load('@CMP/task/TaskGroup')
  },
  data () {
    return {
      todoList,
      inproList,
      doneList
    }
  }
};
</script>

<style scoped>
  .task-group{
    margin: 0 48px;
  }
</style>
