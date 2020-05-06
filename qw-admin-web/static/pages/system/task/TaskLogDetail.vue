<template>
  <div>
    <a-modal 
      title="详情" 
      :visible="isVisible"
      :width="modalWidth"
      :body-style="modalBodyStyle"
      @cancel="handleCancel">
      <template slot="footer">
        <a-button 
          type="primary" 
          @click="handleOk">
          关闭
        </a-button>
      </template>

      <a-descriptions :column="6" bordered >
        <a-descriptions-item :span="2" label="名称">{{ this.data.name | humanizedName }}</a-descriptions-item>
        <a-descriptions-item :span="2" label="所属组">{{ this.data.groupName | humanizedGroup }}</a-descriptions-item>
        <a-descriptions-item :span="2" label="执行方法">{{ this.data.jobClass }}</a-descriptions-item>
        <a-descriptions-item :span="2" label="开始时间">{{ this.data.startDate }}</a-descriptions-item>
        <a-descriptions-item :span="2" label="结束时间">{{ this.data.endDate }}</a-descriptions-item>
        <a-descriptions-item :span="2" label="创建时间">{{ this.data.createDate }}</a-descriptions-item>
        <a-descriptions-item :span="6" label="描述">{{ this.data.description }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/system/task',
  'mixins'
], function(TaskAPIS, MXS, resolve, reject) {
  // 初始化必要的表单信息
  Promise.all([
    TaskAPIS.queryJobs(false),
    TaskAPIS.queryTaskGroups()
  ]).then((datas) => {
    let JOBS = datas[0].sdata.jobs;
    let TASK_GROUPS = datas[1].sdata.taskGroups;

    resolve({
      name: 'TaskLogDetail',
      mixins: [MXS.CommonMixin, MXS.ModalMixin, MXS.ValidatorMixin],
      data () {
        return {
          modalWidth: 1000,
          modalBodyStyle: {
            width: '980px',
            border: 'none'
          },
          jobs: JOBS,
          taskGroups: TASK_GROUPS
        }
      },
      filters: {
        humanizedName (val) {
          return JOBS.filter(item => item.key === val)[0].value;
        },
        humanizedGroup (val) {
          return TASK_GROUPS.filter(item => item.key === val)[0].value;
        }
      },
      methods: {
        handleCancel () {
          this.close();
        },
        handleOk () {
          this.close();
        }
      }
    });
  });
});
</script>

<style scoped>
</style>
