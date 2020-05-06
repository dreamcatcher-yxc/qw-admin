define([/* 'browser',  */'babel'], function (/* browser, */ babel) {
    return {
        convert: function (es6Script) {
            var es5Code = babel.transform(es6Script, { presets: ['es2015'] }).code;
            return es5Code;
        }
    };
});