<template>
  <a-card>
    <div>
      <div>
        <a-form layout="horizontal" :form="form" @submit="onSearch">
          <div class="fold" v-if="$$hasAuth('login-log-list')">
            <a-row >
              <a-col :md="8" :sm="24" >
                <a-form-item
                  label="登录名"
                  :label-col="{span: 5}"
                  :wrapper-col="{span: 18, offset: 1}"
                >
                  <a-input 
                    v-decorator="[
                      'loginName'
                    ]"
                    placeholder="请输入"
                    >
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24" >
                <a-form-item
                  label="登录时间"
                  :label-col="{span: 5}"
                  :wrapper-col="{span: 18, offset: 1}"
                >

                  <a-range-picker
                    :ranges="ranges"
                    show-time
                    format="YYYY/MM/DD HH:mm:ss"
                    v-decorator="[
                      'dateRange'
                    ]"
                  >
                  </a-range-picker>
                </a-form-item>
              </a-col>
            </a-row>
          </div>
          <div style="float: right; margin-top: 3px;">
            <a-button type="primary" html-type="submit">查询</a-button>
            <a-button style="margin-left: 8px" @click="(e) => this.form.resetFields()">重置</a-button>
          </div>
        </a-form>
      </div>
      <div class="operator">
        <a-button v-if="$$hasAuth('login-log-delete')" @click="deleteLog" type="danger">删除</a-button>
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
          <a-icon
            :type="props.text === 'S' ? 'check-circle' : 'close-circle'"
            :style="{ fontSize: '18px', color: props.text === 'S' ? 'green' : 'red' }"
            />
        </template>
      </qw-table>

      <template v-if="isEdit">
        <login-log-detail :show.sync="isEdit" :data="editData" @on-close="(data) => this.init(false)"></login-log-detail>
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
    title: '登录用户',
    dataIndex: 'loginName'
  },
  {
    title: 'IP地址',
    dataIndex: 'ip'
  },
  {
    title: '登录地点',
    dataIndex: 'loginRegion',
  },
  {
    title: '登录时间',
    dataIndex: 'loginDate'
  },
  {
    title: '登录系统',
    dataIndex: 'osType'
  },
  {
    title: '浏览器',
    dataIndex: 'browserType'
  }
];

module.exports = asyncRequire([
  'alias!@API/system/loginLog',
  'mixins',
  'moment'
], function(LogAPIS, MXS, Moment, resolve, reject) {
  resolve({
    name: 'LoginLogList',
    mixins: [MXS.AuthMixin, MXS.CommonMixin],
    components: {
      QwTable: load('@CMP/table/QWTable'),
      QwColumnSelector: load('@CMP/table/QWTableColumnSelector'),
      LoginLogDetail: load('./LoginLogDetail')
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
        isEdit: false,
        ranges: {
          '本月': [Moment(), Moment().endOf('month')]
        }
      }
    },
    mounted () {
      this.$$fetchAuth();
      this.init(true);
      this.__vm_inited__ = true;
    },
    methods: {
      moment () {
        return Moment(arguments); 
      },

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
        if(searchForm.dateRange) {
          searchForm.startDate = Moment(searchForm.dateRange[0]._d).format('YYYY-MM-DD HH:mm:ss');
          searchForm.endDate = Moment(searchForm.dateRange[1]._d).format('YYYY-MM-DD HH:mm:ss');
        }
        var queryForm = {
          loginName: searchForm.loginName,
          startDate: searchForm.startDate,
          endDate: searchForm.endDate
        };
        return LogAPIS.queryLogs(queryForm, this.pagination.current, this.pagination.pageSize)
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
    width: calc(100% - 216px);
    display: inline-block
  }
/*   
  .fold:after {
    clear: right;
    display: inline-block;
  } */
  .operator{
    margin-bottom: 18px;
  }
  @media screen and (max-width: 900px) {
    .fold {
      width: 100%;
    }
  }
</style>
