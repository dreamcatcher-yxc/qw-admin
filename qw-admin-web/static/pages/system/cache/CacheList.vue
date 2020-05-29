<template>
  <a-card>
    <div>
      <div class="operator">
        <a-button v-if="$$hasAuth('qw-cache-clear')" @click="clearCaches" type="danger">清空</a-button>
        <span style="float: right; margin-top: 3px;">
          <a-button @click="init()">刷新</a-button>
          <qw-column-selector 
            :src-columns="columns"
            @change="(selectedColumns) => this.replaceArrayByArray('selectedColumns', selectedColumns)">
          </qw-column-selector>
        </span>
        <div style="clear: both;"></div>
      </div>

      <qw-table
        :columns="selectedColumns"
        :data-source="dataSource"
        :selected-rows="selectedRows"
        :pagination="pagination"
        :loading="isLoading"
        :custom-row="customRow"
        row-key="no"
        @select-row-change="onSelectRowChange"
        @change="onChange"
      >
        <template v-slot:status="props">
          <a-icon
            :type="props.text === 'S' ? 'check-circle' : 'close-circle'"
            :style="{ fontSize: '18px', color: props.text === 'S' ? 'green' : 'red' }"
            />
        </template>
      </qw-table>

      <!-- <template v-if="isEdit">
        <cache-detail :show.sync="isEdit" :data="editData" @on-close="(data) => this.init(false)"></cache-detail>
      </template> -->
    </div>
  </a-card>
</template>

<script lang="es6">
const columns = [
  {
    title: '编号',
    dataIndex: 'no'
  },
  {
    title: '缓存名称',
    dataIndex: 'name'
  },
  {
    title: 'JVM 缓存数量',
    dataIndex: 'memoryStoreSize'
  },
  {
    title: 'Off Heap缓存数量',
    dataIndex: 'offHeapStoreSize',
  },
  {
    title: '硬盘缓存数量',
    dataIndex: 'diskStoreSize'
  },
  {
    title: 'JVM 缓存大小（单位：比特）',
    dataIndex: 'onMemoryStoreBytes'
  },
  {
    title: 'Off Heap缓存大小（单位：比特）',
    dataIndex: 'onOffHeapSizeBytes'
  },
  {
    title: '硬盘缓存大小（单位：比特）',
    dataIndex: 'onDiskStoreSizeBytes'
  }
];

module.exports = asyncRequire([
  'alias!@API/system/cacheManager',
  'mixins',
  'moment'
], function(CacheManagerAPIS, MXS, Moment, resolve, reject) {
  resolve({
    name: 'CacheList',
    mixins: [MXS.AuthMixin, MXS.CommonMixin],
    components: {
      QwTable: load('@CMP/table/QWTable'),
      QwColumnSelector: load('@CMP/table/QWTableColumnSelector')
      // CacheDetail: load('./CacheDetail')
    },
    data () {
      return {
        columns: columns,
        selectedColumns: [],
        dataSource: [],
        selectedRows: [],
        pagination: { 
          current: 1,
          pageSize: 10,
          total: 0,
          showQuickJumper: true,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '30', '40'],
          onShowSizeChange: (current, size) => {
            this.pagination.current = current;
            this.pagination.pageSize = size;
            this.init();
          }
        },
        isEdit: false
      }
    },
    mounted () {
      this.$$fetchAuth();
      this.init(true);
      this.__vm_inited__ = true;
    },
    methods: {
      moment () {
        return Moment(arguments); 
      },

      reopen() {
        this.init(true);
      },

      init(reset) {
        this.showLoading();
        return CacheManagerAPIS.queryMetas()
          .then((data) => {
            var metas = data.sdata.data;
            this.dataSource = [];
            metas.forEach((meta, i) => {
              meta.no = (this.pagination.current - 1) * 10 + i + 1;
              this.dataSource.push(meta);
            });
            this.pagination.total = metas.length;
            this.hideLoading();
            return data;
          });
      },

      onSelectRowChange (skeys, selectedRows) {
        this.selectedRows = selectedRows;
      },

      onChange(pagination, filter, sorter) {
        if(pagination.current === this.pagination.current) {
          return;
        }
        this.pagination.current = pagination.current;
        this.init(false);
      },

      showCacheDetail (record) {
        this.editData = record;
        this.isEdit = true;
      },

      clearCaches () {
        this.$confirm({
          title: '清空确认',
          content: `确定清空选中的缓存吗?`,
          okText: '确认',
          cancelText: '取消',
          onOk: () => {
            this.showLoading();
            return CacheManagerAPIS.clearCaches(this.selectedRows.map(item => item.name))
              .then((data) => {
                this.$message.success('操作成功');
                this.selectedRows = [];
                this.init(true);
              })
              .catch((data) => {
                this.$message.error(data.message);
              })
              .finally(() => this.hideLoading());
          },
          onCancel() {}
        });
      },

      customRow (record, index) {
        return {
          on: {
            click: () => {
              // this.showCacheDetail(record);
            }
          }
        }
      }
    }
  });
});
</script>

<style scoped>
.search{
  margin-bottom: 54px;
}
.fold{
  width: calc(100% - 216px);
  display: inline-block
}
.operator{
  margin-bottom: 18px;
}
@media screen and (max-width: 900px) {
  .fold {
    width: 100%;
  }
}
</style>
