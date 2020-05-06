<template>
  <div>
    <a-modal 
      title="分配角色" 
      :visible="isVisible"
      ok-text="修改"
      cancel-text="取消"
      :body-style="{
        'height': '200px',
        'overflowY': 'scroll'
      }"
      :ok-button-props="{
        props: {
          loading: isLoading
        }
      }"
      @ok="handleOk"
      @cancel="handleCancel">
      <a-tree
        checkable
        :expanded-keys.sync="expandedKeys"
        :auto-expand-parent="autoExpandParent"
        v-model="checkedKeys"
        :load-data="onLoadData"
        :tree-data="treeData"
      />
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/system/user',
  'mixins'
], function(UserAPIS, MXS, resolve, reject) {
  resolve({
    name: 'UserRoleEdit',
    mixins: [MXS.CommonMixin, MXS.ModalMixin, MXS.ValidatorMixin],
    components: {
      QwTable: load('@CMP/table/QWTable')
    },
    data () {
      return {
        expandedKeys: ['root'],
        autoExpandParent: true,
        checkedKeys: [],
        treeData: [
          {
            title: '角色树',
            key: 'root',
            children: []
          }
        ],
      }
    },
    mounted () {
    },
    methods: {
      onLoadData(treeNode) {
        this.showLoading();
        return UserAPIS.queryUserRole(this.data.id)
          .then(data => {
            var sdata = data.sdata;
            var allRoles = sdata.allRoles;
            var rids = sdata.rids;
            
            if(Array.isArray(allRoles)) {
              allRoles.forEach(role => {
                treeNode.dataRef.children.push({
                  title: role.name,
                  key: role.id + '',
                  isLeaf: true
                });
              });
            }

            if(Array.isArray(rids)) {
              rids.forEach(id => {
                this.checkedKeys.push(id + '');
              });
            }
          }).finally(() => this.hideLoading());
      },
      handleCancel () {
        this.close();
      },
      handleOk () {
        this.showLoading();
        var form = {
          userId: this.data.id,
          rids: this.checkedKeys.filter(key => key !== 'root').join(',')
        };
        UserAPIS.editUserRole(form)
          .then(data => {
            this.close();
          }).catch(data => {
            this.$message.error(data.message);
          }).finally(() => this.hideLoading());
      }
    }
  });
});
</script>

<style scoped>
  
</style>
