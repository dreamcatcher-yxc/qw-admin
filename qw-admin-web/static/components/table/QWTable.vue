<template>
  <div class="standard-table">
    <div class="alert">
      <a-alert type="info" :show-icon="true">
        <div slot="message">
          已选择&nbsp;<a style="font-weight: 600">{{selectedRows.length}}</a>&nbsp;项&nbsp;&nbsp;
          <template  v-for="(item, index) in needTotalList" v-if="item.needTotal">
            {{item.title}}总计&nbsp;
            <a :key="index" style="font-weight: 600">
            {{item.customRender ? item.customRender(item.total) : item.total}}
            </a>&nbsp;&nbsp;
          </template>
        </div>
      </a-alert>
    </div>
    <a-table
      :bordered="bordered"
      :loading="loading"
      :columns="columns"
      :data-source="dataSource"
      :row-key="rowKey"
      :pagination="qwTablePagination"
      :row-selection="{selectedRowKeys: selectedRowKeys, onChange: updateSelect, type: computedRowSelectionType}"
      :custom-row="customRow"
      @change="change"
    >
      <template v-for="slot in parentScopeSlots" v-slot:[slot.key]="text, record, index">
        <custom-render-slot 
          :render-wrapper="{render : slot.value}"
          :text="text"
          :record="record"
          :index="index"
        >
        </custom-render-slot>
      </template>
    </a-table>
  </div>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'lodash'
], function(_, resolve, reject) {
  resolve({
    name: 'QWTable',
    components: {
      'custom-render-slot': {
          name: 'CustomRenderSlot',
          functional: true,
          props: {
              renderWrapper: {
                  type: Object,
                  required: true
              },
              text: {
                type: [Object,String,Number, Boolean],
                required: true
              },
              record: {
                  type: Object,
                  required: true
              },
              index: {
                  type: Number,
                  required: true
              }
          },

          render: (h, ctx) => {
              // console.log('作用域插槽开始渲染...');
              return h('div', ctx.props.renderWrapper.render({
                  text: ctx.props.text,
                  record: ctx.props.record,
                  index: ctx.props.index
              }));
          }
      }
    },
    props: ['bordered', 'loading', 'columns', 'dataSource', 'rowKey', 'pagination', 'selectedRows', 'customRow', 'rowSelectionType'],
    data () {
      return {
        parentScopeSlots: [],
        needTotalList: []
      }
    },
    computed: {
      selectedRowKeys () {
        return this.selectedRows.map(item => item[this.rowKey]);
      },
      computedRowSelectionType () {
        if(!!this.rowSelectionType) {
          return this.rowSelectionType;
        }
        return 'checkbox';
      },
      qwTablePagination (pagination) {
        if('showTotal' in this.pagination) {
          return this.pagination;
        }
        let newPagination = _.cloneDeep(this.pagination);
        newPagination.showTotal =  (total, range) => `${range[0]}-${range[1]} 项，共 ${total} 条`;
        return newPagination;
      }
    },
    mounted () {
      this.init();
    },
    methods: {
      init () {
        this.parentScopeSlots = Object.keys(this.$scopedSlots)
          .filter(slotName => typeof this.$scopedSlots[slotName] === 'function')
          .map(slotName => {
              return { key : slotName, value : this.$scopedSlots[slotName] };
          });
      },

      updateSelect (selectedRowKeys, selectedRows) {
        let list = this.needTotalList
        this.needTotalList = list.map(item => {
          var res = {};
          this.jQuery.extend(true, res, item, {
            total: selectedRows.reduce((sum, val) => {
              return sum + val[item.dataIndex]
            }, 0)
          });
          return res;
        });
        this.$emit('select-row-change', this.selectedRowKeys, selectedRows);
      },

      change (pagination, filters, sorter) {
        this.$emit('change', pagination, filters, sorter);
      },

      initTotalList (columns) {
        const totalList = []
        columns.forEach(column => {
          if (column.needTotal) {
            var newCol = {};
            this.jQuery.extend(true, newCol, column, { total : 0 });
            totalList.push(newCol);
          }
        })
        return totalList
      }
    },
    created () {
      this.needTotalList = this.initTotalList(this.columns);
    },
    watch: {
      'selectedRows': function (selectedRows) {
        this.needTotalList = this.needTotalList.map(item => {
          var res = {};
          this.jQuery.extend(true, res, item, {
            total: selectedRows.reduce((sum, val) => {
              return sum + val[item.dataIndex];
            }, 0)
          });
          return res;
        })
      }
    }
  });
});
</script>

<style scoped>
  .alert{
    margin-bottom: 16px;
  }
</style>
