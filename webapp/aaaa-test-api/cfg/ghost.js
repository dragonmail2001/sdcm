/**
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

log4js.loadAppender("dateFile");
log4js.addAppender(log4js.appenderMakers['dateFile']({  
    filename:process.argv[3]+"/aaaa-test-api/aaaa-test-api.log",  
    pattern: '.yyyy-MM-dd.log',alwaysIncludePattern: true,  
    layout: {
        type: 'pattern',
        pattern: '[%d %p %c] %m%n'
    }  
}), 'aaaa-test-api'); 

module.exports = {
    getLogger:function() {
        return log4js.getLogger('aaaa-test-api');
    },
    cenv: 'dev',
    //阿里云上传文件设置【容器会使用的配置，如果不需要上传文件则不用配置】 
    coss:{
        object: "dev",
        bucket: "jxgstatich5",
        craa: {
            region:"oss-cn-shenzhen",
            accessKeyId:"LTAI4G6XuCXeQgS1xnajdhU2",
            accessKeySecret:"2METodRMm8jPSvJ3mURS48KG7V4iNL"
        }
    }, 
    //上传文件时候扩展名设置【容器会使用的配置，如果不需要上传文件则不用配置】  
    fext: {
        'jpg': true,
        'jpeg': true,
        'png': true,
        'webm': true,
        'mp4': true
    },  
    //redis相关设置      
    rdis: {
        //如果cluster是false代表redis是单机模式（host数组中的第一个元素有效），否则是集群模式
        //【应用自己使用的配置】 
        cluster: false,
        prefix: "",        
        host:[
            {
                port: 6379,
                host: '127.0.0.1'     
            }, {
                port: 7001,
                host: '172.16.100.51'     
            }, {
                port: 7001,
                host: '172.16.100.52'
            }, {
                port: 7001,
                host: '172.16.100.53'    
            },
            {
                port: 7002,
                host: '172.16.100.51'     
            }, {
                port: 7002,
                host: '172.16.100.52'
            }, {
                port: 7002,
                host: '172.16.100.53'    
            }        
        ]
    },  
    //token验证支持jwt【应用自己使用的配置】  
    cjwt:{
        basic: {
           name: 'authorization',
           memo: 'Basic'
        },
        blade: {
           name: 'blade-auth',
           memo: 'bearer'
        },        
        pkey: 'BladeX'
    },  
    //后端服务地址配置【应用自己使用的配置】   
    http:{
        php:{
            pref: '',
            type: 'hssl',
            addr: 'fgwebapp.test.yonwan.cn',
            port: '443'
        }
    },
    //后端服务地址配置【应用自己使用的配置】 
    dscm: {
        dscmauth:{
            addr:'10.40.20.155',
            port:5524
        }
    }   
}