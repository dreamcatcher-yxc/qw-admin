<template>
  <a-layout-sider class="sider" width="273">
    <setting-item title="整体风格设置">
      <img-checkbox-group @change="setTheme">
        <img-checkbox img="https://gw.alipayobjects.com/zos/rmsportal/LCkqqYNmvBEbokSDscrm.svg" :checked="true" value="dark"></img-checkbox>
        <img-checkbox img="https://gw.alipayobjects.com/zos/rmsportal/jpRkZQMyYRryryPNtyIC.svg" value="light"></img-checkbox>
      </img-checkbox-group>
    </setting-item>
    <setting-item title="主题色">
      <color-checkbox-group @change="onColorChange" :default-values="[]" :multiple="false">
        <color-checkbox ref="colorNode" color="rgb(245, 34, 45)" value="1"></color-checkbox>
        <color-checkbox color="rgb(250, 84, 28)" value="2"></color-checkbox>
        <color-checkbox color="rgb(250, 173, 20)" value="3"></color-checkbox>
        <color-checkbox color="rgb(19, 194, 194)" value="4"></color-checkbox>
        <color-checkbox color="rgb(82, 196, 26)" value="5"></color-checkbox>
        <color-checkbox color="rgb(24, 144, 255)" value="6"></color-checkbox>
        <color-checkbox color="rgb(47, 84, 235)" value="7"></color-checkbox>
        <color-checkbox color="rgb(114, 46, 209)" value="8"></color-checkbox>
      </color-checkbox-group>
    </setting-item>
    <a-divider/>
    <setting-item title="导航设置">
      <img-checkbox-group @change="setLayout">
        <img-checkbox img="https://gw.alipayobjects.com/zos/rmsportal/JopDzEhOqwOjeNTXkoje.svg" :checked="true" value="side"></img-checkbox>
        <img-checkbox img="https://gw.alipayobjects.com/zos/rmsportal/KDNDBbriJhLwuqMoxcAr.svg" value="head"></img-checkbox>
      </img-checkbox-group>
    </setting-item>
    <setting-item>
      <a-list :split="false">
        <a-list-item>
          栅格模式
          <a-select size="small" default-value="1" slot="actions" style="width: 80px">
            <a-select-option value="1">流式</a-select-option>
            <a-select-option value="2">定宽</a-select-option>
          </a-select>
        </a-list-item>
        <a-list-item>
          固定Header
          <a-switch slot="actions" size="small" :checked="headerFixed" @change="setHeaderFixed"></a-switch>
        </a-list-item>
        <a-list-item>
          下滑时隐藏侧边栏
          <a-switch slot="actions" size="small" :checked="autoHideFixedHeader" :disabled="!headerFixed" @change="setAutoHideFixedHeader"></a-switch>
        </a-list-item>
        <a-list-item>
          固定Siderbar
          <a-switch slot="actions" size="small" :disabled="true"></a-switch>
        </a-list-item>
      </a-list>
    </setting-item>
    <a-divider />
    <setting-item title="其他设置">
      <a-list :split="false">
        <a-list-item>
          色弱模式
          <a-switch slot="actions" size="small" @change="setColorWeak"></a-switch>
        </a-list-item>
        <!-- <a-list-item>
          显示抽屉按钮
          <a-switch slot="actions" size="small"></a-switch>
        </a-list-item> -->
        <a-list-item>
          多页签模式
          <a-switch :checked="multipage" slot="actions" size="small" @change="setMultipage"></a-switch>
        </a-list-item>
      </a-list>
    </setting-item>
    <a-divider></a-divider>
    <a-button id="copyBtn" data-clipboard-text="Sorry, you copy nothing O(∩_∩)O~" @click="copyCode" style="width: 100%" icon="copy" >拷贝代码</a-button>
  </a-layout-sider>
</template>

<script lang="es6">
  const ImgCheckbox = load('@CMP/checkbox/ImgCheckbox'),
        ColorCheckbox = load('@CMP/checkbox/ColorCheckbox'),
        StyleItem = load('@CMP/setting/StyleItem'),
        SettingItem = load('@CMP/setting/SettingItem');

  const ImgCheckboxGroup = () => ImgCheckbox().then(cmp => cmp.Group);
  const ColorCheckboxGroup = () => ColorCheckbox().then(cmp => cmp.Group);

  module.exports = {
    name: 'Setting',
    components : {
      ImgCheckbox, 
      ColorCheckbox, 
      ImgCheckboxGroup, 
      ColorCheckboxGroup, 
      StyleItem,
      SettingItem
    },
    computed: {
      multipage () {
        return this.$store.state.setting.multipage;
      },
      headerFixed () {
        return this.$store.state.setting.headerFixed;
      },
      autoHideFixedHeader () {
        return this.$store.state.setting.autoHideFixedHeader;
      }
    },
    methods: {
      onColorChange (values, colors) {
        if (colors.length > 0) {
          this.$message.info(`您选择了主题色 ${colors}`);
        }
      },
      setTheme (values) {
        this.$store.commit('setting/setTheme', values[0]);
      },
      setLayout (values) {
        this.$store.commit('setting/setLayout', values[0]);
      },
      copyCode () {
        // let clipboard = new Clipboard('#copyBtn')
        // const _this = this
        // clipboard.on('success', function () {
        //   _this.$message.success(`复制成功`)
        //   clipboard.destroy()
        // })
      },
      setMultipage (checked) {
        this.$store.commit('setting/setMultipage', checked);
      },
      setColorWeak (checked) {
        if(checked) {
          document.querySelector('body').setAttribute('class', 'color-weak');
        } else {
          document.querySelector('body').removeAttribute('class');
        }
      },
      setHeaderFixed (fixed) {
        this.$store.commit('setting/setHeaderFixed', fixed);
      },
      setAutoHideFixedHeader (autoHide) {
        this.$store.commit('setting/setAutoHideFixedHeader', autoHide);
      }
    }
  }
</script>

<style scoped>
  .sider {
    background-color: #fff;
    height: 100%;
    padding: 24px;
    font-size: 14px;
    line-height: 1.5;
    word-wrap: break-word;
    position: relative;
  }
  .sider .flex {
    display: flex;
  }
</style>