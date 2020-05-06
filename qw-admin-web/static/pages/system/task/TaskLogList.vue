<template>
  <a-card>
    <div>
      <div>
        <a-form layout="horizontal" :form="form" @submit="onSearch">
          <div class="fold" v-if="$$hasAuth('work-log-list')">
            <a-row>
              <a-col :md="8" :sm="24" >
                <a-form-item
                  label="开始日期"
                  :label-col="{span: 5}"
                  :wrapper-col="{span: 18, offset: 1}"
                >
                  <a-date-picker v-decorator="['startDate']" style="width: 100%"></a-date-picker>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24" >
                <a-form-item
                  label="结束日期"
                  :label-col="{span: 5}"
                  :wrapper-col="{span: 18, offset: 1}"
                >
                  <a-date-picker v-decorator="['endDate']" style="width: 100%"></a-date-picker>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24" >
                <a-form-item
                  label="任务名称"
                  :label-col="{span: 5}"
                  :wrapper-col="{span: 18, offset: 1}"
                >
                  <a-select
                    v-decorator="[
                      'jobClass'
                    ]"
                  >
                    <a-select-option 
                      v-for="job,index in jobs"
                      :key="index" 
                      :value="job.key">
                      {{ job.value }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
          </div>
          <span style="float: right; margin-top: 3px;">
            <a-button type="primary" html-type="submit">查询</a-button>
            <a-button style="margin-left: 8px" @click="(e) => this.form.resetFields()">重置</a-button>
          </span>
        </a-form>
      </div>
      <div class="operator">
        <a-button v-if="$$hasAuth('work-log-delete')" @click="deleteTaskLog" type="danger">删除</a-button>
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
        row-key="no"
        @select-row-change="onSelectRowChange"
        @change="onChange"
      >
        <template v-slot:status="props">
          <a-tag :color="props.text === 1 ? 'green' : 'red'">{{ props.text === 1 ? '成功': '失败' }}</a-tag>
        </template>
      </qw-table>

      <template v-if="isEdit">
        <task-log-detail :show.sync="isEdit" :data="editData" @on-close="(data) => this.init(false)"></task-log-detail>
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
    dataIndex: 'name',
    customRender: (text, record) => {
      let items = record.attach.jobs.filter(job => job.key === text.trim());
      return !!items && items.length > 0 ? items[0].value : text;
    }
  },
  {
    title: '任务所在组',
    dataIndex: 'groupName',
    customRender: (text, record) => record.attach.taskGroups.filter(job => job.key === text)[0].value
  },
  {
    title: '执行结果',
    dataIndex: 'status',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '创建时间',
    dataIndex: 'createDate'
  },
];

module.exports = asyncRequire([
  'alias!@API/system/task',
  'mixins',
  'moment'
], function(TaskAPIS, MXS, Moment, resolve, reject) {
  resolve({
    name: 'TaskLogList',
    mixins: [MXS.AuthMixin, MXS.CommonMixin],
    components: {
      QwTable: load('@CMP/table/QWTable'),
      QwColumnSelector: load('@CMP/table/QWTableColumnSelector'),
      TaskLogDetail: load('./TaskLogDetail')
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
          showQuickJumper: true,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '30', '40'],
          onShowSizeChange: (current, size) => {
            this.pagination.current = current;
            this.pagination.pageSize = size;
            this.init();
          }
        },
        form: this.$form.createForm(this, { name: 'search_form' }),
        jobs: [],
        taskGroups: [],
        isEdit: false
      }
    },
    mounted () {
      // this.$$hasAuth('work-log-detail');
      this.$$fetchAuth();
      // 初始化必要的数据字典信息
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
        this.init(true);
      },
      init(reset) {
        this.showLoading();
        if(reset) {
          this.form.resetFields();
          this.pagination.current = 1;
        };
        var searchForm = {
          startDate: this.form.getFieldValue('startDate'),
          endDate: this.form.getFieldValue('endDate'),
          jobClass: this.form.getFieldValue('jobClass'),
        };
        if(searchForm.startDate) {
          searchForm.startDate = Moment(searchForm.startDate._d).format('YYYY-MM-DD');
        }
        if(searchForm.endDate) {
          searchForm.endDate = Moment(searchForm.endDate._d).format('YYYY-MM-DD');
        } 
        return TaskAPIS.queryTaskLogs(searchForm, this.pagination.current, this.pagination.pageSize)
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
      onSearch (e) {
        e.preventDefault();
        this.pagination.current = 1;
        this.init();
      },
      onSelectRowChange (skeys, selectedRows) {
        this.selectedRows = selectedRows;
      },
      onChange(pagination, filter, sorter) {
        if(pagination.current === this.pagination.current) {
          return;
        }
        this.pagination.current = pagination.current;
        this.init(false);
      },
      editTaskLog (record) {
        // if(!this.$$hasAuth('work-log-detail')) {
        //   return;
        // }
        this.editData = record;
        this.isEdit = true;
      },
      deleteTaskLog () {
        this.$confirm({
          title: '删除确认',
          content: `确定删除选中的【${this.selectedRows.length}】条记录吗?`,
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            this.showLoading();
            return TaskAPIS.deleteTaskLogs(this.selectedRows.map(item => item.workLogId))
              .then((data) => {
                this.$message.success('删除成功');
                this.selectedRows = [];
                this.init(true);
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
              this.editTaskLog(record);
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
