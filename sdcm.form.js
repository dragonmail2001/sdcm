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
var async = require('async');
var events = require('events');

var last = require('./sdcm.util.js').loadLast;
var load = require('./sdcm.util.js').loadConf;
var logj = require('./sdcm.logj.js').getLogger;
var sock = require('./sdcm.sock.js');
var iset = require('./sdcm.iset.js');
var conf = require('./sdcm.conf.js'); 

exports = module.exports = function form(req, res, next) {
    if(!iset.set(req, res)) { 
        res.jsonp({"code": -600000,
            "success": false,
            "message": '请求配置信息错误'
        });  

        logj('main').error("form-err0 [%s][%s]", req.baseUrl, new Date().getTime() - req.uuid.tim.getTime());          
        return; 
    }

    var cfg = load(req.conf.dcfg);
    if(!cfg) {
        res.jsonp({"code": -800000,
            "success": false,
            "message": '请求配置信息错误'
        });  

        logj('main').error("form-err1 [%s][%s]", req.baseUrl,new Date().getTime() - req.uuid.tim.getTime());              
        return;
    }

    if(!cfg.itfs || cfg.itfs.length <= 0) {
        last(cfg, req, res, null,null);
        return;
    }    

    var call = []; cfg.itfs.forEach (function (itf) {
        req.uuid.max = req.uuid.max + 1; 
        call.push(function(fuc) {  
            sock.request(cfg, itf, req, res, null,null, fuc);
        }); 
    });

    async.parallel(call, function(err) {
        if (err) {
            logj('main').error("call-form-err2 [%s][%s][%s][%s][%s]", new Date().getTime() - req.uuid.tim.getTime(), 
                JSON.stringify(req.conf), JSON.stringify(req.uuid), JSON.stringify(err), req.baseUrl);
        }

        if(req.uuid.cur >= req.uuid.max){
            if(req.uuid.jum) {
                if (conf.debug && req.uuid.moc) {
                    last(cfg, req, res, null,null);
                }
            }else {
                last(cfg, req, res, null,null);
            }
        }        
    }); 
};

