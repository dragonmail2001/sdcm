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
    timeout: 20000,
    umfs: 2097152,                                   //上传文件总大小上限2m(2 * 1024 * 1024)
    debug: true, 
    ldir: '/Workspace/cloudy/logs',                  //日志文件目录    
    cftp:{
	    host: "192.168.18.248",
	    port: 21,
	    user: "devftp",
	    password: "aidaidev",
	    keepalive: 10000,
	    path: ""
    },
    cach:[
        {
            port: 6390,
            host: '192.168.18.243'     
        }, {
            port: 6390,
            host: '192.168.18.244'
        }, {
            port: 6390,
            host: '192.168.18.248'    
        }
    ],
    sess:{
        domain: '',
        key: 'sdcm keyboard',
        name: 'sdcm.sid',
        cluster: false,
        time: 600000                             //10分钟
    },
    code: {
        path: '/verifyService?actn=code',
        hostname: '192.168.18.198',
        port: '5524',
        type: 'dscm'        
        //path: '/admin-web/admin/common/captcha.cgi',
        //hostname: '192.168.18.198',
        //port: '8001',
        //type: 'http'
    }, 
    fdir:'/Workspace/cloudy/fdir',                 //前端上传文件保存的临时目录
    dcfg:'/Workspace/cloudy/work'                 //前端请求资源文件本地存放路径
};

module.exports = config;