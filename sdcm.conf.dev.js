/*
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
var path = require('path');

var config  = {   
    cluster: false,
    httpport: 8001,
    timeout:20000,
    debug: true, 
    ldir: 'D:/dev/webfile/logger',                  //日志文件目录    
    cftp:{
	    host: "192.168.18.248",
	    port: 21,
	    user: "devftp",
	    password: "aidaidev",
	    keepalive: 10000,
	    path: ""
    },
    cach:[{
            port: 6390,
            host: '192.168.18.243'     
        }, {
            port: 6390,
            host: '192.168.18.244'
        }, {
            port: 6390,
            host: '192.168.18.248'    
    }],
    sess:{
        domain: '',
        key: 'sdcm keyboard',
        name: 'sdcm.sid',
        time: 600000                             //10分钟
    },
    code: {
        path: '/verifyService?actn=code',
        hostname: '192.168.18.198',
        method: 'post',
        port: '5524'
    }, 
    fdir:'D:/dev/webfile/upload',                 //前端上传文件保存的临时目录
    dcfg:'D:/dev/webroot'                 //前端请求资源文件本地存放路径
};

module.exports = config;