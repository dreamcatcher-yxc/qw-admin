<template>
  <a-card>
    <div>
      <div class="operator">
        <a-button v-if="$$hasAuth('job-add')" @click="addTask" type="primary">新建</a-button>
        <a-button v-if="$$hasAuth('job-trigger') && canTrigger" @click="taskOp('trigger')" type="primary">触发</a-button>
        <a-button v-if="$$hasAuth('job-pause') && canPause" @click="taskOp('pause')" type="primary">暂停</a-button>
        <a-button v-if="$$hasAuth('job-resume') && canResume" @click="taskOp('resume')" type="primary">恢复</a-button>
        <a-button v-if="$$hasAuth('job-delete')" @click="deleteTask" type="danger">删除</a-button>
        <span style="float: right; margin-top: 3px;">
          <a-button @click="init()">刷新</a-button>
          <qw-column-selector 
            :src-columns="columns"
            @change="(selectedColumns) => this.replaceArrayByArray('selectedColumns', selectedColumns)">
          </qw-column-selector>
        </span>
      </div>
      <qw-table
        :columns="selectedColumns"
        :data-source="dataSource"
        :selected-rows="selectedRows"
        :pagination="pagination"
        :loading="isLoading"
        :custom-row="customRow"
        row-selection-type="radio"
        row-key="no"
        @select-row-change="onSelectRowChange"
        @change="onChange"
      >
        <template v-slot:state="props">
          <a-tag :color="props.text|humanizedColor">{{ props.text | humanized }}</a-tag>
        </template>
      </qw-table>

      <template v-if="isAdd">
        <task-add :show.sync="isAdd" @on-close="(data) => this.init(true)"></task-add>
      </template>
      <template v-if="isEdit">
        <task-edit :show.sync="isEdit" :data="editData" @on-close="(data) => this.init(false)"></task-edit>
      </template>
    </div>
  </a-card>
</template>

<script lang="es6">
const columns = [
  {
    title: '编号',
    dataIndex: 'no'
  },
  {
    title: '任务名称',
    dataIndex: 'JOB_NAME',
    customRender: (text, record) => {
      let items = record.attach.jobs.filter(job => job.key === text.trim());
      return !!items && items.length > 0 ? items[0].value : text;
    }
  },
  {
    title: '状态',
    dataIndex: 'TRIGGER_STATE',
    scopedSlots: { customRender: 'state' }
  },
  {
    title: '任务所在组',
    dataIndex: 'JOB_GROUP',
    customRender: (text, record) => record.attach.taskGroups.filter(job => job.key === text)[0].value
  },
  {
    title: '任务类名',
    dataIndex: 'JOB_CLASS_NAME'
  },
  {
    title: '触发器名称',
    dataIndex: 'TRIGGER_NAME',
    show: false
  },
  {
    title: '触发器所在组',
    dataIndex: 'TRIGGER_GROUP'
  },
  {
    title: '表达式',
    dataIndex: 'CRON_EXPRESSION'
  },
  {
    title: '时区',
    dataIndex: 'TIME_ZONE_ID'
  }
];

