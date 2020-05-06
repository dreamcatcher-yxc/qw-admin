<template>
  <div>
    <a-modal 
      title="新增用户" 
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
        <a-form-item v-bind="formItemLayout" label="用户名">
          <a-input
            v-decorator="[
              'username',
              {
                rules: [
                  {
                    required: true,
                    message: '用户名不能为空!',
                  },
                  {
                    validator: validateUsername
                  }
                ],
                validateFirst: true
              }
            ]"
          />
        </a-form-item>
        
        <a-form-item v-bind="formItemLayout" label="昵称">
          <a-input
            v-decorator="[
              'nickname',
              {
                rules: [
                  {
                    required: true,
                    message: '昵称不能为空!',
                  }
                ]
              }
            ]"
          />
        </a-form-item>

        <a-form-item v-bind="formItemLayout" label="性别">
          <a-radio-group 
            default-value="M"
            v-decorator="[
              'gender',
              {
                rules: [
                  {
                    required: true,
                    message: '性别必选!',
                  }
                ]
              }
            ]">
            <a-radio value="M">
              男
            </a-radio>
            <a-radio value="F">
              女
            </a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/system/user',
  'mixins'
], function(UserAPIS, MXS, resolve, reject) {
  resolve({
    name: 'UserAdd',
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
        name: 'user_edit_form',
        mapPropsToFields: () => {
          return {
            gender: this.$form.createFormField({
              value: 'M',
            })
          };
        }
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
       * 判断用户名是否存在
       */
      validateUsername (rule, value, callback) {
        var uPattern = /^[a-zA-Z0-9_-]{2,16}$/;
        
        if(!(uPattern.test(value))) {
           callback(`用户名只能包含: 字母，数字，下划线，减号, 长度必须在2~16位之间`);
        }

        UserAPIS.isExisted(value)
          .then(existed => {
            if(existed) {
              callback(`用户名【${value}】已存在`);
            } else {
              callback();
            }
          }) 
      },
      handleOk () {
        this.form.validateFields((err, values) => {
          if (!err) {
            this.showLoading();
            UserAPIS.addUser(values)
                .then(data => {
                   this.close({ $hook: true});
                }).catch(data => {
                   this.$message.error(data.message);
                }).finally(() => this.hideLoading());
          } else {
            // this.$message.error('校验失败!');
          }
        });
        // this.close();
      }
    }
  });
});
</script>

<style scoped>
  
</style>
