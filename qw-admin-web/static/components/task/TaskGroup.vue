<template>
  <div class="task-group">
    <div class="task-head">
      <h3 class="title"><span v-if="count">{{count}}</span>{{title}}</h3>
      <div class="actions" style="float: right">
        <a-icon class="add" type="plus" draggable="true"></a-icon>
        <a-icon class="more" style="margin-left: 8px" type="ellipsis"></a-icon>
      </div>
    </div>
    <div class="task-content">
      <draggable :options="dragOptions">
        <slot></slot>
      </draggable>
    </div>
  </div>
</template>

<script lang="es6">
  const cmp = new Promise((resolve, reject) => {
      require([
        'vuedraggable'
      ], function(Draggable) {
        const dragOptions = {
          sort: true,
          scroll: true,
          scrollSpeed: 2,
          animation: 150,
          ghostClass: 'dragable-ghost',
          chosenClass: 'dragable-chose',
          dragClass: 'dragable-drag'
        };

        resolve({
          name: 'TaskGroup',
          components: {
            Draggable: Draggable
          },
          props: ['title', 'group'],
          data () {
            var newDragOptions = {};
            this.jQuery.extend(true, newDragOptions, dragOptions, { group: this.group });
            // console.log(newDragOptions);
            return {
              dragOptions: newDragOptions
            }
          },
          computed: {
            count () {
              return this.$slots.default.length;
            }
          }
        });
    });
  });

  module.exports = cmp;
  //# sourceURL= components/task/TaskGroup.vue
</script>

<style>
  .task-group {
    width: 33.33%;
    padding: 8px 8px;
    background-color: #e1e4e8;
    border-radius: 6px;
    border: 1px solid #d1d4d8;
  }
  .task-group .task-head {
    margin-bottom: 8px;
  }
  .task-group .task-head .title {
    display: inline-block;
  }
  .task-group .task-head .title span {
    display: inline-block;
    border-radius: 10px;
    margin: 0 8px;
    font-size: 12px;
    padding: 2px 6px;
    background-color: rgba(27, 31, 35, 0.15);
  }
  .task-group .task-head .actions {
    display: inline-block;
    float: right;
    font-size: 18px;
    font-weight: bold;
  }
  .task-group .task-head .actions i {
    cursor: pointer;
  }
</style>
