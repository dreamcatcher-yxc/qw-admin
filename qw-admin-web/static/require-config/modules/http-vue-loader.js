define((function(env) {
    var baseDeps = ['config', 'es6-parser', 'alias-parser', 'text-loader', 'source-map'];
    if(env !== 'dev') {
        return baseDeps;
    }
    return baseDeps.concat(['vue-hot-load-api', 'vue-template-compiler']);
})(window.__env__), 
function (Conf, es6Parser, aliasParser, ScriptLoader, SM, $VueHotReloadAPI, $VueTemplateCompiler) {
    'use strict';

    //记录当前页面所有的 scopeId
    var SCOPE_ID_MAP = {};
    var VueHotReloadAPI = $VueHotReloadAPI;
    var VueTemplateCompiler = $VueTemplateCompiler;

    /**
     * 默认处理, 函数式编程里面有这种东西, 反正就理解成什么都不干, 给它什么就返回什么.
     * @param {String} value 
     */
    function identity(value) {
        return value;
    }

    /**
     * 解析路径, eg: 
     * > a/b 返回的结果为 { name: 'index', url: 'a/b/index.vue' }, 既名称默认为 index
     * > a/b/c.vue 返回的结果为 { name: 'c', url: 'a/b/c.vue' }
     * @param {String} url 解析路径
     */
    function parseComponentURL(url) {
        var comp = url.match(/(.*?)([^/]+?)\/?(\.vue)?(\?.*|#.*|$)/);
        return {
            name: comp[2],
            url: comp[1] + comp[2] + (comp[3] === undefined ? '/index.vue' : comp[3]) + comp[4]
        };
    }

    /**
     * 合并路径
     * @param {String} baseURL 基础路径
     * @param {String} url 新增路径
     */
    function resolveURL(baseURL, url) {
        if (url.substr(0, 2) === './' || url.substr(0, 3) === '../') {
            return baseURL + url;
        }
        return url;
    }

    /**
     * HttpVueLoader 单例创建工厂
     */
    function HttpVueLoaderFactory() {
        this.httpVueLoader = null;
    }
    HttpVueLoaderFactory.prototype.httpVueLoaderInstance = function() {
        if(this.httpVueLoader === null) {
            this.httpVueLoader = new HttpVueLoader();
        }
        return this.httpVueLoader;
    }
    var httpVueLoaderFactory = new HttpVueLoaderFactory();

    /**
     * 生成一个新的 HttpVueLoader(Vue 组件加载器)
     * @param {String} baseURI 
     */
    function httpVueLoaderGenerator(baseURI) {
        var newHttpVueLoader = httpVueLoaderFactory.httpVueLoaderInstance();
        return function(url) {
            url = url.lastIndexOf('.vue') !== (url.length - 4) ? (url + '.vue') : url;
            var realUrl = resolveURL(baseURI, url);
            return newHttpVueLoader.load(realUrl);
        }
    }

    /**
     * 支持 promise 的 require 加载机制
     */
    function asyncRequire(target) {
        return function(deps, callback) {
            return new Promise(function(resolve, reject) {
                require(deps, function() {
                    var newArgs = [];
                    for(var i = 0; i < arguments.length; i++) {
                        newArgs.push(arguments[i]);
                    }
                    newArgs.push(resolve);
                    newArgs.push(reject);
                    callback.apply(target, newArgs);
                })
            });
        }
    }

    /**
     * 样式处理类
     * @param { Component } component
     * @param { Element } elt 
     */
    function StyleContext(component, elt) {
        this.component = component;
        this.elt = elt;
    }

   
    StyleContext.prototype = {

        /**
         * 创建 base 标签
         * @param { Function } callback 
         */
        withBase: function(callback) {
            var tmpBaseElt;

            if (this.component.baseURI) {
                // firefox and chrome need the <base> to be set while inserting or modifying <style> in a document.
                tmpBaseElt = document.createElement('base');
                tmpBaseElt.href = this.component.baseURI;
                var headElt = this.component.getHead();
                headElt.insertBefore(tmpBaseElt, headElt.firstChild);
            }

            callback.call(this);

            if (tmpBaseElt) {
                this.component.getHead().removeChild(tmpBaseElt);
            }
        },

        /**
         * 将传入的 css 样式, 添加局部样式选择器, eg:
         * > div.foo > b 装换为 div.foo > b[${scopeName}]
         * > div.foo > b[foo]:hover 装换为 div.foo > b[foo][${scopeName}]:hover
         * @param { String } selectorText css 单选择器
         * @param { String } scopeName 局部样式选择器
         */
        __selectorTextScoped : function(selectorText, scopeName) {
            var singleSelectors = selectorText.split(/[ >+]+/);

            if(singleSelectors.length <= 0) {
                return selectorText;
            }
            
            var lastSel = singleSelectors.pop();
            var segments = lastSel.match(/([^ :]+)(.+)?/);
            var newSegment = segments[1] + scopeName + (segments[2]||'');
            return selectorText.substring(0, selectorText.lastIndexOf(lastSel)) + newSegment;
        },
        
        /**
         * 将传入的 rules 全部加上 scopeName 作为属性选择器
         * @param { CSSStyleRule } rules css 规则
         * @param { Sheet } sheet 样式容器
         * @param { String } scopeName 局部属性
         */
        __scopeRuleList : function(rules, sheet, scopeName) {
            for ( var i = 0; i < rules.length; ++i ) {
                var rule = rules[i];

                if (rule.type !== 1 && rule.type !== 4) continue;

                // 媒体选择
                if(rule instanceof CSSMediaRule) {
                    if(rule.cssRules.length > 0) {
                        this.__scopeRuleList(rule.cssRules, rule, scopeName);
                    } 
                    continue;
                }
                
                // 普通选择器
                if(rule instanceof CSSStyleRule) {
                    var scopedSelectors = [];
                    var self = this;

                    rule.selectorText.split(/\s*,\s*/).forEach(function(sel) {
                        scopedSelectors.push(self.__selectorTextScoped(sel, scopeName));
                    });

                    var scopedRule = scopedSelectors.join(',') + rule.cssText.substr(rule.selectorText.length);
                    sheet.deleteRule(i);
                    sheet.insertRule(scopedRule, i);
                }
            }
        },
        
        /**
         * 局部化样式处理
         * @param { Element } styleElt 
         * @param { String } scopeName 
         */
        __process : function(styleElt, scopeName) {
            var sheet = styleElt.sheet;
            var rules = sheet.cssRules;
            // console.log(rules);
            this.__scopeRuleList(rules, sheet, scopeName);
        },

        /**
         * 局部化样式, 包含出错处理
         * @param { Element } styleElt 
         * @param { String } scopeName 
         */
        scopeStyles: function(styleElt, scopeName) {
            try {
                // firefox may fail sheet.cssRules with InvalidAccessError
                this.__process(styleElt, scopeName);
            } catch (ex) {
                if ( ex instanceof DOMException && ex.code === DOMException.INVALID_ACCESS_ERR ) {
                    styleElt.sheet.disabled = true;
                    var self = this;

                    styleElt.addEventListener('load', function onStyleLoaded() {
                        styleElt.removeEventListener('load', onStyleLoaded);
                        // firefox need this timeout otherwise we have to use document.importNode(style, true)
                        setTimeout(function() {
                            self.__process(styleElt, scopeName);
                            styleElt.sheet.disabled = false;
                        });
                    });
                    return;
                }

                throw ex;
            }
        },

        /**
         * 编译工作, 判断当前组件样式是否有 scoped 标识，如果有，则添加局部化样式标识.
         */
        compile: function() {
            var hasTemplate = this.template !== null;
            var scoped = this.elt.hasAttribute('scoped');

            if ( scoped ) {
                // no template, no scopable style needed
                if (!hasTemplate) return;
                // firefox does not tolerate this attribute
                this.elt.removeAttribute('scoped');
            }

            this.withBase(function() {
                var scopeId = this.component.getScopeId();
                var headerElt = this.component.getHead();
                var oldStyleElt = headerElt.querySelector('style['+ scopeId +']');
                if(oldStyleElt) {
                    oldStyleElt.remove();
                }
                headerElt.appendChild(this.elt);
            });

            if (scoped) {
                this.scopeStyles(this.elt, '['+this.component.getScopeId()+']');
            }

            return Promise.resolve();
        },

        getContent: function() {
            return this.elt.textContent;
        },

        setContent: function(content) {
            this.withBase(function() {
                this.elt.textContent = content;
            });
        }
    };

    /**
     * JS 脚本处理类
     * @param { Component } component
     * @param { Element } elt 
     */
    function ScriptContext(component, elt) {
        this.component = component;
        this.elt = elt;
        this.module = { exports:{} };
    }

    ScriptContext.prototype = {

        getContent: function() {
            return this.elt.textContent;
        },

        setContent: function(content) {
            this.elt.textContent = content;
        },

        compile: function() {
            // var childModuleRequire = function(childURL) {
            //     return httpVueLoader.require(resolveURL(this.component.baseURI, childURL));
            // }.bind(this);

            // var childLoader = function(childURL, childName) {
            //     return httpVueLoader(resolveURL(this.component.baseURI, childURL), childName);
            // }.bind(this);

            try {
                var content = SM.parse(this.getContent(), this.component.url);
                // console.log(content);
                var load = httpVueLoaderGenerator(this.component.baseURI);
                var requirex = asyncRequire({});
                // 动态创建一个 Function
                var func = Function('exports', 'require', 'load', 'module', 'asyncRequire', content);
                func.call(this.module, this.module.exports, require, load, this.module, requirex);
            } catch(ex) {
                if ( !('lineNumber' in ex) ) {
                    return Promise.reject(ex);
                }
                var vueFileData = this.getContent().replace(/\r?\n/g, '\n');
                var lineNumber = vueFileData.substr(0, vueFileData.indexOf('script')).split('\n').length + ex.lineNumber - 1;
                throw new (ex.constructor)(ex.message, url, lineNumber);
            }

            // 这里是比较巧妙的一个地方, 本项目中所有的 vue script 中的代码既可以直接为 module.exports = {} 
            // 或者 module.exports = new Promise(...) 都取决于这部分, 可以参看如下文档了解 Promise.resolve:
            // https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/Promise/resolve
            return Promise.resolve(this.module.exports)
                // httpVueLoader.scriptExportsHandler 使用在此, 本项目中没用, 就是一个 exports 之后的处理.
                .then(httpVueLoader.scriptExportsHandler.bind(this))
                // exports 中的内容其实已经包含了 Vue 组件的大部分内容( template 不包含), 所以下一步就是
                .then(function(exports) {
                    this.module.exports = exports;
                }.bind(this));
        }
    };

    /**
     * HTML 脚本处理类
     * @param { Component } component
     * @param { Element } elt 
     */
    function TemplateContext(component, elt) {
        this.component = component;
        this.elt = elt;
    }

    TemplateContext.prototype = {

        getContent: function() {
            return this.elt.innerHTML;
        },

        setContent: function(content) {
            this.elt.innerHTML = content;
        },

        /**
         * 获取所有子节点
         * @param {Function} callback 遍历每个节点之后的回调函数
         */
        getAllChild : function(callback) {
            var arr = [];

            function each($root) {
                var res = callback($root);
                if(!res) {
                    return;
                }  

                var children = $root[0].tagName === 'TEMPLATE' ? $($root[0].content).children() : $root.children();
                arr.push($root);

                if(children.length > 0) {
                    for(var i = 0; i < children.length; i++) {
                        var child = children[i];
                        var $child = $(child);
                        each($child);
                    }
                }
            }

            each($(this.elt));
            return arr;
        },

        compileAlias : function() {
            this.getAllChild(function($node) {
                var tagName = $node[0].tagName;

                if(tagName === 'IMG') {
                    var src = $node.attr('src');
                    if(!src && src.length > 0) {
                        $node.attr('src', aliasParser.parse(src));
                    }
                } else if(tagName === 'A') {
                    var href = $node.attr('href');
                    if(!href && href.length > 0) {
                        $node.attr('href', aliasParser.parse(href));
                    }
                }
            });
        },

        isOldSlotScopeEle : function($node) {
            return !!($node.attr('slot-scope'));
        },

        isNewSlotScopeEle : function($node) {
            var tagName = $node[0].tagName;

            if(tagName !== 'SLOT') {
                return false;
            }

            var attrs = $node[0].attributes;
            
            if(!!attrs) {
                for(var i = 0; i < attrs.length; i++) {
                    var attr = attrs[i];
                    var name = attr.name;

                    if(/v-bind\:[a-z0-9]+/.test(name)) {
                        return true;
                    }
                }
            }
            
            return false;
        },

        isOldNamedSlotEle : function($node) {
            return !!($node.attr('slot'));
        },

        isNewNamedSlotEle : function($node) {
            var tagName = $node[0].tagName;

            if(tagName !== 'TEMPLATE') {
                return !!$node.attr('v-slot');
            }

            var attrs = $node[0].attributes;
            
            if(!!attrs) {
                for(var i = 0; i < attrs.length; i++) {
                    var attr = attrs[i];
                    var name = attr.name;

                    if(/v-slot\:[a-z0-9]+/.test(name)) {
                        return true;
                    }
                }
            }
            
            return false;
        },

        isNewDynamicNamedSlotEle : function($node) {
            var tagName = $node[0].tagName;

            if(tagName !== 'TEMPLATE') {
                return false;
            }

            var attrs = $node[0].attributes;
            
            if(!!attrs) {
                for(var i = 0; i < attrs.length; i++) {
                    var attr = attrs[i];
                    var name = attr.name;

                    if(/v-slot\:\[[a-z0-9]+\]/.test(name)) {
                        return true;
                    }
                }
            }

            return false;
        },

        compile : function() {
            this.compileAlias();
            var self = this;
            var scopeId = self.component.getScopeId();

            this.getAllChild(function($node) {
                var tagName = $node[0].tagName;

                if(
                    tagName !== "SLOT"
                    && tagName !== "TEMPLATE"
                ) {
                    $node.attr(scopeId, '');
                }
                return true;
                // if(
                //     self.isOldNamedSlotEle($node)
                //     || self.isNewNamedSlotEle($node)
                //     || self.isNewDynamicNamedSlotEle($node)
                //     || self.isOldSlotScopeEle($node)
                //     || self.isNewSlotScopeEle($node)
                // ) {
                //     return false;
                // } else {
                //     $node.attr(scopeId, '');
                //     return true;
                // }
            });

            // if(Conf.isDev()) {
                // console.log('url: ', this.component.url);
            // }

            return Promise.resolve();
        }
    };

    /**
     * Vue 文件加载完成之后的处理类.
     * @param {String} name 
     * @param {String} url 
     */
    function Component(name, url) {
        this.name = name;
        this.url = url;
        this.template = null;
        this.script = null;
        this.styles = [];

        if(url in SCOPE_ID_MAP) {
            this._scopeId = SCOPE_ID_MAP[url];
        } else {
            this._scopeId = this._randomString(10);
            SCOPE_ID_MAP[url] = this._scopeId;
        }
    }

    /**
     * aaa
     */
    Component.prototype = {

        getHead: function() {
            return document.head || document.getElementsByTagName('head')[0];
        },

        getScopeId: function() {
            return 'data-v-' + this._scopeId;
        },

        /**
         * 会返回一个 Promise, Promise 的解析结果包含一个完整的 Vue 对象
         * @param {String} componentURL 
         */
        load: function(componentURL) {
            return httpVueLoader
                .httpRequest(componentURL)
                .then(function(responseText) {
                    this.baseURI = componentURL.substr(0, componentURL.lastIndexOf('/')+1);
                    var doc = document.implementation.createHTMLDocument('');
                    // IE requires the <base> to come with <style>
                    doc.body.innerHTML = (this.baseURI ? '<base href="'+this.baseURI+'">' : '') + responseText;
                    var styleAlreadyCreated = document.querySelector('style[' + this.getScopeId()  + ']') != null;

                    for ( var it = doc.body.firstChild; it; it = it.nextSibling ) {
                        switch ( it.nodeName ) {
                            case 'TEMPLATE':
                                this.template = new TemplateContext(this, it, this.getScopeId());
                                break;
                            case 'SCRIPT':
                                this.script = new ScriptContext(this, it);
                                break;
                            case 'STYLE':
                                // dev 环境下每次都会重置组件样式
                                if(Conf.isDev() || !styleAlreadyCreated) {
                                    it.setAttribute(this.getScopeId(), '');
                                    this.styles.push(new StyleContext(this, it));
                                }
                                break;
                        }
                    }

                    return this;
                }.bind(this));
        },

        _normalizeSection: function(eltCx) {
            var promise;

            if ( eltCx === null || !eltCx.elt.hasAttribute('src') ) {
                promise = Promise.resolve(null);
            } else {
                promise = httpVueLoader
                    .httpRequest(eltCx.elt.getAttribute('src'))
                    .then(function(content) {
                        eltCx.elt.removeAttribute('src');
                        return content;
                    });
            }

            // 编译处理
            return promise.then(function(content) {
                        if ( eltCx !== null && eltCx.elt.hasAttribute('lang') ) {
                            var lang = eltCx.elt.getAttribute('lang');
                            eltCx.elt.removeAttribute('lang');
                            return httpVueLoader.langProcessor[lang.toLowerCase()].call(this, content === null ? eltCx.getContent() : content);
                        }
                        return content;
                    }.bind(this))
                    .then(function(content) {
                        if ( content !== null ) {
                            eltCx.setContent(content);
                        }
                    });
        },

        normalize: function() {
            return Promise.all(Array.prototype.concat(
                this._normalizeSection(this.template),
                this._normalizeSection(this.script),
                this.styles.map(this._normalizeSection)
            ))
            .then(function() {
                return this;
            }.bind(this))
            .catch(function(ex) {
                console.error('normalize 异常: ' + this.url);
                console.error(ex);
            }.bind(this));
        },

        compile: function() {
            return Promise.all(Array.prototype.concat(
                this.template && this.template.compile(),
                this.script && this.script.compile(),
                this.styles.map(function(style) { return style.compile(); })
            )).then(function() {
                return this;
            }.bind(this))
            .catch(function(ex) {
                console.error('compile 异常: ' + this.name);
                console.error(ex);
            }.bind(this));
        },

        _randomString : function(len) {
            len = len || 32;
            var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';
            var maxPos = $chars.length;
            var pwd = '';

            for (var i = 0; i < len; i++) {
                pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
            }

            return pwd;
        }
    };

    /**
     * HttpVueLoader class 定义
     */
    function HttpVueLoader() {
        this.langProcessor = {
            html : this.defaultProcessor,
            js : this.defaultProcessor,
            css : this.defaultProcessor,
            es6 : function (es6Script) {
                return es6Parser.convert(es6Script, {presets : ['es2015']});
            }
        }
    }

    /**
     * 此处创建的是一个 Vue 异步组件, 所以 HttpVueLoder 实例创建的所有 Vue 组件都是异步组件, 理解这个知识点很重要,
     * 和使用打包工具 webpack 打包的不一样, 由于他支持 CommonJS 那一套模板系统, 可以预先将引入的组件组装好, 然后给
     * Vue 识别, 此种相当于同步方式, 优点呢，就是减少了 Vue 组件加载 http 请求的数目, 如果量少的话加载速度是相当可观
     * 的, 而缺点也很明显, 如果一个 vue 文件中使用 CommonJS（同步方式） 引入的依赖太多, 则该组件打包出来将会很臃肿,
     * 首次加载速度会慢很多, 所以就有了异步组件的概念, 你可能会说？ webpack 也支持异步配置呀！嗯, 是的，支持的，本项目
     * 也不是和你争辩哪个更优的，因为最佳实践就是配合打包工具，我们这里做的，就是想自己造下轮子...爱折腾...
     */
    HttpVueLoader.prototype.load = function(url, name) {
        return function() {
            var realUrl = aliasParser.parse(url);
            return new Component(name, realUrl)
                // 使用 http Get 请求一个组件, 为什么不是 POST？因为 GET 请求浏览器默认有缓存策略,
                // 在非开发环境下, 可以使每个浏览器只加载一次指定的 vue 文件, 下次代码更新之后, 只要
                // 将全局的版本号更新, 就能够再次加载最新的 vue 文件.
                .load(realUrl)
                // 这个不用说了, vue 文件编译前的预处理, 为下一步的编译做准备
                .then(function(component) {
                    return component.normalize();
                })
                // 组件编译, 会将 style、template、script 进行编译
                .then(function(component) {
                    return component.compile();
                })
                // 组装出一个正常的组件对象
                .then(function(component) {
                    var exports = component.script !== null ? component.script.module.exports : {};

                    if ( component.template !== null ) {
                        exports.template = component.template.getContent();
                    }

                    if ( exports.name === undefined && component.name !== undefined) {
                        exports.name = component.name;
                    }

                    exports._baseURI = component.baseURI;
                    
                    if(Conf.isDev()) {
                        var tUrl = realUrl;
                        var index = tUrl.indexOf('?');
                        if(index > 0) {
                            tUrl = tUrl.substring(0, index);
                        }
                        var metas = tUrl.split('/');
                        metas = metas.filter(meta => {
                            if(!meta) {
                                return false;
                            }
                            if(meta === '..' || meta === '.') {
                                return false;
                            } 
                            return true;
                        });
                        // 创建该组件全局唯一 ID
                        var uniqueId = metas.join("_");
                        // 添加组件加载记录
                        VueHotReloadAPI.createRecord(uniqueId, exports);
                        // console.log('ws: ' + metas.join("_"));
                    }

                    return exports;
                });
        };
    };

    // 重新加载组件, 开发期间很有用!!!
    HttpVueLoader.prototype.reload = function(uniqueId, url, name) {
        this.load(url, name)()
            .then(function(exports) {
                 // 编译新组建模板
                var newComp = exports;
                if(newComp.template) {
                    var fns = VueTemplateCompiler.compileToFunctions(newComp.template);
                    delete newComp.template;
                    newComp.render = fns.render;
                    newComp.staticRenderFns = fns.staticRenderFns;
                }
                // VueHotReloadAPI.rerender(uniqueId, newComp);
                VueHotReloadAPI.reload(uniqueId, newComp);
            });
    }

    /**
     * 注册一个全局组件
     */
    HttpVueLoader.prototype.register = function(Vue, url) {
        var comp = parseComponentURL(url);
        Vue.component(comp.name, httpVueLoader.load(comp.url));
    };

    /**
     * 添加此方法, 可以将 HttpVueLoader 实例作为一个 Vue 插件安装, 在 Vue 加载组件的时候预处理 HttpVueLoader 支持的组件
     * 注意: 如果此组件是全局注册的组件, 则此组件支持 【url:foo/foo.vue】 形式的组件加载, 但是配合 Vue-router, 此种方式
     * 基本似乎有问题, 原因没细究过, 有空再研究吧, 主要还是本人觉得这种方式用着看起来不舒服, 就没管它了...
     */
    HttpVueLoader.prototype.install = function(Vue) {
        Vue.mixin({
            beforeCreate: function () {
                var components = this.$options.components;

                for ( var componentName in components ) {
                    if ( typeof(components[componentName]) === 'string' && components[componentName].substr(0, 4) === 'url:' ) {
                        var comp = parseComponentURL(components[componentName].substr(4));
                        var componentURL = ('_baseURI' in this.$options) ? resolveURL(this.$options._baseURI, comp.url) : comp.url;

                        if ( isNaN(componentName) ) {
                            components[componentName] = httpVueLoader.load(componentURL, componentName);
                        } else {
                            components[componentName] = Vue.component(comp.name, httpVueLoader.load(componentURL, comp.name));
                        }
                    }
                }
            }
        });
    };

    /**
     * 暂时没用, 自己新增的, 本来是为了支持 ES6 Module 语法的, 后面想想还是算了...
     */
    HttpVueLoader.prototype.require = function(moduleName) {
        return window[moduleName];
    };

    HttpVueLoader.prototype.httpRequest = function(url) {
        return new Promise(function(resolve, reject) {
            ScriptLoader(url, function(resp) {
                resolve(resp);
            }, function(xhr, status, errorThrown) {
                reject(status);
            });
        });
    };

    /**
     * scirpt 脚本加载之后的预处理, 这里赋值为 identity, 也就是啥都不干
     */
    HttpVueLoader.prototype.scriptExportsHandler = identity;

    /**
     * vue 中的每个部分 (style, template, scirpt) 部分的默认预编译器, 也是啥都不干
     */
    HttpVueLoader.prototype.defaultProcessor = identity

    /**
     * 创建一个新的 HttpVueLoader
     */
    var httpVueLoader = new HttpVueLoader();

    return httpVueLoader;
});