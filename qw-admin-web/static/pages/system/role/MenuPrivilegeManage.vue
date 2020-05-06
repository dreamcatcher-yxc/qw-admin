<template>
  <span>
    <a-modal 
      title="菜单权限管理" 
      :visible="isVisible"
      :width="modalWidth"
      :body-style="modalBodyStyle"
      cancel-text="取消"
      @cancel="handleCancel">
      <template slot="footer">
        <a-button 
          type="primary" 
          @click="handleOk">
          关闭
        </a-button>
      </template>
      <a-row>
        <!-- 左侧: 菜单树 -->
        <a-col :span="12">
          <menu-tree @select="onMenuTreeNodeSelected"></menu-tree>
        </a-col>
        <!-- 右侧: 权限树 -->
        <a-col :span="12">
          <privilege-tree :menu-id="selectedMenuId"></privilege-tree>
        </a-col>
      </a-row>
    </a-modal>
  </span>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'mixins'
], function(MXS, resolve, reject) {
  resolve({
    name: 'MenuPrivilegeManage',
    mixins: [MXS.CommonMixin, MXS.ModalMixin],
    components: {
      MenuTree: load('./MenuTree'),
      PrivilegeTree: load('./PrivilegeTree')
    },
    data () {
      return {
        modalWidth: 1000,
        modalBodyStyle: {
          width: '980px',
          border: 'none'
        },
        selectedMenuId: null,
      }
    },
    methods: {
      onMenuTreeNodeSelected (data) {
        this.selectedMenuId = data.treeNode.bId;
      },
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
  min-height: 600px;
  border: none;
}
</style>
