<template>
  <div class="container">
    <div class="content">
      <div class="top">
        <div class="header">
          <img alt="logo" class="logo" :src="logoUrl" />
          <span class="title">{{systemName}}</span>
        </div>
        <div class="desc">永捷物流</div>
      </div>
      <div class="login">
        <a-form @submit="onSubmit" :form="form">
          <a-tabs size="large" :tab-bar-style="{textAlign: 'center'}" style="padding: 0 2px;">
            <a-tab-pane tab="账户密码登录" key="1">
              <span>
                <a-alert type="error" :closable="true" v-show="error" :message="error" show-icon style="margin-bottom: 24px;" />
              </span>
              <a-form-item>
                <a-input 
                  size="large" 
                  placeholder="用户名"
                  v-decorator="[
                    'username',
                    {rules: [{ required: true, message: '请输入账户名', whitespace: true}]}
                  ]"
                >
                  <a-icon slot="prefix" type="user"></a-icon>
                </a-input>
              </a-form-item>

              <a-form-item>
                <a-input
                  size="large"
                  placeholder="密码" 
                  type="password"
                  v-decorator="[
                    'password',
                    {rules: [{ required: true, message: '请输入密码', whitespace: true}]}
                  ]"
                >
                  <a-icon slot="prefix" type="lock"></a-icon>
                </a-input>
              </a-form-item>

              <!-- 验证码 -->
              <a-form-item>
                <a-row :gutter="8">
                  <a-col :span="16">
                    <a-input
                     size="large" 
                     placeholder="验证码"
                     v-decorator="[
                      'checkCode',
                      {rules: [{ required: true, message: '请输入验证码', whitespace: true}]}
                     ]">
                      <a-icon slot="prefix" type="picture"></a-icon>
                    </a-input>
                  </a-col>
                  <a-col :span="8">
                    <img :src="checkCodeUrl" @click="changeCheckCode" title="验证码" style="width: 100px; height: 35px; border: 1px solid #E0E0E0;">
                  </a-col>
                </a-row>
              </a-form-item>
            </a-tab-pane>

            <a-tab-pane tab="手机号登录" key="2">
              <a-form-item>
                <a-input size="large" placeholder="mobile number" >
                  <a-icon slot="prefix" type="mobile"></a-icon>
                </a-input>
              </a-form-item>
              <a-form-item>
                <a-row :gutter="8" style="margin: 0 -4px">
                  <a-col :span="16">
                    <a-input size="large" placeholder="captcha">
                    <a-icon slot="prefix" type="mail"></a-icon>
                  </a-input>
                  </a-col>
                  <a-col :span="8" style="padding-left: 4px">
                    <a-button style="width: 100%" class="captcha-button" size="large">获取验证码</a-button>
                  </a-col>
                </a-row>
              </a-form-item>
            </a-tab-pane>
          </a-tabs>
          <div>
             <span @click="() => this.remember = !this.remember"><a-checkbox :checked="remember">自动登录</a-checkbox><span>
             <a style="float: right">忘记密码</a>
          </div>
          <a-form-item>
            <a-button :loading="logging" style="width: 100%; margin-top: 24px; cursor: pointer;" size="large" html-type="submit" type="primary">登录</a-button>
          </a-form-item>
          <div>
            其他登录方式
            <a-icon class="icon" type="alipay-circle"></a-icon>
            <a-icon class="icon" type="taobao-circle"></a-icon>
            <a-icon class="icon" type="weibo-circle"></a-icon>
            <router-link style="float: right" to="/dashboard/workplace" >注册账户</router-link>
          </div>
        </a-form>
      </div>
    </div>
    <global-footer :link-list="linkList" :copyright="copyright" />
  </div>
</template>

