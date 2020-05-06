/**
 * 此插件用于加载 ES6 模块.
 */
define(['es6-parser', 'jquery'], function (es6Parser, $) {
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
            // 添加默认后缀为 js
            if(name.lastIndexOf('.') !== name.length) {
                name = name + '.js';
            }

            var esScriptUrl;

            if(name.indexOf('https://') === 0
               || name.indexOf('http://') === 0
                || name.indexOf('//') === 0
                || name.indexOf('/') === 0) {
                esScriptUrl = name;
            } else {
                var baseUrl = config.baseUrl;

                if(baseUrl.lastIndexOf('/') === baseUrl.length - 1) {
                    baseUrl = baseUrl.substring(0, baseUrl.length - 1);
                }

                esScriptUrl = baseUrl + '/' + name;
            }

            $.getScript(esScriptUrl, function (response, status) {
                if(status === 'success') {
                    var es5Script = es6Parser.convert(response);
                    onload.fromText(es5Script);
                } else {
                    console.error(status);
                }
            })
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