<template>
  <div>
    <div :class="['mask', openDrawer ? 'open' : 'close']" @click="close"></div>
    <div :class="['drawer', placement, openDrawer ? 'open' : 'close']">
      <div ref="drawer" style="position: relative; width: 273px; height: 100%; background-color: #FFF;">
        <slot></slot>
      </div>
      <div v-if="showHandler" :class="['handler-container', placement]" ref="handler" @click="handle">
        <template v-if="$slots.handler">
          <slot name="handler"></slot>
        </template>
        <template v-else>
          <div class="handler">
            <a-icon :type="openDrawer ? 'close'  : 'bars'"></a-icon>
          </div>
        </template>  
      </div>
    </div>
  </div>
</template>

<script lang="es6">
module.exports = Promise.resolve({
  name: 'Drawer',
  data () {
    return {
      drawerWidth: 0
    }
  },
  props: {
    openDrawer: {
      type: Boolean,
      required: false,
      default: false
    },
    placement: {
      type: String,
      required: false,
      default: 'left'
    },
    showHandler: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  watch: {
    'drawerWidth': function (val) {
      if (this.placement === 'left') {
        this.$refs.handler.style.left = val + 'px'
      } else {
        this.$refs.handler.style.right = val + 'px'
      }
    }
  },
  computed: {
    height () {
      return this.$store.state.screen.height + 'px';
    }
  },
  mounted () {
    this.drawerWidth = this.getDrawerWidth()
  },
  methods: {
    open () {
      this.$emit('change', true)
    },
    close () {
      this.$emit('change', false)
    },
    handle () {
      this.$emit('change', !this.openDrawer)
    },
    getDrawerWidth () {
      return this.$refs.drawer.clientWidth
    }
  }
});
</script>

<style scoped>
  .mask {
    position: fixed;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.2);
    transition: all 0.5s;
    z-index: 100;
  }
  .mask.open {
    display: inline-block;
  }
  .mask.close {
    display: none;
  }
  .drawer {
    position: fixed;
    height: 100%;
    transition: all 0.5s;
    z-index: 110;
  }
  .drawer.left {
    left: 0px;
  }
  .drawer.left.open {
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  }
  .drawer.left.close {
    transform: translatex(-100%);
  }
  .drawer.right {
    right: 0px;
  }
  .drawer.right.open {
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
  }
  .drawer.right.close {
    transform: translatex(100%);
  }
  .drawer .sider {
    height: 100%;
  }
  .handler-container {
    position: fixed;
    top: 200px;
    text-align: center;
    transition: all 0.5s;
    cursor: pointer;
  }
  .handler-container .handler {
    height: 40px;
    width: 40px;
    background-color: #fff;
    z-index: 100;
    font-size: 26px;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
    line-height: 40px;
  }
  .handler-container.left .handler {
    border-radius: 0 5px 5px 0;
  }
  .handler-container.right .handler {
    border-radius: 5px 0 0 5px;
  }
</style>
