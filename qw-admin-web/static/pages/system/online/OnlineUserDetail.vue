<template>
  <div>
    <a-modal 
      title="详情" 
      :visible="isVisible"
      :width="modalWidth"
      :body-style="modalBodyStyle"
      @cancel="handleCancel">
      <template slot="footer">
        <a-button 
          type="primary" 
          @click="handleOk">
          关闭
        </a-button>
      </template>

      <a-descriptions :column="3" size="small" bordered >
        <a-descriptions-item :span="3" label="登录名">{{ this.data.loginName }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="ip地址">{{ this.data.ip }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="登录地点">{{ this.data.loginRegion }}ms</a-descriptions-item>
        <a-descriptions-item :span="3" label="登录时间">{{ this.data.loginDate }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="登录类型">{{ this.data.loginType | loginTypeHumanized }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="登出时间">{{ this.data.logoutDate || '/' }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="登出类型">{{ this.data.logoutType | logoutTypeHumanized }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="登录系统">{{ this.data.osType }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="浏览器类型">{{ this.data.browserType }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="描述">{{ this.data.description }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="操作概述">{{ this.data.description }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/system/task',
  'mixins'
], function(TaskAPIS, MXS, resolve, reject) {
  resolve({
    name: 'OnlineUserDetail',
    mixins: [MXS.CommonMixin, MXS.ModalMixin, MXS.ValidatorMixin],
    data () {
      return {
        modalWidth: '1000px',
        modalBodyStyle: {
          width: '980px',
          border: 'none'
        },
      }
    },
    filters: {
      loginTypeHumanized (val) {
        if(!val) {
          return '/';
        }
        return val === 'login' ? '用户名/密码登录' 
          : val === 'auto-login' ? '自动登录'
          : val;
      },
      logoutTypeHumanized (val) {
        if(!val) {
          return '/';
        }
        return val === 'logout-api' ? '主动登出' 
          : val === 'session-expiration' ? '会话超时'
          : val;
      },
    },
    methods: {
      handleCancel () {
        this.close();
      },
      handleOk () {
        this.close();
      }
    }
  });
});
</script>

<style scoped>
.long-text-wrapper {
  max-width: 850px;
}
</style>
