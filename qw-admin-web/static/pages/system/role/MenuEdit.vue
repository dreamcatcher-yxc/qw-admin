<template>
  <div>
    <a-modal 
      title="编辑菜单" 
      :visible="isVisible"
      ok-text="修改"
      cancel-text="取消"
      :ok-button-props="{
        props: {
          loading: isLoading
        }
      }"
      @ok="handleOk"
      @cancel="handleCancel">
      
       <a-form :form="form" @submit="handleSubmit">
        <a-form-item v-bind="formItemLayout" label="名称">
          <a-input
            v-decorator="[
              'name',
              {
                rules: [
                  {
                    required: true,
                    message: '名称不能为空!',
                  }
                ],
                validateFirst: true
              }
            ]"
          />
        </a-form-item>
        
        <a-form-item v-if="isMenuItem" v-bind="formItemLayout" label="资源路径">
          <a-input
            v-decorator="[
              'url',
              {
                rules: [
                  {
                    required: true,
                    message: '资源路径不能为空!',
                  }
                ]
              }
            ]"
          />
        </a-form-item>

        <a-form-item v-if="canBeSelectIcon" v-bind="formItemLayout" label="图标">
          <a-button @click="isShowIconSelectorView = true">
            <template v-if="!selectedIcon">
              选择图标
            </template>
            <template v-else>
              <a-icon :type="selectedIcon" style="font-size: 16px;"></a-icon>
            </template>   
          </a-button>
        </a-form-item>  
        
        <a-form-item v-bind="formItemLayout" label="描述">
          <a-textarea 
            placeholder="" 
            allow-clear
            v-decorator="[
              'description'
            ]"  
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <icon-selector :show.sync="isShowIconSelectorView" @selected="onIconSelected"></icon-selector>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/system/menu',
  'mixins'
], function(MenuAPIS, MXS, resolve, reject) {
  resolve({
    name: 'MenuEdit',
    mixins: [MXS.CommonMixin, MXS.ModalMixin, MXS.ValidatorMixin],
    components: {
      IconSelector: load('@CMP/tool/IconSelector')
    },
    data () {
      return {
        formItemLayout: {
          labelCol: {
            xs: { span: 24 },
            sm: { span: 4 },
          },
          wrapperCol: {
            xs: { span: 24 },
            sm: { span: 20 },
          }
        },
        isShowIconSelectorView: false,
        canBeSelectIcon: false,
        selectedIcon: null,
        isMenuItem: false
      }
    },
    created () {
      this.form = this.$form.createForm(this, { name: 'menu_edit_form' });
    },
    mounted () {
      this.init();
    },
    methods: {
      init() {
        MenuAPIS.queryMenu(this.data.menuId)
          .then(resp => resp.sdata.menu)
          .then(menu => {
            this.selectedIcon =  menu.icon;
            this.isMenuItem = menu.isLeaf === 1;
            this.canBeSelectIcon = !(this.isMenuItem && menu.treeParentId.indexOf('-') > 0);

            if(this.isMenuItem) {
              this.form.getFieldDecorator('url', {
                rules: [
                  {
                    required: true,
                    message: '资源路径不能为空!',
                  }
                ]
              });
            }

            this.form.setFieldsValue({
              name: menu.name,
              url: menu.url,
              description: menu.description
            });
          });
      },

      onIconSelected(icon) {
        this.selectedIcon = icon;
      },

      handleSubmit (e) {
        e.preventDefault()
      },

      handleCancel () {
        this.close();
      },

      handleOk () {
        this.form.validateFields((err, values) => {
          if (!err) {
            this.showLoading();
            values.id = this.data.menuId;
            values.icon = this.selectedIcon||'';
            values.url = values.url||'';
            MenuAPIS.editMenu(values)
              .then(data => {
                  this.close({ $hook: true });
              }).catch(data => {
                  this.$message.error(data.message);
              }).finally(() => this.hideLoading());
          }
        });
      }
    }
  });
});
</script>

<style scoped>
  
</style>
