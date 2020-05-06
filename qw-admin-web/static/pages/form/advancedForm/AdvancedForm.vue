<template>
  <div>
    <a-card class="card" title="仓库管理" :bordered="false">
      <repository-form ref="repository" :show-submit="false"></repository-form>
    </a-card>
    <a-card class="card" title="任务管理" :bordered="false">
      <task-form ref="task" :show-submit="false"></task-form>
    </a-card>
    <a-card title="用户管理" :bordered="false">
      <table-form></table-form>
    </a-card>
    <footer-tool-bar>
      <a-button type="primary" @click="validate" :loading="loading">提交</a-button>
    </footer-tool-bar>
  </div>
</template>

<script lang="es6">
module.exports = {
  name: 'AdvancedForm',
  components: {
    RepositoryForm: load('./RepositoryForm'),
    TableForm: load('./TableForm'), 
    TaskForm: load('./TaskForm'), 
    FooterToolBar: load('@CMP/tool/FooterToolBar')
  },
  data () {
    return {
      desc: '高级表单常见于一次性输入和提交大批量数据的场景。',
      loading: false
    }
  },
  methods: {
    validate () {
      this.$refs.repository.form.validateFields((err, values) => {
        if (!err) {
          console.log('Received values of form: ', values)
        }
      })
      this.$refs.task.form.validateFields((err, values) => {
        if (!err) {
          console.log('Received values of form: ', values)
        }
      })
    }
  }
}
</script>

<style scoped>
  .card{
    margin-bottom: 24px;
  }
</style>
