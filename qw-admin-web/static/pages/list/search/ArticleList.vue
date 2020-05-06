<template>
  <div>
    <search-form></search-form>
    <a-card :bordered="false">
      <a-list item-layout="vertical">
        <a-list-item :key="n" v-for="n in 10">
          <a-list-item-meta title="Alipay">
            <div slot="description">
              <a-tag >Ant Design</a-tag>
              <a-tag >设计语言</a-tag>
              <a-tag >蚂蚁金服</a-tag>
            </div>
          </a-list-item-meta>
          <div class="content">
            <div class="detail">
              段落示意：蚂蚁金服设计平台 ant.design，用最小的工作量，无缝接入蚂蚁金服生态，提供跨越设计与开发的体验解决方案。蚂蚁金服设计平台
              ant.design，用最小的工作量，无缝接入蚂蚁金服生态，提供跨越设计与开发的体验解决方案。
            </div>
            <div class="author">
              <a-avatar size="small" src="https://gw.alipayobjects.com/zos/rmsportal/WdGqmHpayyMjiEhcKoVE.png"></a-avatar>
              <a>ICZER</a>发布在
              <a href="https://github.com/iczer">https://github.com/iczer</a>
              <em>2018-08-05 22:23</em>
            </div>
          </div>
          <span slot="actions"><a-icon style="margin-right: 8px" type="star-o"></a-icon> 156</span>
          <span slot="actions"><a-icon style="margin-right: 8px" type="like-o"></a-icon>1435</span>
          <span slot="actions"><a-icon style="margin-right: 8px" type="message"></a-icon>4</span>
        </a-list-item>
      </a-list>
    </a-card>
  </div>
</template>

<script lang="es6">
module.exports = {
  name: 'ArticleList',
  components: {
    SearchForm: load('./SearchForm')
  }
}
</script>

<style scoped>
  .extra {
    width: 272px;
    height: 1px;
  }
  .content .detail {
    line-height: 22px;
    max-width: 720px;
  }
  .content .author {
    color: rgba(0, 0, 0, 0.45);
    margin-top: 16px;
    line-height: 22px;
  }
  .content .author  > :global(.ant-avatar) {
    vertical-align: top;
    margin-right: 8px;
    width: 20px;
    height: 20px;
    position: relative;
    top: 1px;
  }
  .content .author  > em {
    color: rgba(0, 0, 0, 0.25);
    font-style: normal;
    margin-left: 16px;
  }
</style>
