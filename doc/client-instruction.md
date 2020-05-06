## 1、简介

Vue 作为目前前端炙手可热的前端框架，既可以直接内嵌在网页中实现我们网站的一小部分功能，简化我们的开发！也可以配合打包工具、vue 全家桶构建大型的单页应用。首先得指出，个人觉得 Vue SPA 的最佳实践是配合配合打包工具，本项目并非挑战此最佳实践，而是尝试 Requirejs + Vue + Vuex + Vue-router + axios + ant.design 构建纯前端实现 SPA 的可能性。

项目灵感起源于 `http-vue-loader`, 一个可以前端直接加载 .vue 文件的库, 看起来很神奇，于是笔者去研究了它的源码，其原理不是很复杂，却让笔者萌生了直接使用 `http-vue-loader` 构建一个纯前端 SPA 的想法。当然，在落地的过程中遇到了很多坑，如果不懂  `http-vue-loader` 原理，这个项目你是不可能实现的，因此，本项目的 `http-vue-loader` 已经被笔者优化了，其中包括: style 样式局部化优化（很重要），script 模块加载优化（重要），template 优化（配合 style 优化），网络加载器缓存控制优化。

> 本项目基于 [vue-antd-admin](https://gitee.com/iczer/vue-antd-admin) 改造，.vue 文件相似度 80% 以上，但不兼容，毕竟是纯前端的版本，CMD 方式的模板加载是不支持的。

## 2、预览地址

[require-vue-admin](http://dreamcatcher_yxc.gitee.io/require-vue-admin/index.html)

## 3、项目特色

- vue 文件几乎和 CMD 方式一致
- vue 组件支持异步组件、非异步组件(本项目中未用到)
- 支持别名加载依赖，加载组件（很重要），eg:
  - @ 根路径
  - @CMP components 路径
  - @IMG 图片路径
  - 更多请查看 ~/static/require-config/modules/config.js 配置
- 支持局部样式（很重要）
- vue 文件支持 ES6 语法
- 支持 development、production 环境
- 缓存支持（生产环境更新缓存只需要修改全局 version 即可更新）（很重要）
- 支持热更新
- 权限系统

## 4、注意事项

 项目中有许多依赖依赖于 cdn，本地构建服务访问请确保网络畅通。

## 5、最后的话

代码中的重要模块 `http-vue-loader` 已经添加注释，在阅读理解源码之前，建议掌握如下知识点：

- HTML/JAVASCRIPT/CSS3 中级编程知识
- 熟悉 Vue.js，并且对其异步组件加载原理有大概的认识
- 对 Vue 组件 template 中使用 html 模板的注意事项(具体可以参看 Vue 官方文档说明)
- 熟悉 Promise 编程
- 什么是 AMD、CMD 模块化

> 项目不支持 IE，如果有问题可留言笔者。