<template>
  <a-card>
    <div>
      <div>
        <a-form layout="horizontal" :form="form" @submit="onSearch">
          <div class="fold">
            <div class="fold-left">
              <a-row>
                <a-col :md="8" :sm="24" >
                  <a-form-item
                    label="操作者"
                    :label-col="{span: 5}"
                    :wrapper-col="{span: 18, offset: 1}"
                  >
                    <a-input v-decorator="['operatorName']" style="width: 100%"></a-input>
                  </a-form-item>
                </a-col>

                <a-col :md="8" :sm="24" >
                  <a-form-item
                    label="操作描述"
                    :label-col="{span: 5}"
                    :wrapper-col="{span: 18, offset: 1}"
                  >
                    <a-input v-decorator="['operation']" style="width: 100%"></a-input>
                  </a-form-item>
                </a-col>

                <!-- <a-col :md="8" :sm="24" >
                  <a-form-item
                    label="方法"
                    :label-col="{span: 5}"
                    :wrapper-col="{span: 18, offset: 1}"
                  >
                    <a-input v-decorator="['operateMethod']" style="width: 100%"></a-input>
                  </a-form-item>
                </a-col> -->
                <a-col :md="8" :sm="24" >
                  <a-form-item
                    label="结果"
                    :label-col="{span: 5}"
                    :wrapper-col="{span: 18, offset: 1}"
                  >
                    <a-select v-decorator="['result']">
                      <a-select-option value="">所有</a-select-option>
                      <a-select-option value="S">成功</a-select-option>
                      <a-select-option value="F">失败</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row>
                <a-col :md="8" :sm="24" >
                  <a-form-item
                    label="客户端地址"
                    :label-col="{span: 5}"
                    :wrapper-col="{span: 18, offset: 1}"
                  >
                    <a-input v-decorator="['ip']" style="width: 100%"></a-input>
                  </a-form-item>
                </a-col>
                <a-col :md="8" :sm="24" >
                  <a-form-item
                    label="开始日期"
                    :label-col="{span: 5}"
                    :wrapper-col="{span: 18, offset: 1}"
                  >
                    <a-date-picker v-decorator="['start']" style="width: 100%"></a-date-picker>
                  </a-form-item>
                </a-col>
                <a-col :md="8" :sm="24" >
                  <a-form-item
                    label="结束日期"
                    :label-col="{span: 5}"
                    :wrapper-col="{span: 18, offset: 1}"
                  >
                    <a-date-picker v-decorator="['end']" style="width: 100%"></a-date-picker>
                  </a-form-item>
                </a-col>
              </a-row>
            </div>
            <div class="fold-right">
              <a-button type="primary" html-type="submit">查询</a-button>
              <a-button style="margin-left: 8px" @click="(e) => this.form.resetFields()">重置</a-button>
            </div>
          </div>  
        </a-form>
      </div>
      <div class="operator">
        <a-button v-if="$$hasAuth('operate-log-delete')" @click="deleteLog" type="danger">删除</a-button>
        <span style="float: right; margin-top: 3px;">
          <a-button @click="init()">刷新</a-button>
          <qw-column-selector 
            :src-columns="columns"
            @change="(selectedColumns) => this.replaceArrayByArray('selectedColumns', selectedColumns)">
          </qw-column-selector>
        </span>
        <div style="clear: both;"></div>
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
          <a-tag :color="props.text === 'S' ? 'green' : 'red'">{{ props.text === 'S' ? '成功': '失败' }}</a-tag>
        </template>
      </qw-table>

      <template v-if="isEdit">
        <operate-log-detail :show.sync="isEdit" :data="editData" @on-close="(data) => this.init(false)"></operate-log-detail>
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
    title: '操作人',
    dataIndex: 'operatorName'
  },
  {
    title: '客户地址',
    dataIndex: 'ip'
  },
  {
    title: '操作结果',
    dataIndex: 'result',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '操作描述',
    dataIndex: 'operation'
  }
];

module.exports = asyncRequire([
  'alias!@API/system/log',
  'mixins',
  'moment'
], function(LogAPIS, MXS, Moment, resolve, reject) {
  resolve({
    name: 'OperateLogList',
    mixins: [MXS.AuthMixin, MXS.CommonMixin],
    components: {
      QwTable: load('@CMP/table/QWTable'),
      QwColumnSelector: load('@CMP/table/QWTableColumnSelector'),
      OperateLogDetail: load('./OperateLogDetail')
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
        isEdit: false
      }
    },
    mounted () {
      // this.$$hasAuth('operate-log-detail');
      this.$$fetchAuth();
      this.init(true);
      this.__vm_inited__ = true;
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
        var searchForm = this.form.getFieldsValue();
        if(searchForm.start) {
          searchForm.start = Moment(searchForm.start._d).format('YYYY-MM-DD');
        }
        if(searchForm.end) {
          searchForm.end = Moment(searchForm.end._d).format('YYYY-MM-DD');
        }
        return LogAPIS.queryLogs(searchForm, this.pagination.current, this.pagination.pageSize)
          .then((data) => {
            var logs = data.sdata;
            this.dataSource = [];
            logs.forEach((log, i) => {
              log.no = (this.pagination.current - 1) * 10 + i + 1;
              this.dataSource.push(log);
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
      showLogDetail (record) {
        // if(!this.$$hasAuth('operate-log-detail')) {
        //   return;
        // }
        this.editData = record;
        this.isEdit = true;
      },
      deleteLog () {
        this.$confirm({
          title: '删除确认',
          content: `确定删除选中的记录吗?`,
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            this.showLoading();
            return LogAPIS.deleteLogs(this.selectedRows.map(item => item.id))
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
              this.showLogDetail(record);
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
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    justify-content: center;
    align-items: center;
    align-content: center;
  }
  .fold-left {
    display: inline-block;
    width: calc(100% - 216px);
  }
  .fold-right{
    display: inline-block;
    width: 210px;
    text-align: right;
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
