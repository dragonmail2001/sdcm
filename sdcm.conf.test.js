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
    cluster: false,
    httpport: 4999,
    timeout: 20000,
    umfs: 2097152,                                   //上传文件总大小上限2m(2 * 1024 * 1024)
    debug: true, 
    ldir: '/home/u1/logs/nodejs',                  //日志文件目录  
    fext: {
        'jpg': true,
        'jpeg': true,
        'png': true
    },  
    cftp:{
        host: "192.168.18.251",
        port: 21,
        user: "testftp",
        password: "aidaitest",
        keepalive: 10000,
        path: ""
    },
    cach:[{
            port: 6390,
            host: '192.168.18.200'
        }, {
            port: 6390,
            host: '192.168.18.201'
        }, {
            port: 6390,
            host: '192.168.18.202'
        }, {
            port: 6391,
            host: '192.168.18.200'
        }, {
            port: 6391,
            host: '192.168.18.201'
        }, {
            port: 6391,
            host: '192.168.18.202'
        }
    ],
    ccps:{
        enabled: true,
        cluster: true,
        namespace:"sdcmnp",
        link:"sdcmlk",
        chat:"dscm",
        sync:{
            addr:"192.168.18.210",
            port:9876,
            iurl:"/imApp?actn=talk"
        },
        auth:{
            name:"auth",
            addr:"192.168.18.254",
            port:5524,
            iurl:"/userApp?actn=auth"
        }
    },
    acl:{
        "webpc":{
            allow:["*.cnaidai.com","*"],
            deny:["webshop.cnaidai.com"]
        },
        "webchat":{
            allow:["wechat.cnaidai.com"]
        },
        "webjr":{
            allow:["*.cnaidai.com","ph-node-http"],
            deny:["webshop.cnaidai.com"]
        },
        "wxshop":{
            allow:["wechat.laixiangke.com"]
        },
        "wapshop":{
            allow:["wap.laixiangke.com"]
        }
    },
    sess:{
        //domain: '.cnaidai.com',
        domain: [
            ["*.abc.com", ".abc.com"],
            ["*.abc.com:*", ".abc.com"],//开发环境
            ["*.cnaidai.com", ".cnaidai.com"],
            ["*.cnaidai.com:*", ".cnaidai.com"],//开发环境
            ["*.laixiangke.com", ".laixiangke.com"],
            ["*.laixiangke.com:*", ".laixiangke.com"]//开发环境
        ],
        key: 'sdcm keyboard',
        name: 'sdcm.sid',
        cluster: true,
        time: 600000                             //10分钟
    },
    code: {
        path: '/verifyService?actn=code',
        hostname: '192.168.18.198',
        port: '5524',
        type: 'dscm'
    }, 
    fdir:'/home/u1/upload',                 //前端上传文件保存的临时目录
    dcfg:'/home/u1/webapp'                 //前端请求资源文件本地存放路径
};

module.exports = config;