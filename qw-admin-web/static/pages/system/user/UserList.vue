<template>
  <a-card>
    <div>
      <a-form layout="horizontal" :form="form" @submit="onSearch">
        <div class="fold" v-if="$$hasAuth('user-list')">
          <a-row >
          <a-col :md="24" :sm="24" >
            <a-form-item
              label="用户名"
              :label-col="{span: 2}"
              :wrapper-col="{span: 4, offset: 0}"
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
        <span style="float: right; margin-top: 3px;">
          <a-button type="primary" html-type="submit">查询</a-button>
          <a-button style="margin-left: 8px" @click="(e) => this.form.resetFields()">重置</a-button>
        </span>
      </a-form>
    </div>
    <div>
      <div class="operator">
        <a-button v-if="$$hasAuth('user-add')" @click="addUser" type="primary">新建</a-button>
        <a-button v-if="$$hasAuth('user-assign-role')" @click="assignRole" type="primary">分配角色</a-button>
        <a-button v-if="$$hasAuth('user-delete')" @click="deleteUser" type="danger">删除</a-button>
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
          <a-tag :color="props.text === 1 ? 'green' : '#CCC'">{{ props.text === 1 ? '启用' : '禁用' }}</a-tag>
        </template>
      </qw-table>

      <template v-if="isAdd">
        <user-add :show.sync="isAdd" @on-close="(data) => this.init(true)"></user-add>
      </template>
      <template v-if="isEdit">
        <user-edit :show.sync="isEdit" :data="editData" @on-close="(data) => this.init(false)"></user-edit>
      </template>
      <template v-if="isEditUserRole">
        <user-role-edit :show.sync="isEditUserRole" :data="editData"></user-role-edit>
      </template> 
    </div>
  </a-card>
</template>

<script lang="es6">
const columns = [
  {
    title: '编号',
    dataIndex: 'no',
    customRender: text => text
  },
  {
    title: '用户名',
    dataIndex: 'username',
    sorter: true
  },
  {
    title: '昵称',
    dataIndex: 'nickname'
  },
  {
    title: '性别',
    dataIndex: 'gender',
    customRender: (text) => text.toUpperCase() === 'M' ? '男' : '女',
  },
  {
    title: '状态',
    dataIndex: 'status',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '电话',
    dataIndex: 'phone',
    sorter: true,
    show: false,
    customRender: (text) => text||'/',
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    show: false,
    customRender: (text) => text||'/',
  }
];

module.exports = asyncRequire([
  'alias!@API/system/user',
  'mixins'
], function(UserAPIS, MXS, resolve, reject) {
  resolve({
    name: 'UserList',
    mixins: [MXS.AuthMixin, MXS.CommonMixin],
    components: {
      QwTable: load('@CMP/table/QWTable'),
      QwColumnSelector: load('@CMP/table/QWTableColumnSelector'),
      UserAdd: load('./UserAdd'),
      UserEdit: load('./UserEdit'),
      UserRoleEdit: load('./UserRoleEdit')
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
        isAdd: false,
        isEdit: false,
        editData: {},
        isEditUserRole: false
      }
    },
    mounted () {
      this.$$hasAuth('user-edit');
      this.$$fetchAuth();
      this.init(true);
      this.__vm_inited__ = true;
    },
    methods: {
      reopen() {
        // this.$message.info('该标签被重新打开');
        this.init(true);
      },
      init(reset) {
        this.showLoading();
        if(reset) {
          this.form.resetFields();
          this.pagination.current = 1;
        }
        UserAPIS.queryUsers(this.form.getFieldValue('username'), this.pagination.current, this.pagination.pageSize)
          .then((data) => {
            var users = data.sdata;
            this.dataSource = [];
            users.forEach((user, i) => {
              user.no = (this.pagination.current - 1) * 10 + i + 1;
              this.dataSource.push(user);
            });
            this.pagination.total = data.scount;
            this.hideLoading();
          });
      },
      onSearch(e) {
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
      addUser () {
        this.isAdd = true;
      },
      editUser (record) {
        if(!this.$$hasAuth('user-edit')) {
          return;
        }
        this.isEdit = true;
        this.editData = record;
      },
      assignRole () {
        if(this.selectedRows.length !== 1) {
          this.$message.warning('有且仅能选择一个用户');
          return;
        }
        this.editData = this.selectedRows[0];
        this.isEditUserRole = true;
      },
      deleteUser () {
        if(this.selectedRows.length <= 0) {
          this.$message.warning('至少选择一行');
          return;
        }
        this.$confirm({
          title: '删除确认',
          content: `确定删除选择的【${this.selectedRows.length}】条记录吗?`,
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            this.showLoading();
            return UserAPIS.deleteUsers(this.selectedRows.map(user => user.id))
              .then((data) => {
                this.$message.success('删除成功');
                this.selectedRows = [];
                this.form.resetFields();
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
              this.editUser(record);
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