<script lang="es6">
  module.exports = asyncRequire([
    'alias-parser',
    'config',
    'alias!@API/auth/index'
  ], function(aliasParser, Conf, AuthAPI, resolve, reject) {
    resolve({
      name: 'Login',
      components: {
        GlobalFooter : load('@LAYOUT/GlobalFooter')
      },
      beforeCreate() {
        this.form = this.$form.createForm(this, { name: 'login_form' });
      },
      data () {
        return {
          logoUrl : aliasParser.parse('@IMG/vue-antd-logo.jpg'),
          logging: false,
          remember : true,
          error: '',
          formItemLayout: {
            labelCol: {
              xs: { span: 24 },
              sm: { span: 8 },
            },
            wrapperCol: {
              xs: { span: 24 },
              sm: { span: 16 },
            },
          },
          checkCodeUrl: Conf.format2ServerUrl('check-code?v=' + new Date().getTime())
        }
      },
      computed: {
        systemName () {
          return this.$store.state.setting.systemName;
        },
        linkList () {
          return this.$store.state.setting.footerLinks;
        },
        copyright () {
          return this.$store.state.setting.copyright;
        }
      },
      mounted () {
        this.jQuery('#loading-mask').remove();
        // 尝试自动登录
        AuthAPI.tryAutoLoginByCookie()
          .then(() => {
            this.$router.push('/dashboard/workplace');
          }).catch(() => {});
      },
      methods: {
        changeCheckCode() {
          this.checkCodeUrl = Conf.format2ServerUrl('check-code?v=' + new Date().getTime())
        },
        onSubmit (e) {
          e.preventDefault()
          this.form.validateFieldsAndScroll((err, values) => {
            if(err) {
              return;
            }

            this.logging = true;
            let formData = {
              username: this.form.getFieldValue('username'),
              password: this.form.getFieldValue('password'),
              checkCode: this.form.getFieldValue('checkCode'),
              remember: this.remember
            };
            AuthAPI.login(formData).then((data) => {
              this.$message.success('登录成功', 3);
              this.$router.push('/dashboard/workplace');
              // this.$store.commit('account/setuser', formData);
              this.logging = false;
            }).catch(eData => {
              this.changeCheckCode();
              this.error = eData.message;
              this.logging = false;
            })
          })
        }
      }
    })
  });
</script>

<style scoped>
  .container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    overflow: auto;
    background: #f0f2f5 url('https://gw.alipayobjects.com/zos/rmsportal/TVYTbAXWheQpRcWDaDMu.svg') no-repeat center 110px;
    background-size: 100%;
  }
  .container .content {
    padding: 32px 0;
    flex: 1;
  }
  
  @media (min-width: 768px) {
    .container .content {
    padding: 112px 0 24px;
  }

  }
  .container .content .top {
    text-align: center;
  }
  .container .content .top .header {
    height: 44px;
    line-height: 44px;
  }
  .container .content .top .header a {
    text-decoration: none;
  }
  .container .content .top .header .logo {
    height: 44px;
    vertical-align: top;
    margin-right: 16px;
    border-radius: 22px;
  }
  .container .content .top .header .title {
    font-size: 33px;
    color: rgba(0, 0, 0, 0.85);
    font-family: 'Myriad Pro', 'Helvetica Neue', Arial, Helvetica, sans-serif;
    font-weight: 600;
    position: relative;
    top: 2px;
  }
  .container .content .top .desc {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.45);
    margin-top: 12px;
    margin-bottom: 40px;
  }
  .container .content .login {
    width: 368px;
    margin: 0 auto;
  }
  @media screen and (max-width: 576px) {
    .container .content .login {
    width: 95%;
  }

  }
  @media screen and (max-width: 320px) {
    .container .content .login .captcha-button {
      font-size: 14px;
    }
  }
  .container .content .login .icon {
    font-size: 24px;
    color: rgba(0, 0, 0, 0.2);
    margin-left: 16px;
    vertical-align: middle;
    cursor: pointer;
    transition: color 0.3s;
  }
  .container .content .login .icon:hover {
    color: #1890ff;
  }
</style>
