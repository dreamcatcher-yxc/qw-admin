<template>
  <div>
    <a-modal 
      title="新增角色" 
      :visible="isVisible"
      ok-text="添加"
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
                  },
                  {
                    validator: validateRoleName
                  }
                ],
                validateFirst: true
              }
            ]"
          />
        </a-form-item>
        
        <a-form-item v-bind="formItemLayout" label="描述">
          <a-textarea 
            placeholder="描述信息" 
            allow-clear
            v-decorator="[
              'description'
            ]"  
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/system/rolePrivilege',
  'mixins'
], function(RoleAPIS, MXS, resolve, reject) {
  resolve({
    name: 'RoleAdd',
    mixins: [MXS.CommonMixin, MXS.ModalMixin, MXS.ValidatorMixin],
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
        }
      }
    },
    created () {
      this.form = this.$form.createForm(this, {
        name: 'role_add_form',
        mapPropsToFields: () => {
          return {
            gender: this.$form.createFormField({
              value: 'system',
            }),
            status: this.$form.createFormField({
              value: true,
            }),
          };
        }
      });
    },
    mounted () {
      // ...
    },
    methods: {
      handleSubmit (e) {
        e.preventDefault()
      },
      handleCancel () {
        this.close();
      },
      /**
       * 判断角色名是否存在
       */
      validateRoleName (rule, value, callback) {
        var uPattern = /^[a-zA-Z0-9_-]{4,16}$/;
        
        if(!(uPattern.test(value))) {
           callback(`用户名只能包含: 字母，数字，下划线，减号, 长度必须在4~16位之间`);
        }

        RoleAPIS.isExisted(value)
          .then(existed => {
            if(existed) {
              callback(`角色名【${value}】已存在`);
            } else {
              callback();
            }
          }) 
      },
      handleOk () {
        this.form.validateFields((err, values) => {
          if (!err) {
            this.showLoading();
            var formData = {};
            this.jQuery.extend(true,formData, values);
            RoleAPIS.addRole(formData)
                .then(data => {
                   this.close({ $hook: true});
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
