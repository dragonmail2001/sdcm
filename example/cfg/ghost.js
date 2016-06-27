var log4js = require('log4js');

log4js.loadAppender("dateFile");
log4js.addAppender(log4js.appenderMakers['dateFile']({  
    filename:"/home/u1/logs/nodejs/webpc.log",  
    pattern: '.yyyy-MM-dd',alwaysIncludePattern: true,  
    layout: {
        type: 'pattern',
        pattern: '[%d %p %c] %m%n'
    }  
}), 'webpc'); 

module.exports = {
    getLogger:function() {
        return log4js.getLogger('webpc');
    },
    dscm:{
        dcuser:{
            addr:'192.168.18.198',
            port:5524
        },
        dcbusiness:{
            addr:'192.168.18.198',
            port:5522
        },
        creditcenter:{
            addr:'192.168.18.198',
            port:4520
        }
    },
    webpcHost:{
        url: "http://pc.cnaidai.com/webpc"
    },
    uploadHost: {
        url: "http://adtp.cnaidai.com:881"
    }
}