<template>
  <div>
    <search-form></search-form>
    <a-list
      :grid='{ gutter: 24, xl: 4, lg: 3, md: 3, sm: 2, xs: 1 }'
    >
      <a-list-item :key="n" v-for="n in 8" style="padding: 0 12px">
        <a-card hoverable>
          <img slot="cover" src="https://gw.alipayobjects.com/zos/rmsportal/iZBVOIhGJiAnhplqjvZW.png" height="154">
          <a-card-meta title="Ant Design">
            <div slot="description">
              城镇中有那么多的酒馆，她却偏偏走进了我的酒馆
            </div>
          </a-card-meta>
          <div class="content">
            <span>4小时前</span>
            <avatar-list>
              <avatar-list-item size="small" tips="曲丽丽" src="https://gw.alipayobjects.com/zos/rmsportal/ZiESqWwCXBRQoaPONSJe.png"></avatar-list-item>
              <avatar-list-item size="small" tips="周星星" src="https://gw.alipayobjects.com/zos/rmsportal/tBOxZPlITHqwlGjsJWaF.png"></avatar-list-item>
              <avatar-list-item size="small" tips="董娜娜" src="https://gw.alipayobjects.com/zos/rmsportal/sBxjgqiuHMGRkIjqlQCd.png"></avatar-list-item>
            </avatar-list>
          </div>
        </a-card>
      </a-list-item>
    </a-list>
  </div>
</template>

<script lang="es6">
  const SearchForm = load('./SearchForm');
  const AvatarList = load('@CMP/tool/AvatarList');
  const AvatarListItem = () => AvatarList().then(cmp => cmp.Item);

  module.exports = {
    name: 'ProjectList',
    components: {
      SearchForm,
      AvatarList, 
      AvatarListItem
    }
  };
</script>

<style scoped>
  .content {
    display: flex;
    margin-top: 16px;
    margin-bottom: -4px;
    line-height: 20px;
    height: 20px;
  }
  .content  > span {
    color: rgba(0, 0, 0, 0.45);
    flex: 1;
    font-size: 12px;
  }
  .content .avatarList {
    flex: 0 1 auto;
  }
</style>
