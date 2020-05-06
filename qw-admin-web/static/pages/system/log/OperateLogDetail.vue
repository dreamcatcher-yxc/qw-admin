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
        <a-descriptions-item :span="1" label="操作者">{{ this.data.operatorName }}</a-descriptions-item>
        <a-descriptions-item :span="1" label="操作结果">{{ this.data.result | statusHumanized }}</a-descriptions-item>
        <a-descriptions-item :span="1" label="耗时">{{ this.data.duration }}ms</a-descriptions-item>
        <a-descriptions-item :span="3" label="操作时间">{{ this.data.operateTime }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="操作方法">{{ this.data.operateMethod }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="操作内容">{{ this.data.operation }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="接收参数">
          <div class="long-text-wrapper">{{ this.data.operateParam }}</div>
        </a-descriptions-item>
        <a-descriptions-item :span="3" label="操作概述">{{ this.data.message }}</a-descriptions-item>
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
    name: 'OperateLogDetail',
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
      statusHumanized (val) {
        return val === 'S' ? '成功' : '失败';
      }
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
