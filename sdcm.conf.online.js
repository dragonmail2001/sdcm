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
var path = require('path');

var config  = {   
    cluster: true,
    httpport: 8001,
    timeout: 20000,
    umfs: 2097152,                                   //上传文件总大小上限2m(2 * 1024 * 1024)
    debug: false, 
    ldir: '/home/u1/logs/main',                  //日志文件目录  
    fext: {
        'jpg': true,
        'jpeg': true,
        'png': true
    },  
    cftp:{
            host: "192.168.100.2",
            port: 21,
            user: "aidaiftp",
            password: "AiDaiftp",
            keepalive: 10000,
            path: ""
    },
    cach:[{
            port: 6390,
            host: '192.168.100.31'     
        }, {
            port: 6390,
            host: '192.168.100.32'
        }, {
            port: 6390,
            host: '192.168.100.39'    
        }, {
            port: 6391,
            host: '192.168.100.31'    
        }, {
            port: 6391,
            host: '192.168.100.32'    
        }, {
            port: 6391,
            host: '192.168.100.39'    
    }],
    cacl:{
        //"webpc":{
        //    allow:["*.cnaidai.com"],
        //    deny:[""]
        //},
        "webchat":{
            allow:["wechat.cnaidai.com"]
        }
        //"webjr":{
        //    allow:["a.cnaidai.com"]
        //},
        //"wxshop":{
        //   allow:["wechat.laixiangke.com"]
        //},
        //"wapshop":{
        //   allow:["wap.laixiangke.com"]
        //}
    },
    sess:{
        //domain: '.cnaidai.com',
        domain: [
            //["*.cnaidai.com", ".cnaidai.com"],
            //["*.cnaidai.com:*", ".cnaidai.com"],//开发环境
            //["*.laixiangke.com", ".laixiangke.com"],
            //["*.laixiangke.com:*", ".laixiangke.com"]//开发环境
        ],
        key: 'sdcm keyboard',
        name: 'sdcm.sid',
        cluster: true,
        time: 1800000                             //30分钟
    },
    code: {
        path: '/verifyService?actn=code',
        hostname: '192.168.100.23',
        port: '5524',
        type: 'dscm'
    }, 
    fdir:'/home/u1/upload',                 //前端上传文件保存的临时目录
    dcfg:'/home/u1/webapp'                 //前端请求资源文件本地存放路径
};

module.exports = config;