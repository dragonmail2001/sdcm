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
var async = require('async');
var events = require('events');

var last = require('./sdcm.util.js').loadLast;
var load = require('./sdcm.util.js').loadConf;
var logj = require('./sdcm.logj.js');
var sock = require('./sdcm.sock.js');
var iset = require('./sdcm.iset.js');
var conf = require('./sdcm.conf.js'); 

exports = module.exports = function form(req, res, next) {
    if(!iset.set(req, res)) { 
        res.jsonp({"code": -500000,
            "message": 'enverr',
            "success": false
        });  

        logj.reqerr("call-form-err0", req, 'enverr');          
        return; 
    }

    var cfg = sock.loader(req, res, null, null);
    if(!cfg) {              
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
            logj.reqerr("call-form-err1", req, err);
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

