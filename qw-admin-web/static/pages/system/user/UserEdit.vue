<template>
  <div>
    <a-modal 
      title="编辑用户" 
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
        <a-form-item v-bind="formItemLayout" label="用户名">
          <a-input
            :disabled="true"
            v-decorator="[
              'username'
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
            v-bind:value="'M'"
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

        <a-form-item v-bind="formItemLayout" label="电话">
          <a-input
            v-decorator="[
              'phone',
              {
                rules: [
                  {
                    required: true,
                    message: '电话必填!',
                  },
                  {
                    validator: validPhone 
                  }
                ],
                validateFirst: true
              }
            ]"
          />
        </a-form-item>

        <a-form-item v-bind="formItemLayout" label="邮箱">
          <a-input
            v-decorator="[
              'email',
              {
                rules: [
                  {
                    required: true,
                    message: '邮箱必填!',
                  },
                  {
                    validator: validEmail 
                  }
                ],
                validateFirst: true
              }
            ]"
          />
        </a-form-item>

        <a-form-item v-bind="formItemLayout" label="状态">
          <a-switch 
            v-decorator="[
              'status', 
              { 
                valuePropName: 'checked' 
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
  'alias!@API/system/user',
  'mixins'
], function(UserAPIS, MXS, resolve, reject) {
  resolve({
    name: 'UserEdit',
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
            username: this.$form.createFormField({
              value: this.data.username,
            }),
            nickname: this.$form.createFormField({
              value: this.data.nickname,
            }),
            gender: this.$form.createFormField({
              value: this.data.gender.toUpperCase(),
            }),
            phone: this.$form.createFormField({
              value: this.data.phone,
            }),
            email: this.$form.createFormField({
              value: this.data.email,
            }),
            status: this.$form.createFormField({
              value: this.data.status === 1
            })
          };
        }
      });
    },
    mounted () {
    },
    methods: {
      init() {
        
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
            values.id = this.data.id;
            values.autoReplaceHeader = false;
            values.status = values.status ? 1 : 0;

            UserAPIS.editUser(values)
                .then(data => {
                  this.$message.success(data.message);
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
