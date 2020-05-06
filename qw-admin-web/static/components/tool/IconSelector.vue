<template>
  <div>
    <a-modal 
      title="选择图标"
      :visible="isVisible"
      :width="modalWidth"
      :body-style="modalBodyStyle"
      ok-text="添加"
      cancel-text="取消"
      :ok-button-props="{
        props: {
          loading: isLoading
        }
      }"
      @ok="handleOk"
      @cancel="handleCancel">
      <a-row>
        <!-- 左侧: 菜单树 -->
        <a-col :span="24">
          <a-tabs :default-active-key="0">
            <a-tab-pane v-for="item,index in antIcons" :key="index" :tab="item.title">
              <a-row type="flex" justify="start">
                <a-col :span="3" v-for="icon, index2 in item.icons" :key="index2" @click="onSelected(icon)">
                  <div class="icon-wrapper" :title="icon">
                    <a-icon :type="icon" style="font-size: 30px;"></a-icon>
                  </div>
                </a-col>  
              </a-row>  
            </a-tab-pane>
          </a-tabs>
        </a-col>
      </a-row>
    </a-modal>  
  </div>
</template>

<script lang="es6">
 module.exports = asyncRequire([
  'mixins',
  'alias!@CMP/tool/antIcons'
], function(MXS, AntIcons, resolve, reject) {
  resolve({
    name: 'IconSelector',
    mixins: [MXS.CommonMixin, MXS.ModalMixin],
    data () {
      return {
        modalWidth: 1000,
        modalBodyStyle: {
          width: '980px',
          border: 'none',
          maxHeight: '500px',
          overflowY: 'scroll'
        },
        antIcons: AntIcons
      }
    },
    created () {
      
    },
    methods: {
      handleSubmit (e) {
        e.preventDefault()
      },
      handleCancel () {
        this.close();
      },
      handleOk () {
        this.close();
      },
      onSelected (icon) {
        this.$emit('selected', icon);
        this.close();
      }
    }
  });
});
</script>

<style scoped>
.icon-wrapper {
  padding: 10px;
  width: 100%;
  text-align: center; 
  cursor: pointer;
}
.icon-wrapper:hover {
  background-color: #EEE;
  border-radius: 3px;
}
</style>
