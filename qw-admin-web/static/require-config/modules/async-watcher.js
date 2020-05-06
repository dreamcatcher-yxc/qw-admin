define(function() {
    'use strict';
    
    /**
     * 
     * @param {*} callback 监听回调, 接收监听对象(没有传递时为 null), handlerCloser(结束监听方法), 可直接调用 handlerCloser 或者返回 true 结束监听
     * @param {*} target 监听对象, 可选
     * @param {*} timeout 监听超时时间, 默认为 30 秒
     */
    function wacher(callback, target, timeout) {
        timeout = timeout||30;
        target = target||null;
        var intervalHandler;
        var timeoutHandler;

        var handlerCloser = function() { 
            clearInterval(intervalHandler);
            clearTimeout(timeoutHandler);
            // console.log('监听完成...');
        }

        // console.log('监听开始...');
        
        intervalHandler = setInterval(function() {
            var finish = callback(target, handlerCloser);
            if(finish) {
                handlerCloser();
            }
        }, 100);

        timeoutHandler = setTimeout(handlerCloser, 30 * 1000);
    };
    
    return wacher;
});