<template>
  <a-card>
    <div>
      <div>
        <a-form layout="horizontal" :form="form" @submit="onSearch">
          <div class="fold" v-if="$$hasAuth('online-user-list')">
            <a-row >
              <a-col :md="8" :sm="24" >
                <a-form-item
                  label="用户名"
                  :label-col="{span: 5}"
                  :wrapper-col="{span: 18, offset: 1}"
                >
                  <a-input 
                    v-decorator="[
                      'username'
                    ]"
                    placeholder="请输入"
                    >
                  </a-input>
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
        <a-button v-if="$$hasAuth('online-user-delete')" @click="deleteLog" type="danger">踢出</a-button>
        <div style="float: right; margin-top: 3px;">
          <a-button @click="init()">刷新</a-button>
          <qw-column-selector 
            :src-columns="columns"
            @change="(selectedColumns) => this.replaceArrayByArray('selectedColumns', selectedColumns)">
          </qw-column-selector>
        </div>
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
          <span v-if="props.text">
            {{' 在线 '}}<a-tag color="blue">当前用户</a-tag>
          </span>
          <span v-else>
            {{ '在线' }}
          </span>
        </template>
      </qw-table>

      <template v-if="isEdit">
        <online-user-detail :show.sync="isEdit" :data="editData" @on-close="(data) => this.init(false)"></online-user-detail>
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
    title: '用户名',
    dataIndex: 'username'
  },
  {
    title: '登录时间',
    dataIndex: 'loginDate'
  },
  {
    title: '最近访问时间',
    dataIndex: 'lastAccessDate'
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
    title: '状态',
    dataIndex: 'isCurrentUser',
    scopedSlots: { customRender: 'status' }
  }
];

module.exports = asyncRequire([
  'alias!@API/system/onlineUser',
  'mixins',
  'moment'
], function(OnlineUserAPIS, MXS, Moment, resolve, reject) {
  resolve({
    name: 'OnlineUserList',
    mixins: [MXS.AuthMixin, MXS.CommonMixin],
    components: {
      QwTable: load('@CMP/table/QWTable'),
      QwColumnSelector: load('@CMP/table/QWTableColumnSelector'),
      OnlineUserDetail: load('./OnlineUserDetail')
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
        var queryForm = {
          username: searchForm.username
        };
        return OnlineUserAPIS.queryOnlineUsers(queryForm, this.pagination.current, this.pagination.pageSize)
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
        return;
        this.editData = record;
        this.isEdit = true;
      },

      deleteLog () {
        this.$confirm({
          title: '踢出确认',
          content: '确定将选中的在线用户强制下线吗?',
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            this.showLoading();
            return OnlineUserAPIS.deleteOnlineUsers(this.selectedRows.map(item => item.sessionId))
              .then((data) => {
                this.$message.success('操作成功');
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
  .operator{
    margin-bottom: 18px;
  }
  @media screen and (max-width: 900px) {
    .fold {
      width: 100%;
    }
  }
</style>
