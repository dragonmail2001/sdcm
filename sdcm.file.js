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
var ftp = require('ftp'); 
var path = require('path');
var async = require('async');
var multiparty = require('multiparty');

var load = require('./sdcm.util.js').loadConf; 
var last = require('./sdcm.util.js').loadLast;
var logj = require('./sdcm.logj.js').getLogger;
var conf = require('./sdcm.conf.js'); 
var sock = require('./sdcm.sock.js');
var iset = require('./sdcm.iset.js');

exports = module.exports = function file (req, res, next) {
    if(!iset.set(req, res)) { 
        res.jsonp({"code": -500000,
            "success": false,
            "message": '请求配置信息错误'
        });  

        logj('main').error("file-err0 [%s]", new Date().getTime() - req.uuid.tim.getTime());          
        return; 
    }

    var form = new multiparty.Form({uploadDir: conf.fdir+'/', maxFilesSize:conf.umfs});
    form.parse(req, function(err, fld, fle){  
        if(err){
            req.uuid.err = true; req.uuid.msg = err;    
            res.jsonp({"code": -300000, "success": false,
                "message": 'file size exceeded ' + conf.umfs
            }); 

            logj('main').error("file-err1 [%s][%s][%s][%s]", new Date().getTime() - req.uuid.tim.getTime(), 
                JSON.stringify(req.uuid), JSON.stringify(err), req.baseUrl);            
            return;
        } 

        var cfg = load(req.conf.dcfg); 
        if(!cfg) {
            res.jsonp({"code": -800000,
                "success": false,
                "message": '请求配置信息错误'
            });  

            logj('main').error("file-err2 [%s][%s]", req.baseUrl,new Date().getTime() - req.uuid.tim.getTime());              
            return;
        }

        sock.file(req, res, fld, fle);
        
        if(!cfg.itfs || cfg.itfs.length <= 0) {
            last(cfg, req, res, fld, fle);
            return;
        }               

        var call = []; cfg.itfs.forEach(function(itf){
            req.uuid.max = req.uuid.max + 1; 
            call.push(function(fuc) {     
                sock.request(cfg, itf, req, res, fld, fle, fuc);
            });
        });

        async.parallel(call, function(err) {
            if (err) {
                logj('main').error("file-call-err [%s][%s][%s]", new Date().getTime() - req.uuid.tim.getTime(), 
                    JSON.stringify(err), JSON.stringify(req.uuid));
            }

            if(req.uuid.cur >= req.uuid.max){
                if(req.uuid.jum) {
                    if (conf.debug && req.uuid.moc) {
                        last(cfg, req, res, fld, fle);
                    }
                }else{          
                    last(cfg, req, res, fld, fle);
                }
            }
        });                      
    });
};

