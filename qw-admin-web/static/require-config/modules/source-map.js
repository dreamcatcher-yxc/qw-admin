define([
    'config'
], function(Conf) {
    'use strict';

    return {
        parse: function(script, name) {
            if(Conf.env !== 'development') {
                return script;
            }
            var lines = script.split(/\r\n/);

            while(lines.length > 0) {
                var line = lines[lines.length - 1];
                if(line.trim().length === 0) {
                    lines.pop();
                }
                break;
            }
            
            var lastLine = lines[lines.length - 1].trim();
            
            if(lastLine.indexOf('//# sourceURL=') !== 0) {
                lines.push('//# sourceURL= ' + name);
            }

            return lines.join('\r\n');
        }
    }
});