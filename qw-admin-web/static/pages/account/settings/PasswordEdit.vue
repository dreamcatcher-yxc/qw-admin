<template>
  <div>
    <a-modal 
      title="修改密码" 
      :visible="isVisible"
      ok-text="保存"
      cancel-text="取消"
      :ok-button-props="{
        props: {
          loading: isLoading
        }
      }"
      @ok="handleOk"
      @cancel="handleCancel">
      
       <a-form :form="form" @submit="handleSubmit">
        <a-form-item v-bind="formItemLayout" label="原密码">
          <a-input-password
            v-decorator="[
              'srcPassword',
              {
                rules: [
                  {
                    required: true,
                    message: '原密码不能为空!',
                  },
                  {
                    validator: validatePassword
                  }
                ],
                validateFirst: true
              }
            ]"
          />
        </a-form-item>
        
        <a-form-item v-bind="formItemLayout" label="新密码">
          <a-input-password
            v-decorator="[
              'newPassword',
              {
                rules: [
                  {
                    required: true,
                    message: '新密码不能为空!',
                  },
                  {
                    validator: validatePassword
                  },
                  {
                    validator: (rule, value, callback) => {
                      let srcPassword = this.form.getFieldValue('srcPassword');
                      if(!srcPassword) {
                        return callback();
                      }
                      if(srcPassword === value) {
                        return callback('原密码和新密码不能相同');
                      }
                      return callback();
                    }
                  }
                ],
                validateFirst: true
              }
            ]"
          />
        </a-form-item>

        <a-form-item v-bind="formItemLayout" label="确认密码">
          <a-input-password
            v-decorator="[
              'confirmPassword',
              {
                rules: [
                  {
                    required: true,
                    message: '确认密码不能为空!',
                  },
                  {
                    validator: validatePassword
                  },
                  {
                    validator: (rule, value, callback) => {
                      let newPassword = this.form.getFieldValue('newPassword');
                      if(!newPassword) {
                        return callback();
                      }
                      if(newPassword !== value) {
                        return callback('确认密码与新密码不一致');
                      }
                      return callback();
                    }
                  }
                ],
                validateFirst: true
              }
            ]"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/system/account',
  'mixins'
], function(AccountAPIS, MXS, resolve, reject) {
  resolve({
    name: 'PasswordEdit',
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
        name: 'password_edit_form'
      });
    },
    methods: {
      handleSubmit (e) {
        e.preventDefault()
      },
      handleCancel () {
        this.close();
      },
      /**
       * 校验密码
       */
      validatePassword (rule, value, callback) {
        var uPattern = /^[a-zA-Z0-9_-]{6,16}$/;
        
        if(!(uPattern.test(value))) {
           callback('密码只能包含: 字母，数字，下划线，减号, 长度必须在6~16位之间');
        }

        callback();
      },
      handleOk () {
        this.form.validateFields((err, values) => {
          if (!err) {
            this.showLoading();
            AccountAPIS.modifyPassword(values)
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
