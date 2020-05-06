define([
    'config',
    'jquery',
], function(Conf, $) {
    'use strict';
    
    var needCache = Conf.env !== 'development';
    var currentVersion = Conf.version;

    return function(url, success, error) {
        if(url.indexOf('?') < 0) {
            url = url + '?brower_spa_version=' + currentVersion;
        } else if(url.lastIndexOf('&') === url.length - 1){
            url = url.substring(0, url.length - 1) + 'brower_spa_version=' + currentVersion;
        } else {
            url = url + '&brower_spa_version=' + currentVersion;
        }
        $.ajax({
            url: url,
            cache: needCache,
            type: 'GET',
            dataType: 'text',
            timeout: Conf.timeout * 1000,
            success: success,
            error: error||function() {}
        });
    }
});