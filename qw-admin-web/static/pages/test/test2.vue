<template>
  <div>
    <div class="wrapper">
      <a-descriptions title="昨日天气">
        <a-descriptions-item label="日期">
          {{ yesterday.date }}
        </a-descriptions-item>
        <a-descriptions-item label="最高温度">
          {{ yesterday.high }}
        </a-descriptions-item>
        <a-descriptions-item label="风向">
          {{ yesterday.fx }}
        </a-descriptions-item>
        <a-descriptions-item label="最低温度">
          {{ yesterday.low }}
        </a-descriptions-item>
        <a-descriptions-item label="风力">
          {{ yesterday.fl | removeCDATA }}
        </a-descriptions-item>
        <a-descriptions-item label="天气">
          {{ yesterday.type }}
        </a-descriptions-item>
      </a-descriptions>
    <div>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'alias!@API/test/index',
  'lodash'
], (TestAPI, _, resolve, reject) => {
  resolve({
    name: 'test2-page',
    data () {
      return {
        city: '昆明',
        forecast: [],
        wendu: 0,
        ganmao: '',
        yesterday: {
          date: '',
          fl: '',
          fx: '',
          high: '',
          low: '',
          type: ''
        }
      }
    },
    filters: {
      removeCDATA (str) {
        let reg = /^\<\!\[CDATA\[([^\]]+)\]\]\>$/;
        if(reg.test(str)) {
          return reg.exec(str)[1];
        }
        return str;
      }
    },
    mounted () {
      this.fetchWeathData();
    },
    methods: {
      fetchWeathData () {
        TestAPI.queryWeather(this.city)
          .then(resp => {
            if(resp.status === 200 && resp.data.status === 1000) {
              return resp.data.data;
            }
            return Promise.reject(resp.desc);
          })
          .then(data => {
            // es6 解析复制不能用, babel 插件解析不了...
            this.forecast = [];
            data.forecast.forEach(item => this.forecast.push(item));
            this.wendu = data.wendu;
            this.ganmao = data.ganmao;
            this.$set(this, 'yesterday', data.yesterday);
          })
          .catch(errMsg => this.$message.error(errMsg));
      }
    }
  });
});
</script>
<style scoped>
div.wrapper {
  padding: 5px 15px;
  background-color: #FFF;
}
</style>