<template>
  <a-card>
    <div>
      <a-form layout="horizontal" :form="form" @submit="onSearch">
        <div class="fold" v-if="$$hasAuth('role-list')">
          <a-row >
          <a-col :md="24" :sm="24" >
            <a-form-item
              label="角色名"
              :label-col="{span: 2}"
              :wrapper-col="{span: 4, offset: 0}"
            >
              <a-input 
                v-decorator="[
                  'name'
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
        <a-button v-if="$$hasAuth('role-add')" @click="addRole" type="primary">新建</a-button>
        <a-button @click="assignPrivilege" type="primary">分配权限</a-button>
        <a-button @click="menuPrivilegeManage" type="primary">菜单权限配置</a-button>
        <a-button v-if="$$hasAuth('privilege-generate')" @click="privilegeGenerate" type="primary">重新生成权限树</a-button>
        <a-button v-if="$$hasAuth('role-delete')" @click="deleteRole" type="danger">删除</a-button>
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
      ></qw-table>

      <template v-if="isAdd">
        <role-add :show.sync="isAdd" @on-close="(data) => this.init(true)"></role-add>
      </template>
      <template v-if="isEdit">
        <role-edit :show.sync="isEdit" :data="editData" @on-close="(data) => this.init(false)"></role-edit>
      </template>
      <template v-if="isEditRolePrivilege">
        <role-privilege-edit :show.sync="isEditRolePrivilege" :data="editData"></role-privilege-edit>
      </template> 
      <template v-if="isMenuPrivilegeManage">
        <menu-privilege-manage :show.sync="isMenuPrivilegeManage"></menu-privilege-manage>
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
    title: '名称',
    dataIndex: 'name'
  },
  {
    title: '类型',
    dataIndex: 'type'
  },
  {
    title: '描述',
    dataIndex: 'description'
  }
];

module.exports = asyncRequire([
  'alias!@API/system/rolePrivilege',
  'mixins'
], function(RoleAPIS, MXS, resolve, reject) {
  resolve({
    name: 'RoleList',
    mixins: [MXS.AuthMixin, MXS.CommonMixin],
    components: {
      QwTable: load('@CMP/table/QWTable'),
      QwColumnSelector: load('@CMP/table/QWTableColumnSelector'),
      RoleAdd: load('./RoleAdd'),
      RoleEdit: load('./RoleEdit'),
      RolePrivilegeEdit: load('./RolePrivilegeEdit'),
      MenuPrivilegeManage: load('./MenuPrivilegeManage'),
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
        form: this.$form.createForm(this, { name: 'search_form' }),
        isAdd: false,
        isEdit: false,
        editData: {},
        isEditRolePrivilege: false,
        isMenuPrivilegeManage: false,
        isPrivilegeGenerate: false
      }
    },
    mounted () {
      this.$$hasAuth('role-edit');
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
        RoleAPIS.queryRoles(this.form.getFieldValue('name'), this.pagination.current)
          .then((data) => {
            var roles = data.sdata;
            this.dataSource = [];
            roles.forEach((role, i) => {
              role.no = (this.pagination.current - 1) * 10 + i + 1;
              this.dataSource.push(role);
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
      addRole () {
        this.isAdd = true;
      },
      editRole (record) {
        if(!this.$$hasAuth('role-edit')) {
          return;
        }
        this.isEdit = true;
        this.editData = record;
      },
      assignPrivilege () {
        if(this.selectedRows.length <= 0) {
          this.$message.warning('有且仅能选择一行');
          return;
        }
        this.editData = this.selectedRows[0];
        this.isEditRolePrivilege = true;
      },
      menuPrivilegeManage () {
        this.isMenuPrivilegeManage = true;
      },
      privilegeGenerate () {
        this.isPrivilegeGenerate = true;
        this.$confirm({
          title: '操作确认',
          content: `确定重新生成权限树信息吗?`,
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            this.showLoading();
            return RoleAPIS.privilegeGenerate()
              .then((data) => {
                this.$message.success('操作成功');
              })
              .catch((data) => {
                this.$message.error(data.message);
              })
              .finally(() => this.hideLoading());
          },
          onCancel() {}
        });
      },
      deleteRole () {
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
            return RoleAPIS.deleteRoles(this.selectedRows.map(role => role.id))
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
              this.editRole(record);
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
