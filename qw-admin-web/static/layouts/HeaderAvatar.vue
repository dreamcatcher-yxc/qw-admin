<template>
  <a-dropdown style="display: inline-block; height: 100%; vertical-align: initial" >
    <span style="cursor: pointer">
      <a-avatar class="avatar" size="small" shape="circle" :src="currUser.avatar"></a-avatar>
      <span>{{currUser.name}}</span>
    </span>
    <a-menu style="width: 150px" slot="overlay" @click="handler">
      <!-- 
      <a-menu-item key="user">
        <a-icon type="user"></a-icon>
        <span>个人中心</span>
      </a-menu-item> 
      -->
      <a-menu-item key="setting">
        <a-icon type="setting"></a-icon>
        <span>设置</span>
      </a-menu-item>
      <a-menu-divider></a-menu-divider>
      <a-menu-item key="logout">
          <a-icon type="poweroff"></a-icon>
          <span>退出登录</span>
      </a-menu-item>
    </a-menu>
  </a-dropdown>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/auth/index'
], function name(AuthAPI, resolve, reject) {
  resolve({
    name: 'HeaderAvatar',
    computed: {
      currUser () {
        return this.$store.state.account.user;
      }
    },
    mounted () {
      this.$store.dispatch('account/fetchUserInfo');
    },
    methods: {
      handler (data) {
        var key = data.key;
        if(key === "logout") {
          this.$confirm({
            title: '确认注销当前用户的登录状态吗?',
            content: '',
            okText: '确认',
            cancelText: '取消',
            onOk: () => {
              AuthAPI.logout()
                .then(r => {
                  this.$router.push({ path : '/login' });
                }).catch(r => {
                  this.$router.push({ path : '/login' });
                });
            }
          });
        } else if(key === 'setting'){
          this.$router.push('/account/settings');
        } else {
          this.$message.warning('实现中...');
        }
      }
    }
  });
});
</script>

<style scoped>
  .avatar{
    margin: 20px 4px 20px 0;
    color: #1890ff;
    background: hsla(0,0%,100%,.85);
    vertical-align: middle;
  }
</style>