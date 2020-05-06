<template>
  <a-popover trigger="click" placement="bottomRight">
    <template slot="content">
      <a-spin :spinning="loadding">
        <a-tabs>
          <a-tab-pane tab="通知" key="1">
            <a-list>
              <!-- <a-list-item>
                <a-list-item-meta title="你收到了 14 份新周报" description="一年前">
                  <a-avatar style="background-color: white" slot="avatar" src="https://gw.alipayobjects.com/zos/rmsportal/ThXAXghbEsBCCSDihZxY.png"/>
                </a-list-item-meta>
              </a-list-item> -->
            </a-list>
          </a-tab-pane>
        </a-tabs>
      </a-spin>
    </template>
    <span @click="fetchNotice" class="header-notice">
      <a-badge count="0">
        <a-icon :class="['header-notice-icon', theme]" type="bell" />
      </a-badge>
    </span>
  </a-popover>
</template>

<script lang="es6">
  module.exports = {
    name: 'HeaderNotice',
    data () {
      return {
        loadding: false
      }
    },
    computed: {
      theme () {
        return this.$store.state.setting.layout === 'side' ? 'light' : this.$store.state.setting.theme
      }
    },
    methods: {
      fetchNotice () {
        if (this.loadding) {
          this.loadding = false
          return
        }
        this.loadding = true
        setTimeout(() => {
          this.loadding = false
        }, 1000)
      }
    }
  };
</script>

<style>
  .header-notice {
    display: inline-block;
    transition: all 0.3s;
  }
  .header-notice span {
    vertical-align: initial;
  }
  .header-notice .header-notice-icon {
    font-size: 16px;
    padding: 4px;
  }
  .header-notice .header-notice-icon.dark {
    color: #fff;
  }
  .header-notice .header-notice-icon.light {
    color: rgba(0, 0, 0, 0.65);
  }
</style>