module.exports = asyncRequire([
  'alias!@API/system/task',
  'mixins'
], function(TaskAPIS, MXS, resolve, reject) {
  resolve({
    name: 'TaskList',
    mixins: [MXS.AuthMixin, MXS.CommonMixin],
    components: {
      QwTable: load('@CMP/table/QWTable'),
      QwColumnSelector: load('@CMP/table/QWTableColumnSelector'),
      TaskAdd: load('./TaskAdd'),
      TaskEdit: load('./TaskEdit')
    },
    data () {
      return {
        columns: columns,
        selectedColumns: [],
        dataSource: [],
        selectedRows: [],
        pagination: { 
          current: 1,
          pageSize: 10,
          total: 0,
          pageSizeOptions: ['10', '20', '30', '40'],
          showQuickJumper: true
        },
        isAdd: false,
        isEdit: false,
        editData: {},
        canTrigger: false,
        canPause: false,
        canResume: false,
        jobs: [],
        taskGroups: []
      }
    },

    filters: {
      humanized (val) {
        return val === 'WAITING' ? '等待':
               val === 'PAUSED' ? '暂停':
               val === 'ACQUIRED' ? '正常执行':
               val === 'BLOCKED' ? '阻塞':
               val === 'ERROR' ? '错误' : val;
      },
      humanizedColor (val) {
        return val === 'WAITING' ? 'green':
               val === 'PAUSED' ? '#999999':
               val === 'ACQUIRED' ? '#33CC99':
               val === 'BLOCKED' ? '#FF6600':
               val === 'ERROR' ? '#CC0000' : '#000000';
      }
    },

    mounted () {
      this.$$hasAuth('job-reschedule');
      this.$$fetchAuth();
      // 初始化必要的表单信息
      Promise.all([
        TaskAPIS.queryJobs(false),
        TaskAPIS.queryTaskGroups()
      ]).then((datas) => {
        let jobs = datas[0].sdata.jobs;
        let taskGroups = datas[1].sdata.taskGroups;
        jobs.forEach(job => this.jobs.push(job));
        taskGroups.forEach(task => this.taskGroups.push(task));
        this.init(true);
        this.__vm_inited__ = true;
      });
    },
    methods: {
      reopen() {
        // this.$message.info('该标签被重新打开');
        this.init(true);
      },
      init(reset) {
        this.showLoading();
        if(reset) {
          this.pagination.current = 1;
        }
        return TaskAPIS.queryTasks(this.pagination.current)
          .then((data) => {
            var jobs = data.sdata;
            this.dataSource = [];
            jobs.forEach((job, i) => {
              job.no = (this.pagination.current - 1) * 10 + i + 1;
              job.attach = {
                jobs: this.jobs,
                taskGroups: this.taskGroups
              };
              this.dataSource.push(job);
            });
            this.pagination.total = data.scount;
            this.hideLoading();
            return data;
          });
      },

      updateSelectRowOperationState (record) {
        if(!!record) {
          if(record.TRIGGER_STATE === 'ACQUIRED' || record.TRIGGER_STATE === 'WAITING') {
            this.canTrigger = true;
            this.canPause = true;
            this.canResume = false;
          } else {
            this.canTrigger = false;
            this.canPause = false;
            this.canResume =  true;
          }
        } else {
          this.canTrigger = false;
          this.canPause = false;
          this.canResume = false;
        }
      },

      onSelectRowChange (skeys, selectedRows) {
        this.selectedRows = selectedRows;
        if(this.selectedRows.length !== 1) {
          this.updateSelectRowOperationState();
        } else {
          this.updateSelectRowOperationState(this.selectedRows[0]);
        }
      },

      onChange(pagination, filter, sorter) {
        if(pagination.current === this.pagination.current) {
          return;
        }
        this.pagination.current = pagination.current;
        this.init(false);
      },

      addTask () {
        this.isAdd = true;
      },

      editTask (record) {
        if(!this.$$hasAuth('job-reschedule')) {
          return;
        }
        this.isEdit = true;
        this.editData = {
          jobClassName: record.JOB_CLASS_NAME,
          jobGroupName: record.JOB_GROUP,
          cronExpression: record.CRON_EXPRESSION
        };
      },
      
      taskOp (action) {
        if(this.selectedRows.length <= 0) {
          this.$message.warning('有且仅能选择一行');
          return;
        }
        let record = this.selectedRows[0];
        this.editData = {
          jobClassName: record.JOB_CLASS_NAME,
          jobGroupName: record.JOB_GROUP,
          cronExpression: record.CRON_EXPRESSION
        };

        let message;
        let apiName;

        switch (action) {
          case 'trigger':
            message = '确定立即执行该任务吗?'
            apiName = 'triggerTask';
            break;
          case 'pause':
            message = '确定暂停执行该任务吗?'
            apiName = 'pauseTask';
            break;
          case 'resume':
            message = '确定恢复执行该任务吗?'
            apiName = 'resumeTask';
            break;
          default:
            return;
        };

        this.$confirm({
          title: '操作确认',
          content: message,
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            this.showLoading();
            let form = {
              jobClassName: this.selectedRows[0].JOB_CLASS_NAME,
              jobGroupName: this.selectedRows[0].JOB_GROUP,
            };
            return TaskAPIS[apiName](form)
              .then((data) => {
                this.$message.success('操作成功');
                this.init()
                  .then((data) => {
                    this.selectedRows = data.sdata.filter(item => item.no === this.selectedRows[0]['no']);
                    this.updateSelectRowOperationState(this.selectedRows[0]);
                  });
              })
              .catch((data) => {
                this.$message.error(data.message);
              })
              .finally(() => this.hideLoading());
          },
          onCancel() {}
        });
      },

      menuPrivilegeManage () {
        this.isMenuPrivilegeManage = true;
      },

      deleteTask () {
        if(this.selectedRows.length !== 1) {
          this.$message.warning('有且仅能选择一行');
          return;
        }
        this.$confirm({
          title: '删除确认',
          content: `确定删除选中的记录吗?`,
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            this.showLoading();
            var form = {
              jobClassName: this.selectedRows[0].JOB_CLASS_NAME,
              jobGroupName: this.selectedRows[0].JOB_GROUP,
            };
            return TaskAPIS.deleteTask(form)
              .then((data) => {
                this.$message.success('删除成功');
                this.selectedRows = [];
                this.init();
              })
              .catch((data) => {
                this.$message.error(data.message);
              })
              .finally(() => this.hideLoading());
          },
          onCancel() {}
        });
      },

      customRow (record, index) {
        return {
          on: {
            click: () => {
              this.editTask(record);
            }
          }
        }
      }
    }
  });
});
</script>

<style scoped>
  .search{
    margin-bottom: 54px;
  }
  .fold{
    width: calc(100% - 216px);
    display: inline-block
  }
  .operator{
    margin-bottom: 18px;
  }
  @media screen and (max-width: 900px) {
    .fold {
      width: 100%;
    }
  }
</style>
