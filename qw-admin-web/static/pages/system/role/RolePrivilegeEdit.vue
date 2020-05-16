<template>
  <div>
    <a-modal 
      title="权限分配"
      :visible="isVisible"
      :body-style="modalBodyStyle"
      ok-text="修改"
      cancel-text="取消"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <a-spin :spinning="isLoading">
        <a-row>
          <a-col :span="24">
            <role-privilege-tree ref="rpTree" :role-id="this.data.id"></role-privilege-tree>
          </a-col>
        </a-row>
      </a-spin>  
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'mixins'
], function(MXS, resolve, reject) {
  resolve({
    name: 'RolePrivilegeEdit',
    mixins: [MXS.CommonMixin, MXS.ModalMixin],
    components: {
      RolePrivilegeTree: load('./RolePrivilegeTree')
    },
    data () {
      return {
        modalBodyStyle: {
          width: '520px',
          border: 'none',
          maxHeight: '500px',
          overflowY: 'scroll'
        },
        roleTypes: []
      }
    },
    mounted () {
    },
    methods: {
      handleCancel () {
        this.close();
      },
      handleOk () {
        this.showLoading();
        this.$refs.rpTree.submit()
           .then(resp => {
             if(!!resp) {
               this.$message.success('角色权限信息更新成功');
             }
           })
          .finally(() => this.hideLoading())
          .then(() => this.close());
      }
    }
  });
});
</script>

<style scoped>
.iframe {
  width: 100%;
  min-height: 400px;
  border: none;
}
</style>
