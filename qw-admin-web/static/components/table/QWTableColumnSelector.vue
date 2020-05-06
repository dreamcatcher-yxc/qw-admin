<template>
  <a-dropdown
    ref="dropdown"
    :visible="show"
    :trigger="['click']">
    <template slot="overlay">
      <div style="background-color: #FFF; border: 1px solid #EEE; border-radius: 3px;">
        <a-menu @click="handleMenuClick">
          <a-menu-item v-for="item in menus" :key="item.key">
            <a-checkbox v-model="item.checked" @change="selectChange">{{ item.value }}</a-checkbox>
          </a-menu-item>
        </a-menu>
        <!-- <div>
          <a-button type="link" @click="handleOk">确定</a-button>
          <a-button type="link" @click="handleCancel">取消</a-button>
        </div> -->
      </div>
    </template>  
    <a-button
      style="margin-left: 8px"
      @click="e => show = !show"
      >
      筛选列 <a-icon :type="show ? 'caret-up' : 'caret-down'" />
    </a-button>
  </a-dropdown>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'jquery'
], function(jQuery, resolve, reject) {
  resolve({
    name: 'QWTableColumnSelector',
    props: {
      srcColumns: {
        type: Array,
        required: true
      }
    },
    data () {
      return {
        show: false,
        menus: []
      }
    },
    mounted () {
      jQuery(window.document).on('click', (e) => {
        let $target = jQuery(e.target);

        if($target.hasClass('ant-dropdown-trigger') ||
          $target.parents('button.ant-dropdown-trigger').length ||
          $target.hasClass('ant-dropdown') ||
          $target.parents('div.ant-dropdown').length) {
            // ...
        } else {
          this.show = false;
        }
      });
      this.init();
    },
    methods: {
      init () {
        this.menus = [];
        this.srcColumns.forEach(col => {
          this.menus.push({
            key: col.dataIndex,
            value: col.title,
            checked: 'show' in col ? col.show : true
          });
        });
        this.generateAndReplaceNewColumns();
        this.$emit('change', this.newColumns);
      },

      generateAndReplaceNewColumns () {
        this.newColumns = [];
        this.menus.forEach((item, index) => {
          if(item.checked) {
            this.newColumns.push(this.srcColumns[index]);
          }
        });
      },

      handleMenuClick(e) {
        // ...
      },

      selectChange () {
        this.generateAndReplaceNewColumns();
        this.$emit('change', this.newColumns);
      }
    },

    destroyed () {
      jQuery(window.document).unbind('click');
    }
  });
});
</script>

<style scoped>
  .ant-menu > .ant-menu-item:not(:last-child) {
    margin-bottom: initial !important;
  }
  .alert{
    margin-bottom: 16px;
  }
</style>
