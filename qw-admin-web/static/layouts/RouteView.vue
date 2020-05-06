<template>
  <transition name="page-toggle">
    <template v-if="multipage">
      <keep-alive>
        <router-view></router-view>
      </keep-alive>
    </template>
    <template v-else>
      <router-view></router-view>
    </template>    
  </transition>
</template>

<script lang="es6">
  module.exports = {
    name: 'RouteView',
    computed: {
      multipage () {
        return this.$store.state.setting.multipage;
      }
    }
  };
</script>

<style scoped>
</style>