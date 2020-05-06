<template>
  <div style="margin: -24px -24px 0px">
    <page-header :breadcrumb="breadcrumb" :title="title" :logo="logo" :avatar="avatar">
      <slot name="action"  slot="action"></slot>
      <slot slot="content" name="headerContent"></slot>
      <div slot="content" v-if="!this.$slots.headerContent && desc">
        <p style="font-size: 14px;line-height: 1.5;color: rgba(0,0,0,.65)">{{desc}}</p>
        <div class="link">
          <template  v-for="(link, index) in linkList">
            <a :key="index" :href="link.href"><a-icon :type="link.icon" />{{link.title}}</a>
          </template>
        </div>
      </div>
      <slot slot="extra" name="extra"></slot>
    </page-header>
    <div ref="page" :class="['page-content', layout]" >
      <slot ></slot>
    </div>
  </div>
</template>

<script lang="es6">
  var uniqueId = '';

  module.exports = {
    name: 'PageLayout',
    components: {
      PageHeader : load('@CMP/page/PageHeader')
    },
    props: ['desc', 'logo', 'title', 'avatar', 'linkList', 'extraImage'],
    data () {
      return {
        breadcrumb: []
      }
    },
    computed: {
      layout() {
        return this.$store.state.setting.layout;
      }
    },
    mounted () {
      this.getBreadcrumb();
    },
    updated () {
      this.getBreadcrumb();
    },
    methods: {
      getBreadcrumb () {
        let menu = this.$store.getters['share/getMenuNameByMenuKey'](this.$route.path);

        if(!menu) {
          this.breadcrumb = this.$route.matched;
          uniqueId = new Date().getTime();
        } else {
          let tArr = [];
          let tMenu = menu;
          let newUniqueId = '';
          do {
            tArr.splice(0, 0, {
              path: tMenu.path,
              meta: {
                menuName: tMenu.name
              }
            });
            newUniqueId += tMenu.key;
            tMenu = tMenu.parent;
          } while (tMenu);
          if(newUniqueId !== uniqueId) {
            uniqueId = newUniqueId;
            this.breadcrumb = tArr;
          }
        }
      }
    }
  };
</script>

<style scoped>
.link {
  margin-top: 16px;
  line-height: 24px;
}

.link a {
  font-size: 14px;
  margin-right: 32px;
}

.link a i {
  font-size: 22px;
  margin-right: 8px;
}

.page-content.side {
  margin: 5px 15px 0px;
}

.page-content.head {
  margin: 24px auto 0;
  max-width: 1400px;
}
</style>