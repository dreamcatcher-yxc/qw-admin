/**
 * 配置 zTree 将后台返回的 simpleData 类型的树转换为 zTree 支持的树节点类型,
 * 并且支持节点排序功能.
 */
define(['jquery'], function($){
    var trees = {
        convert : function (serverRespData, converter) {
            if(!$.isArray(serverRespData)) {
                return [];
            }

            serverRespData = this.__toTreeNodes(serverRespData, converter);
            var that = this;
            serverRespData = serverRespData.sort(function (a, b) {
                return that._compareStr(a.pId, b.pId);
            });
            var tArr = [];
            var tArr2 = [];

            // 分组
            $.each(serverRespData, function (index, element) {
                if(tArr2.length === 0 || tArr2[0].pId === element.pId) {
                    tArr2.push(element);
                } else {
                    tArr.push(tArr2);
                    tArr2 = [element];
                }
                if(index === (serverRespData.length - 1)) {
                    tArr.push(tArr2);
                }
            });
            serverRespData = tArr;

            // 每组之内排序
            for(var i = 0; i < serverRespData.length; i++) {
                serverRespData[i] = serverRespData[i].sort(function (a, b) {
                    return a.orderIndex > b.orderIndex ? 1 : -1;
                });
            }

            // 重组
            tArr = [];
            for(var i = 0; i < serverRespData.length; i++) {
                tArr = tArr.concat(serverRespData[i]);
            }
            serverRespData = tArr;

            return serverRespData;
            //
            //
            // serverRespData.sort(function (a, b) {
            //     $.each(arr, fun)
            // })
        },

        /**
         * 规则如下:
         * <ul>
         *     <li>_compareStr('abc', 'abd') === -1</li>
         *     <li>_compareStr('abc', 'abcd') === -1</li>
         *     <li>_compareStr('abcd', 'abd') === -1</li>
         *     <li>_compareStr('abd', 'abc') === 1</li>
         *     <li>_compareStr('abcd', 'abc') === 1</li>
         *     <li>_compareStr('abd', 'abcd') === 1</li>
         *     <li>_compareStr('abc', 'abc') === 0</li>
         * </ul>
         * 既一次按照按照每个字符创对应位置上的字符进行比较，如果相同则继续向后比较，若期间比较后者同位置上字符值较大，则返回-1,
         * 若期间比较前者同位置上字符值较大，则返回1。若两个字符串同位置上字符都相同，则前者长度较长返回1，后者长度较长返回-1,
         * 两者长度一致返回0.
         * @param str1
         * @param str2
         * @returns {number}
         * @private
         */
        _compareStr : function(str1, str2) {
            var len1 = str1.length;
            var len2 = str2.length;
            var tLen = len1 < len2 ? len1 : len2;

            for(var i = 0; i < tLen; i++) {
                if(str1[i] < str2[i]) {
                    return -1;
                } else if(str1[i] > str2[i]) {
                    return 1;
                }
            }

            return len1 === len2 ? 0 : ((len1 > len2) ? 1 : -1)
        },

        __toTreeNodes : function (serverRespData, converter) {
            var tArr = [];
            $.each(serverRespData, function (i, n) {
                if(!converter) {
                    tArr.push({
                        bId : n.id,
                        id : n.treeId,
                        pId : n.treeParentId,
                        orderIndex : n.orderIndex,
                        name : n.name,
                        url : n.url,
                        description : n.description
                    });
                } else {
                    tArr.push(converter(i, n));
                }
            });
            return tArr;
        }
    };

    return trees;
});