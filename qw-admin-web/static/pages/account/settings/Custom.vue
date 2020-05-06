<template>
  <a-list itemLayout="horizontal">
    <a-list-item>
      <a-list-item-meta>
        <a slot="title">风格配色</a>
        <span slot="description">
            整体风格配色设置
        </span>
      </a-list-item-meta>
      <div slot="actions">
        <a-switch 
          checked-children="暗色" 
          un-checked-children="白色" 
          :default-checked="navTheme === 'dark'" 
          @on-change="onChange" 
        >
        </a-switch>
      </div>
    </a-list-item>
    <a-list-item>
      <a-list-item-meta>
        <a slot="title">主题色</a>
        <span slot="description">
            页面风格配色： <a style="color: red" v-html="colorFilter(primaryColor)"></a>
        </span>
      </a-list-item-meta>
    </a-list-item>
  </a-list>
</template>

<script lang="es6">
// 个性化设置
module.exports = {
  name: 'Custom',
  data () {
    return {
      navTheme: 'dark',
      primaryColor: '红色'
    }
  },
  filters: {
    themeFilter (theme) {
      const themeMap = {
        'dark': '暗色',
        'light': '白色'
      };
      return themeMap[theme];
    }
  },
  methods: {
    colorFilter (color) {
      return color;
    },

    onChange (checked) {
      if (checked) {
        this.$message.info('dark theme');
      } else {
        this.$message.info('light theme')
      }
    }
  }
};
</script>

<style scoped>

</style>
