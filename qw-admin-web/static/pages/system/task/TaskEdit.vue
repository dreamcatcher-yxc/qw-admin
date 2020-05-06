<template>
  <div>
    <a-modal 
      title="编辑任务" 
      :visible="isVisible"
      ok-text="修改"
      cancel-text="取消"
      :ok-button-props="{
        props: {
          loading: isLoading
        }
      }"
      @ok="handleOk"
      @cancel="handleCancel">
      
       <a-form :form="form" @submit="handleSubmit">
        <a-form-item v-bind="formItemLayout" label="任务名">
          <a-select
            v-decorator="[
              'jobClassName',
              {
                rules: [
                  {
                    required: true,
                    message: '任务名不能为空!',
                  }
                ]
              }
            ]"
          >
            <a-select-option v-for="item,index in jobs" :key="index" :value="item.key">
              {{ item.value }}
            </a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item v-bind="formItemLayout" label="任务分组">
          <a-select
            v-decorator="[
              'jobGroupName',
              {
                rules: [
                  {
                    required: true,
                    message: '任务分组不能为空!',
                  }
                ]
              }
            ]"
          >
            <a-select-option v-for="item,index in taskGroups" :key="index" :value="item.key">
              {{ item.value }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item v-bind="formItemLayout" label="计划(CRON)">
          <a-input 
            v-decorator="[
              'cronExpression', 
              { 
                rules: [
                  {
                    required: true,
                    message: '执行CRON表达式必填!',
                  }
                ]
              }
            ]" 
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/system/task',
  'mixins'
], function(TaskAPIS, MXS, resolve, reject) {
  resolve({
    name: 'TaskEdit',
    mixins: [MXS.CommonMixin, MXS.ModalMixin, MXS.ValidatorMixin],
    data () {
      return {
        formItemLayout: {
          labelCol: {
            xs: { span: 24 },
            sm: { span: 4 },
          },
          wrapperCol: {
            xs: { span: 24 },
            sm: { span: 20 },
          }
        },
        jobs: [],
        taskGroups: []
      }
    },
    created () {
      this.form = this.$form.createForm(this, {
        name: 'task_edit_form',
        mapPropsToFields: () => {
          return {
            jobClassName: this.$form.createFormField({
              value: this.data.jobClassName,
            }),
            jobGroupName: this.$form.createFormField({
              value: this.data.jobGroupName,
            }),
            cronExpression: this.$form.createFormField({
              value: this.data.cronExpression,
            })
          };
        }
      });
    },
    mounted () {
      // 初始化必要的表单信息
      Promise.all([
        TaskAPIS.queryJobs(false),
        TaskAPIS.queryTaskGroups()
      ]).then((datas) => {
        let jobs = datas[0].sdata.jobs;
        let taskGroups = datas[1].sdata.taskGroups;
        jobs.forEach(job => this.jobs.push(job));
        taskGroups.forEach(task => this.taskGroups.push(task));
      });
    },
    methods: {
      handleSubmit (e) {
        e.preventDefault()
      },
      handleCancel () {
        this.close();
      },
      handleOk () {
        this.form.validateFields((err, values) => {
          if (!err) {
            this.showLoading();
            TaskAPIS.editTask(values)
                .then(data => {
                   this.close({ $hook: true });
                }).catch(data => {
                   this.$message.error(data.message);
                }).finally(() => this.hideLoading());
          }
        });
      }
    }
  });
});
</script>

<style scoped>
  
</style>
