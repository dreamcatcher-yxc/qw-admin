<template>
  <div class="avatar-list">
    <ul>
      <slot>
      </slot>
    </ul>
  </div>
</template>

<script lang="es6">
  const cmp = new Promise((resolve, reject) => {
    require([
      'antd'
    ], function(antd) {
      const AAvatar = antd.Avatar;
      const ATooltip = antd.Tooltip;
      const Item = {
        name: 'AvatarListItem',
        props: {
          size: {
            type: String,
            required: false,
            default: 'small'
          },
          src: {
            type: String,
            required: true
          },
          tips: {
            type: String,
            required: false
          }
        },
        methods: {
          renderAvatar (h, size, src) {
            return h(AAvatar, {props: {size: size, src: src}}, [])
          }
        },
        render (h) {
          const avatar = this.renderAvatar(h, this.$props.size, this.$props.src)
          return h(
            'li',
            {class: 'avatar-item'},
            [this.$props.tips ? h(ATooltip, {props: {title: this.$props.tips}}, [avatar]) : avatar]
          )
        }
      };

      resolve({
        name: 'AvatarList',
        Item: Item
      });
    });
  });

  module.exports = cmp;
  //# sourceURL= components/tool/AvatarList.vue
</script>

<style>
  .avatar-list {
    display: inline-block;
  }
  .avatar-list ul {
    display: inline-block;
    margin-left: 8px;
    font-size: 0;
  }
  .avatar-list ul .avatar-item {
    display: inline-block;
    font-size: 14px;
    margin-left: -8px;
    width: 20px;
    height: 20px;
  }
  .avatar-list ul .avatar-item .ant-avatar {
    border: 1px solid #fff;
    width: 20px;
    height: 20px;
    cursor: pointer;
  }
</style>
