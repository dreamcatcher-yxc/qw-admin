<template>
  <div>
    <a-modal 
      title="权限分配"
      :visible="isVisible"
      :body-style="modalBodyStyle"
      @cancel="handleCancel"
    >
    <template slot="footer">
      <a-button 
        type="primary" 
        @click="handleOk">
        关闭
      </a-button>
    </template>
      <a-row>
        <a-col :span="24">
          <role-privilege-tree :role-id="this.data.id"></role-privilege-tree>
        </a-col>
      </a-row>  
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
        this.close();
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
