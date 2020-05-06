<template>
  <div
    :class="['step-item', link ? 'linkable' : null]"
    @click="go"
  >
    <span :style="titleStyle">{{title}}</span>
    <a-icon :style="iconStyle" :type="icon"></a-icon>
    <slot></slot>
  </div>
</template>

<script lang="es6">
  const cmp = new Promise((resolve, reject) => {
    const Group = {
      name: 'AStepItemGroup',
      render (h) {
        return h(
          'div',
          {attrs: {style: 'text-align: center; margin-top: 8px'}},
          [h('div', {attrs: {style: 'text-align: left; display: inline-block;'}}, [this.$slots.default])]
        )
      }
    };

    resolve({
      name: 'AStepItem',
      Group: Group,
      props: ['title', 'icon', 'link', 'titleStyle', 'iconStyle'],
      methods: {
        go () {
          const link = this.link;
          if (link) {
            this.$router.push(link);
          }
        }
      }
    });
  });

  module.exports = cmp;
  //# sourceURL= components/tool/AStepItem.vue
</script>

<style scoped>
  .step-item {
    cursor: pointer;
  }
  :global .ant-steps-item-process .linkable {
    color: #40a9ff;
  }
</style>
