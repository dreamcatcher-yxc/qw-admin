define([
    'config'
], function(config) {
    'use strict';
    
    var alias = config.alias;
    var aliaKeys = Object.keys(alias);

    return {
        parse: function(path) {
            // 判断有无比别名
            var needReplaceKeys = aliaKeys.filter(function(val) { 
                return path.indexOf(val) === 0;
            });

            if(needReplaceKeys.length <= 0) {
                return path;
            }

            // 选长度最长的
            var needReplaceKey = needReplaceKeys[0];

            for(var i = 1; i < needReplaceKeys.length; i++) {
                if(needReplaceKeys[i].length >= needReplaceKey.length) {
                    needReplaceKey = needReplaceKeys[i];
                }
            }

            var needReplaceVal = alias[needReplaceKey];
            return needReplaceVal + path.substring(needReplaceKey.length);
        }
    }
});