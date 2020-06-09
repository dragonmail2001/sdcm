/**
 * Copyright 2015-2115 the original author or authors.
 *
 * Licensed under the MIT Licensed
 *
 * @email   dragonmail2001@163.com
 * @author  jinglong.zhaijl
 * @date    2015-10-24
 * @Source: https://github.com/dragonmail2001
 *
 */
var log4js = require('log4js');

/**
 * 
 * 定义两个日志输出，一个是main，另外一个filer，后续可根据应用需求
 * 任意增加日志输出，指定不同分类的日志输出到不同文件；
 *
 */
log4js.configure({
    appenders: [{
        category: 'main',
        type: 'dateFile', 
        layout: {
            type: 'pattern',
            pattern: '[%d %p %c] %m%n'
        },	
        pattern: '.yyyy-MM-dd.log',alwaysIncludePattern: true,			
        filename: process.argv[3]+'/main/main', 
    }/*,{
        category: 'ccps',
        type: 'dateFile', 
        layout: {
            type: 'pattern',
            pattern: '[%d %p %c] %m%n'
        },  
        pattern: '.yyyy-MM-dd.log',alwaysIncludePattern: true,          
        filename: process.argv[3]+'/main/ccps', 
    }*/],
    levels: {
        "log_file":"ALL",
        "console":"ALL",
        "log_date":"ALL" 	
    }
});

/**
 * 
 * 外部引用日志时候的函数入口类
 * tmplog.getLogger('main').info('这是使用例子');
 *
 */
//exports.getLogger = function(name){
//    return log4js.getLogger(name);
//};
var logj = exports = module.exports = {};

logj.toStr = function(err) {
    var str = "";
    if(err) {
        str = err.toString();
        if(err.message) {
            str = err.message;
        }

        if(err.stack) {
            str = str + "\n" + err.stack;
        }
    }

    return str;
};

logj.strerr = function(ft, str, err){
    return log4js.getLogger('main').error(ft + ' path=[%s] err=[%s]', str, logj.toStr(err));
};

logj.reqerr = function(ft, req, err){
    return log4js.getLogger('main').error(ft+' time=[%s] method=[%s] url=[%s] head=[%s] param=[%s] body=[%s] query=[%s] err=[%s]', 
        new Date().getTime() - req._$sdcm$_.uuid.tim.getTime(), req.method, req.baseUrl, JSON.stringify(req.headers), 
        JSON.stringify(req.params), JSON.stringify(req.body), JSON.stringify(req.query), logj.toStr(err));
};

logj.reqinf = function(ft, req, err){
    var waste = new Date().getTime() - req._$sdcm$_.uuid.tim.getTime();
    if(waste > 10){  
        Object.defineProperty(req.params, "password", { enumerable: false });
        Object.defineProperty(req.body, "password", { enumerable: false });
        Object.defineProperty(req.query, "password", { enumerable: false });  
        return log4js.getLogger('main').info(ft+' time=[%s] method=[%s] url=[%s] head=[%s] param=[%s] body=[%s] query=[%s] err=[%s]', 
            new Date().getTime() - req._$sdcm$_.uuid.tim.getTime(), req.method, req.baseUrl, JSON.stringify(req.headers), 
            JSON.stringify(req.params), JSON.stringify(req.body), JSON.stringify(req.query),
            logj.toStr(err));
    }
};

logj.logger = function(){
    return log4js.getLogger('main');
};



