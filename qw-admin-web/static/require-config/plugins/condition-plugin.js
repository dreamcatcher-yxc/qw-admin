/**
 * 按需加载依赖库
 */
define(['jquery', 'config'], function ($, process) {
    'use strict';

    var methods = {
        // 浏览器环境加载
        explorer : function(meta, condition, trueLoadModuleId, falseLoadModuleId) {
            var currentExplorerInfo = (function () {
                var explorer = window.navigator.userAgent;

                if (explorer.indexOf("QQBrowser") >= 0 || explorer.indexOf("QQ") >= 0) {
                    return  "腾讯QQ";
                } else if (explorer.indexOf("Safari") >= 0 && explorer.indexOf("MetaSr") >= 0) {
                    return "搜狗";
                } else if (!!window.ActiveXObject || "ActiveXObject" in window) {//IE
                    if (!window.XMLHttpRequest) {
                        return "IE6";
                    } else if (window.XMLHttpRequest && !document.documentMode) {
                        return "IE7";
                    } else if (!-[1,] && document.documentMode && !("msDoNotTrack" in window.navigator)) {
                        return "IE8";
                    } else {//IE9 10 11
                        var hasStrictMode = (function () {
                            return this === undefined;
                        }());
                        if (hasStrictMode) {
                            if (!!window.attachEvent) {
                                return "IE10";
                            } else {
                                return "IE11";
                            }
                        } else {
                            return "IE9";
                        }
                    }
                } else {//非IE
                    if (explorer.indexOf("LBBROWSER") >= 0) {
                        return "猎豹";
                    } else if (explorer.indexOf("360ee") >= 0) {
                        return "360极速浏览器";
                    } else if (explorer.indexOf("360se") >= 0) {
                        return "360安全浏览器";
                    } else if (explorer.indexOf("se") >= 0) {
                        return "搜狗浏览器";
                    } else if (explorer.indexOf("aoyou") >= 0) {
                        return "遨游浏览器";
                    } else if (explorer.indexOf("qqbrowser") >= 0) {
                        return "QQ浏览器";
                    } else if (explorer.indexOf("baidu") >= 0) {
                        return "百度浏览器";
                    } else if (explorer.indexOf("Firefox") >= 0) {
                        return "火狐";
                    } else if (explorer.indexOf("Maxthon") >= 0) {
                        return "遨游";
                    } else if (explorer.indexOf("Chrome") >= 0) {
                        return "谷歌（或360伪装）";
                    } else if (explorer.indexOf("Opera") >= 0) {
                        return "欧朋";
                    } else if (explorer.indexOf("TheWorld") >= 0) {
                        return "世界之窗";
                    } else if (explorer.indexOf("Safari") >= 0) {
                        return "苹果";
                    } else {
                        return "其他";
                    }
                }
            })();
            var efs = condition.split(',');

            // 满足加载条件
            if($.inArray(currentExplorerInfo, efs) >= 0) {
                var sptUrl = meta.req.toUrl(trueLoadModuleId);
                var $script = $('<script type="text/javascript" charset="utf-8" src="{0}">'.format(sptUrl));

                $script.on('load', function () {
                    meta.onload({
                        loaded: true
                    });
                });

                $('head').append($script);
            } else {
                meta.onload({
                    loaded : false // 标识该资源不加载
                });
            }
        },

        // 环境加载
        env : function(meta, condition, trueLoadModuleId, falseLoadModuleId) {
            if($.inArray(process.env, condition.split(',')) >= 0) {
                meta.req([trueLoadModuleId], function (value) {
                    meta.onload(value);
                });
            } else {
                meta.req([falseLoadModuleId], function (value) {
                    meta.onload(value);
                });
            }
        }
    };

    return {
        /**
         * 必须定义
         * @param name 模块名称
         * @param req 提供 toUrl 方法, 根据 name, 计算该模块资源的全路径.
         * @param onload 自 2.1.0 之后, 提供了 onload.fromText() 将文本转换为 js 代码执行.
         * @param config 全局配置信息
         */
        load: function (name, req, onload, config) {
            var arr = name.split('?');

            if(arr.length < 2) {
                onload({
                    loaded : false // 标识该资源不加载
                });
            }

            var meta = {name : name, req : req, onload : onload, config : config};
            var method = arr[0];
            var urlParams = (function (params) {
                var config = {};

                $.each(params.split('&'), function (i, val) {
                    var tArr = val.split('=');
                    config[tArr[0]] = tArr[1];
                });

                return config;
            })(arr[1]);
            var condition = urlParams.c,
                trueLoadModuleId = urlParams.y,
                falseLoadModuleId = urlParams.n;

            methods[method](meta, condition, trueLoadModuleId, falseLoadModuleId);
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