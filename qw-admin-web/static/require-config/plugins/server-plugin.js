/**
 * 此插件用于加载服务器端配置至客户端, 如服务器端当前环境.
 */
define(['jquery'], function ($, R) {
    'use strict';

    return {
        /**
         * 必须定义
         * @param name 模块名称
         * @param req 提供 toUrl 方法, 根据 name, 计算该模块资源的全路径.
         * @param onload 自 2.1.0 之后, 提供了 onload.fromText() 将文本转换为 js 代码执行.
         * @param config 全局配置信息
         */
        load: function (name, req, onload, config) {
            var serverResourcesUrl = '/admin/rest/' + name;

            $.getJSON(serverResourcesUrl, function (resp) {
                R.init(resp)
                    .ifOk(function () {
                        onload(this.result.body);
                    });
            });
        },

        /**
         * 该方法不是必须的, 如果定义了, 则会在 load 方法之前调用
         * @param name 模块名称
         * @param normalize 规范会名称
         * @returns {string}
         */
        normalize: function (name, normalize) {
            return name;
        },
    }
});