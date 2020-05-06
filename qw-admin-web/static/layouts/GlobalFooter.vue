<template>
  <div class="footer">
    <div class="links">
      <a class="a" target="_blank" :key="index" :href="item.link ? item.link : 'javascript: void(0)'" v-for="(item, index) in linkList">
          <span>
            <a-icon v-if="item.icon" :type="item.icon"/>
          </span>
          <span class="foo">{{item.name}}</span>
      </a>
    </div>
    <div class="copyright">
      Copyright 
      <span>
        <a-icon type="copyright" />
      </span>
      {{copyright}}
    </div>
  </div>
</template>

<script lang="es6">
    module.exports = {
      name: 'GlobalFooter',
      props: ['copyright', 'linkList']
    };
</script>

<style scoped>
.footer {
    padding: 0 16px;
    margin: 48px 0 24px;
    text-align: center;
}

.footer .copyright{
  color: rgba(0,0,0,.45);
  font-size: 14px;
}

.footer .links {
  margin-bottom: 8px;
}

.footer .links .a:not(:last-child) {
    margin-right: 40px !important;
}

.footer .links .a {
    color: rgba(0,0,0,.45);
    -webkit-transition: all .3s;
    transition: all .3s;
}
</style>