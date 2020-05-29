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
        <a-descriptions-item :span="3" label="缓存名称">{{ this.data.name }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="JVM 缓存数量">{{ this.data.memoryStoreSize }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="Off Heap缓存数量">{{ this.data.offHeapStoreSize }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="硬盘缓存数量">{{ this.data.diskStoreSize }}ms</a-descriptions-item>
        <a-descriptions-item :span="3" label="JVM 缓存大小">{{ this.data.onMemoryStoreBytes }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="Off Heap缓存大小">{{ this.data.onOffHeapSizeBytes }}</a-descriptions-item>
        <a-descriptions-item :span="3" label="硬盘缓存大小">{{ this.data.onDiskStoreSizeBytes }}ms</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'mixins'
], function(MXS, resolve, reject) {
  resolve({
    name: 'CacheDetail',
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
