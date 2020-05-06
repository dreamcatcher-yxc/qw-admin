define([
    'alias!@API/auth/index',
    'jquery',
    'async-validator'
],function (AuthAPI, jQuery, validator) {
    /*
        包含所有可以 "混入" 组件的公共选项, 使用的时候可以选择性的导入
    */

    /**
     * 公共 mixin 选项, 如 loading
     */
    var CommonMixin = {
        props : {
            data : {
                type : [Object, Array, String, Boolean, Number],
                default() {
                    return {}
                }
            }
        },

        filters : {
            menuName: function(route) {
                return route.meta.menuName || route.meta.title || route.name;
            }
        },

        computed: {
            isLoading: function() {
                return this.qwIsLoading;
            }
        },

        data : function(){
            return {
                qwIsLoading : false
            }
        },

        methods : {
            showLoading : function() {
                this.qwIsLoading = true;
            },

            hideLoading : function() {
                this.qwIsLoading = false;
            },

            replaceArrayByArray: function(toArrayKey, fromArray) {
                var toArray = this[toArrayKey];
                if(!Array.isArray(toArray) || !Array.isArray(fromArray)) {
                    return [];
                }
                this[toArrayKey] = [];
                fromArray.forEach(item => this[toArrayKey].push(item));
            }
        }
    };

    /**
     * 模态框 mixin 选项
     */
    var ModalMixin = {
        props : {
            data : {
                type : [Object, Array, String, Boolean, Number],
                default : function() {
                    return {}
                }
            },

            show : {
                type : Boolean,
                required : true
            },

            // 标记当前是否是处于编辑状态, 默认为 false
            isEdit : {
                type : Boolean,
                default : false
            }
        },

        data : function() {
            return {
                qwModalReturnData : {},
                qwModalIsShow : true
            }
        },

        computed:  {
            isVisible : function() {
                return this.show;
            }
        },

        watch : {
            show : function(newVal, oldVal) {
                this.qwModalIsShow  = newVal;
            },

            qwModalIsShow : function(newVal, oldVal) {
                if(!newVal) {
                    this.$emit('update:show', newVal);

                    if(!('$hook' in this.qwModalReturnData)) {
                        this.qwModalReturnData.$hook = false;
                    }

                    this.$emit('on-close', this.qwModalReturnData);
                }
            }
        },

        methods : {
            close : function(returnData) {
                var modalReturnData = returnData||this.qwModalReturnData;
                this.qwModalReturnData = modalReturnData;
                this.qwModalIsShow = false;
            }
        }
    };

    /**
     * 权限相关全局混入
     */
    var AuthMixin = {
        data : function() {
            return {
                qwAuthMapChangeFlag : 1,  // 权限更新标志
                qwAuthMapMeta : {} // 权限元数据
            }
        },

        computed : {
            // 权限映射
            $$authMap : function () {
                this.qwAuthMapChangeFlag++;
                return this.qwAuthMapMeta;
            }
        },

        methods : {
            /**
             * 判断是否当前登录用户是否具有指定的权限
             * @param expression 权限判断表达式
             */
            $$hasAuth : function (expression) {
                // console.log(this.$options.name + '.$$hasAuth...');

                if(expression in this.$$authMap) {
                    return !!this.$$authMap[expression];
                }

                this.qwAuthMapMeta[expression] = false; // 总是设置为 false
                return false;
            },

            /**
             * 从服务器端查询权限
             */
            $$fetchAuth : function (auths) {
                if(typeof auths === 'string') {
                    this.$$hasAuth(auths);
                }

                if(Array.isArray(auths)) {
                    auths.forEach(auth => this.$$hasAuth(auth));
                }

                var keys = [];
                var that = this;

                for(var key in that.qwAuthMapMeta) {
                    keys.push(key);
                }
                
                var keys = JSON.stringify(keys);
                return AuthAPI.validateAuth(keys)
                    .then(function(data) {
                        var sdata = data.sdata;
                        for(var key in that.qwAuthMapMeta) {
                            that.qwAuthMapMeta[key] = sdata[key];
                        }
                        that.qwAuthMapChangeFlag++;
                    });
            }
        }
    };

    /**
     * validator 校验规则混入
     */
    var ValidatorMixin = {
        methods: (function(jQuery, validator) {
            var newValidator = {};
            jQuery.extend(true, newValidator, validator);
            return newValidator;
        })(jQuery, validator)
    }

    /**
     * 业务相关混入
     * @type {{}}
     */
    var BusinessMixin = {
        data : function() {
            return {};
        },
        methods: {

        }
    };

    return  {
        CommonMixin : CommonMixin,
        ModalMixin : ModalMixin,
        AuthMixin : AuthMixin,
        ValidatorMixin: ValidatorMixin,
        BusinessMixin : BusinessMixin
    }
});