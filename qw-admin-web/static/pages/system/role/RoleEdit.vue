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
        
        <a-form-item v-bind="formItemLayout" label="类型">
          <a-select
            v-decorator="[
              'type',
              {
                rules: [
                  {
                    required: true,
                    message: '类型不能为空!',
                  }
                ]
              }
            ]"
          >
            <a-select-option v-for="item,index in roleTypes" :key="index" :value="item.key">
              {{ item.value }}
            </a-select-option>
          </a-select>
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
    name: 'RoleEdit',
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
        },
        roleTypes: []
      }
    },
    created () {
      this.form = this.$form.createForm(this, {
        name: 'role_edit_form',
        mapPropsToFields: () => {
          return {
            name: this.$form.createFormField({
              value: this.data.name,
            }),
            type: this.$form.createFormField({
              value: this.data.type,
            }),
            description: this.$form.createFormField({
              value: this.data.description
            })
          };
        }
      });
    },
    mounted () {
      // 初始化可选的角色类型信息
      RoleAPIS.queryRoleTypes()
        .then(data => {
          let roleTypes = data.sdata.types;
          roleTypes.forEach(item => this.roleTypes.push(item));
        });
    },
    methods: {
      init() {
        
      },
      /**
       * 判断角色名是否存在
       */
      validateRoleName (rule, value, callback) {
        if(value === this.data.name) {
          callback();
          return;
        }

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
            RoleAPIS.editRole(values)
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
