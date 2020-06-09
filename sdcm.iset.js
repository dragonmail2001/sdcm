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
var getContextName = require('./sdcm.util.js').getContextName; 
var getClientIp = require('./sdcm.util.js').getClientIp;  
var loadConf = require('./sdcm.util.js').loadConf;
var conf = require('./sdcm.conf.js');
var path = require('path');
var url = require('url');

// Generate four random hex digits.  
//function _s4id() {  
//   return (((1+Math.random())*0x10000)|0).toString(16).substring(1);  
//};  
// Generate a pseudo-GUID by concatenating random hexadecimal.  
//function _guid() {  
//   return (_s4id()+_s4id()+_s4id()+_s4id()+_s4id()+_s4id()+_s4id()+_s4id());  
//}; 

exports = module.exports = function iset(req, res, next) {
    req._$sdcm$_ = {
        user: {
            code: null,
            user: null,
            addr: getClientIp(req)
        },
        conf: {
            btpl: false
        },
        objc: {},
        rslt: {},
        uuid: {
            max: 0,
            cur: 0,
            msg: 'ok',
            tim: new Date(),
            app: ''
        },
        exit: false,
        file: false
    };
    req._$cjwt$_ = {};

    var cctx = req.baseUrl.split('/');
    if(cctx.length <= 0) {
        res.status(500).jsonp({"code": 500,
            "message": 'ctxerr format err!!!',
            "success": false
        });        
        return;
    } 

    if(cctx.length > 2) { req._$sdcm$_.uuid.app = cctx[1]; }
    req._$sdcm$_.conf.name = getContextName(cctx[cctx.length-1]);
    cctx.splice(cctx.length-1, 1);

    req._$sdcm$_.conf.dcfg = path.join(process.argv[2], 
        cctx.join('/'), 
        req._$sdcm$_.conf.name + '.cfg'
    );

    res.header("Access-Control-Allow-Credentials", "true");
    res.header("Access-Control-Allow-Origin", req.headers.origin);
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    res.header("Access-Control-Allow-Methods","PUT,POST,GET,DELETE,OPTIONS"); 

    var cfg = loadConf(req, res);
    if(!cfg) {  
        res.status(500).jsonp({"message": 'ctxerr path not surpport!!'});                
        return;
    }

    cfg.baseUrl = req.baseUrl;
    req._$sdcm$_.conf.ocfg = cfg;
    next();      
}