<template>
  <page-layout :desc="desc" :title="$route | menuName" :link-list="linkList">
    <div slot="extra" class="extraImg">
      <img :src="extraImage"/>
    </div>
    <transition name="page-toggle">
      <keep-alive v-if="multipage">
        <router-view ref="page" />
      </keep-alive>
      <router-view v-else ref="page" />
    </transition>
  </page-layout>
</template>

<script lang="es6">
module.exports = asyncRequire([
  'mixins'
], function(MXS, resolve, reject) {
  resolve({
    name: 'PageView',
    components: {
      PageLayout : load('./PageLayout')
    },
    mixins: [ MXS.CommonMixin ],
    data () {
      return {
        title: '',
        desc: '',
        linkList: [],
        extraImage: ''
      }
    },
    computed: {
      multipage () {
        return this.$store.state.setting.multipage;
      }
    },
    mounted () {
      this.getPageHeaderInfo();
    },
    updated () {
      this.getPageHeaderInfo();
    },
    methods: {
      getPageHeaderInfo () {
        this.title = this.$route.name;
        const page = this.$refs.page;
        if (page) {
          this.desc = page.desc;
          this.linkList = page.linkList;
          this.extraImage = page.extraImage;
        }
      }
    }
  });
});
</script>

<style scoped>
.head-info {
  position: relative;
  text-align: center;
  padding: 0 32px;
}
.head-info span {
  color: rgba(0, 0, 0, 0.45);
  display: inline-block;
  font-size: 14px;
  line-height: 22px;
  margin-bottom: 4px;
}
.head-info p {
  color: rgba(0, 0, 0, 0.85);
  font-size: 24px;
  line-height: 32px;
  margin: 0;
}
.head-info em {
  background-color: #e8e8e8;
  position: absolute;
  height: 56px;
  width: 1px;
  top: 0;
  right: 0;
}
</style>