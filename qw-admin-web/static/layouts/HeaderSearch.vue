<template>
  <span class="header-search">
    <a-icon type="search" class="search-icon" @click="enterSearchMode"></a-icon>
    <a-auto-complete
      ref="input"
      :data-source="dataSource"
      :class="['search-input', searchMode ? 'enter' : 'leave']"
      placeholder="站内搜索"
      @blur="leaveSearchMode"
    >
    </a-auto-complete>
  </span>
</template>

<script lang="es6">
  module.exports = {
    name: 'HeaderSearch',
    data () {
      return {
        dataSource: ['上周统计', '近一月统计', '近半年统计'],
        searchMode: false
      }
    },
    methods: {
      enterSearchMode () {
        this.searchMode = true;
        setTimeout(() => this.$refs.input.focus(), 300);
      },
      leaveSearchMode () {
        this.searchMode = false;
      }
    }
  };
</script>

<style>
  .header-search .search-icon {
    font-size: 16px;
    cursor: pointer;
  }
  .header-search .search-input {
    border: 0;
    border-bottom: 1px rgba(3, 5, 6, 0.23) solid;
    transition: width 0.3s ease-in-out;
  }
  .header-search .search-input input {
    border: 0;
    box-shadow: 0 0 0 0;
  }
  .header-search .search-input.leave {
    width: 0px;
  }
  .header-search .search-input.leave input {
    display: none;
  }
  .header-search .search-input.enter {
    width: 200px;
  }
  .header-search .search-input.enter input:focus {
    box-shadow: 0 0 0 0;
  }
</style>