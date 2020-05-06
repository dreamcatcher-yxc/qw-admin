<template>
  <div class="account-settings-info-view">
    <a-row :gutter="16">
      <a-col :md="24" :lg="16">
        <template v-if="isLoading">
          <a-skeleton avatar :paragraph="{ rows: 4 }" />
        </template>
        <template v-else>
          <template v-if="!isEdit">
            <a-descriptions bordered>
              <a-descriptions-item :span="3" label="用户名">{{ userData.username }}</a-descriptions-item>
              <a-descriptions-item :span="3" label="昵称">{{ userData.nickname }}</a-descriptions-item>
              <a-descriptions-item :span="3" label="性别">{{ userData.gender.toLowerCase() === 'm' ? '男' : '女' }}</a-descriptions-item>
              <a-descriptions-item :span="3" label="电话">{{ userData.phone }}</a-descriptions-item>
              <a-descriptions-item :span="3" label="邮箱">{{ userData.email }}</a-descriptions-item>
              <!-- <a-descriptions-item label="类型">系统用户</a-descriptions-item> -->
            </a-descriptions>
            <div style="float: right; padding: 10px;">
              <a-button type="primary" @click="toEditView">修改</a-button>
            </div>  
          </template>  
          
          <template v-if="isEdit">
            <a-form :form="form" @submit="handleSubmit" layout="vertical">
              <a-form-item
                label="用户名"
              >
                <a-input :value="userData.username" disabled />
              </a-form-item>

              <a-form-item
                label="昵称"
              >
                <a-input 
                  v-decorator="[
                    'nickname',
                    {
                      rules: [
                        { required: true, message: '昵称不能为空' }
                      ]
                    }
                  ]" 
                  placeholder="昵称" />
              </a-form-item>

              <a-form-item label="性别">
                <a-radio-group 
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
                  <a-radio value="M">男</a-radio>
                  <a-radio value="F">女</a-radio>
                </a-radio-group>
              </a-form-item>

              <a-form-item label="电话">
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

              <a-form-item label="邮箱">
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

              <a-form-item>
                <a-button type="primary" :loading="isLoading" html-type="submit">保存</a-button>
                <a-button style="margin-left: 8px" @click="isEdit = false">取消</a-button>
              </a-form-item>
            </a-form>
          </template>
        </template>
      </a-col>
      <a-col :md="24" :lg="8" :style="{ minHeight: '180px' }">
        <div class="ant-upload-preview" @click="isEditAvatar = true" >
          <a-icon type="cloud-upload-o" class="upload-icon"></a-icon>
          <div class="mask">
            <a-icon type="plus"></a-icon>
          </div>
          <img :src="option.img" />
        </div>
      </a-col>
    </a-row>

    <template v-if="isEditAvatar">
      <avatar-modal ref="modal" :show.sync="isEditAvatar" :img="option.img" @on-close="setavatar"/>
    </template>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/auth/index',
  'alias!@API/system/account',
  'mixins',
  'alias-parser'
], function (AuthAPIS, AccountAPIS, MXS, AliasParser, resolve, reject) {
  resolve({
    name: 'BaseSettings',
    mixins: [MXS.CommonMixin, MXS.ValidatorMixin],
    components: {
      AvatarModal: load('./AvatarModal')
    },
    data () {
      return {
        // cropper
        preview: {},
        option: {
          img: '',
          info: true,
          size: 1,
          outputType: 'jpeg',
          canScale: false,
          autoCrop: true,
          // 只有自动截图开启 宽度高度才生效
          autoCropWidth: 180,
          autoCropHeight: 180,
          fixedBox: true,
          // 开启宽度和高度比例
          fixed: true,
          fixedNumber: [1, 1]
        },
        userData: {
          gender: 'M'
        },
        isEdit: false,
        isEditAvatar: false,
        form: null
      }
    },
    mounted () {
      this.initUserData();
    },
    methods: {
      initUserData () {
        this.showLoading;
        return AuthAPIS.fetchUserInfo()
          .then(data => {
            let userInfo = data.sdata.userInfo;
            this.userData = userInfo;
            // this.option.img = AliasParser.parse('@SERVER_IMGS_BASE_URL/' + this.userData.header);
            this.option.img = this.userData.header;
            return userInfo;
            this.hideLoading();
          });
      },
      initEditFormInfo (userInfo) {
        this.form = this.$form.createForm(this, {
          name: 'edit_form',
          mapPropsToFields: () => {
            return {
              nickname: this.$form.createFormField({
                value: userInfo.nickname
              }),
              gender: this.$form.createFormField({
                value: userInfo.gender
              }),
              phone: this.$form.createFormField({
                value: userInfo.phone
              }),
              email: this.$form.createFormField({
                value: userInfo.email
              })
            };
          }
        });
      },
      toEditView() {
        this.isEdit = true;
        this.initEditFormInfo(this.userData);
      },
      setavatar (rdata) {
        if(rdata.$hook) {
          this.initUserData();
        }
      },
      handleSubmit (e) {
        e.preventDefault();
        this.form.validateFields((err, values) => {
          if (!err) {
            this.showLoading();
            AccountAPIS.modifyBaseInfo(values)
              .then((data) => {
                this.$message.info('修改成功');
                // 重新加载用户信息
                this.$store.dispatch('account/fetchUserInfo');
                this.initUserData()
                  .then(() => this.isEdit = false);
              }).catch((data) => {
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
.avatar-upload-wrapper {
  height: 200px;
  width: 100%;
}
.ant-upload-preview {
  position: relative;
  margin: 0 auto;
  width: 100%;
  max-width: 180px;
  border-radius: 50%;
  box-shadow: 0 0 4px #ccc;
}
.ant-upload-preview .upload-icon {
  position: absolute;
  top: 0;
  right: 10px;
  font-size: 1.4rem;
  padding: 0.5rem;
  background: rgba(222, 221, 221, 0.7);
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.2);
}
.ant-upload-preview .mask {
  opacity: 0;
  position: absolute;
  background: rgba(0, 0, 0, 0.4);
  cursor: pointer;
  transition: opacity 0.4s;
}
.ant-upload-preview .mask:hover {
  opacity: 1;
}
.ant-upload-preview .mask i {
  font-size: 2rem;
  position: absolute;
  top: 50%;
  left: 50%;
  margin-left: -1rem;
  margin-top: -1rem;
  color: #d6d6d6;
}
.ant-upload-preview img,
.ant-upload-preview .mask {
  width: 100%;
  max-width: 180px;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;
}
</style>
