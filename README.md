# sdcm
nodejs|top|mtop|service|sdcm|dscm|rpc|ddd data convert matrix

sdcm: service data convert matrix（服务数据转换），用来将组件化服务接口转换为http接口输出，未来可能会对接dobble或者hsf这样的开源中间件；

dscm：distribute service call matrix（分布式服务调用），包括服务端和客户端，基于spring和netty，用来把普通的spring bean deploy成一个rpc服务；

cddl：cloudy distribute data layer（分布式数据适配层），基于myabits的分库分表现在主要应用在mysql（兼容其他关系型数据库）；

#install

npm config set registry https://registry.npm.taobao.org 

npm info underscore （如果上面配置正确这个命令会有字符串response）

NODE_PATH  系统需要设置NodeJS环境变量

//log4js全局安装
npm install log4js@0.6.26 -g

//安装sdcm
npm install

###############################################################
#配置文件说明
sdcm.conf.js

var config  = {

    cluster: false,                                  //是否运行在集群模式
    httpport: 8001,                                  //服务启动在哪个端口
    timeout: 20000,                                  //调用后端服务的超时时间，单位ms
    umfs: 2097152,                                   //上传文件总大小上限2m(2 * 1024 * 1024)
    debug: true,                                     //是否运行在debug模式，如果是开发环境建议设置成true(*.cfg *.ejs不会缓存便于开发)否则设置成false，
    ldir: '/Workspace/cloudy/logs',                  //日志文件目录    
    cftp:{                                           //后端ftp服务设置，用户上传的文件会上传到ftp服务
	    host: "192.168.18.248",
	    port: 21,
	    user: "devftp",
	    password: "aidaidev",
	    keepalive: 10000,
	    path: ""
    },
    cach:[{                                          //session弃用redis集群设置
            port: 6390,
            host: '192.168.18.243'     
        }, {
            port: 6390,
            host: '192.168.18.244'
        }, {
            port: 6390,
            host: '192.168.18.248'    
    }],
    sess:{                                          //session相关的参数设置，建议整站用一个顶级域名，方便session管理
        domain: '',
        key: 'sdcm keyboard',
        name: 'sdcm.sid',
        cluster: true,                              //redis是否要集群
        time: 600000                                //10分钟
    },
    code: {                                         //验证码采用后端服务生成可以是dscm服务或者http服务
        path: '/verifyService?actn=code',
        hostname: '192.168.18.198',
        port: '5524',
        type: 'dscm'        
        //path: '/admin-web/admin/common/captcha.cgi',
        //hostname: '192.168.18.198',
        //port: '8001',
        //type: 'http'
    }, 
    fdir:'/Workspace/cloudy/fdir',                  //前端上传文件保存的临时目录
    dcfg:'/Workspace/cloudy/work'                   //前端请求资源文件本地存放路径

};


###############################################################
#启动运行

node sdcm.main.js
