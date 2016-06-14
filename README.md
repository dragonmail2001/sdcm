# sdcm
nodejs|top|mtop|service|sdcm|dscm|rpc|ddd data convert matrix


#install

NODE_PATH  系统需要设置NodeJS环境变量

npm install log4js@0.6.26 -g

###############################################################
#配置文件说明， 后续会增加单机版redis的支持

var config  = {   
    cluster: false,                                  //是否运行在集群模式
    httpport: 8001,                                  //服务启动在哪个端口
    timeout:20000,                                   //调用后端服务的超时时间，单位ms
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
        time: 600000                                //10分钟
    },
    code: {                                         //验证码采用后端服务生成可以是dscm服务或者http服务
        path: '/verifyService?actn=code',
        hostname: '192.168.18.198',
        method: 'post',
        port: '5524'
    }, 
    fdir:'/Workspace/cloudy/fdir',                  //前端上传文件保存的临时目录
    dcfg:'/Workspace/cloudy/work'                   //前端请求资源文件本地存放路径
};


