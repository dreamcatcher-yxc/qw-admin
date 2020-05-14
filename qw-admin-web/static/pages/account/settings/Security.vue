<template>
  <div>
    <a-list
      item-layout="horizontal"
      :data-source="menus"
    >
      <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
        <a-list-item-meta>
          <a slot="title">{{ item.title }}</a>
          <span slot="description">
            <span class="security-list-description">{{ item.description }}</span>
            <span v-if="item.value"> : </span>
            <span class="security-list-value" v-html="item.value"></span>
          </span>
        </a-list-item-meta>
        <template v-if="item.actions">
          <a slot="actions" @click="invoke(item.actions.callback)">{{ item.actions.title }}</a>
        </template>
      </a-list-item>
    </a-list>
    <template v-if="isEdit">
      <password-edit :show.sync="isEdit" @on-close="onPasswordEditorClose"></password-edit>
    </template>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/auth/index'
], function(AuthAPIS, resolve, reject) {
  resolve({
    name: 'Security',
    components: {
      PasswordEdit: load('./PasswordEdit')
    },
    computed : {
      canBeAutoLogin () {
        return this.$store.getters['account/canBeAutoLogin'];
      },
      menus () {
        let menus = [
          { 
              title: '账户密码', 
              description: '提示', 
              value: '<span style="color:red">为了您的账户安全，建议定期更换密码</span>', 
              actions: { 
                title: '修改',
                callback: 'editPassword'
              } 
          }
        ];
        if(this.canBeAutoLogin) {
          menus.push({ 
            title: '自动登录', 
            description: '您在当前浏览器环境可以使用上次登录凭据自动登录系统', 
            value: '', 
            actions: { 
              title: '关闭', 
              callback: 'cleanAutoLoginInfo'
            } 
          });
        }
        return menus;
      }
    },
    data () {
      return {
        isEdit: false
      }
    },
    mounted () {
      this.$store.dispatch('account/fetchUserInfo');
    },
    methods: {
      invoke (methodName) {
        this[methodName]();
      },
      editPassword () {
        this.isEdit = true;
      },
      cleanAutoLoginInfo () {
        this.$confirm({
          title: '提示',
          content: `确认关闭吗?`,
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            AuthAPIS.cleanAutoLoginInfo()
              .then(resp => {
                this.$store.dispatch('account/fetchUserInfo');
              })
              .catch(resp => {
                this.$message.error(resp.message);
              });
          },
          onCancel: () => {
            // ...
          }
        });
      },
      onPasswordEditorClose (rdata) {
        if(rdata.$hook) {
          this.$success({
            title: '提示',
            content: `密码修改成功，点击确认注销当前登录状态?`,
            okText: '确认',
            onOk: () => {
              this.logout();
            },
            onCancel: () => {
              this.logout();
            }
          });
        }
      },
      logout () {
        this.$router.push({ name: 'home' });
      }
    }
  });
});
</script>

<style scoped>

</style>
