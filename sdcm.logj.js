/*
 * Copyright 2015-2115 the original author or authors.
 *
 * Licensed under the MIT Licensed
 *
 * @email   dragonmail2001@163.com
 * @author  jinglong.zhaijl
 * @date    2015-10-24
 *
 */
var conf = require('./sdcm.conf.js');
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
        filename: conf.ldir+'/main', 
    }],
    levels: {
      "[all]": conf.loglevel  	
    }
});

/**
 * 
 * 外部引用日志时候的函数入口类
 * tmplog.getLogger('main').info('这是使用例子');
 *
 */
exports.getLogger = function(name){
    return log4js.getLogger(name);
};


