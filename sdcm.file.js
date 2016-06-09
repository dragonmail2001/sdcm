/*
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

var conf = require('./configure'); 
var loadConf = require('./sdcm.util.js').loadConf; 
var loadLast = require('./sdcm.util.js').loadLast;
var logger = require('./sdcm.logj.js').getLogger;
var sock = require('./sdcm.sock.js');
var iset = require('./sdcm.iset.js');

exports = module.exports = function file (req, res, next) {
    if(!iset.set(req, res)) { return; }

    var form = new multiparty.Form({uploadDir: conf.fdir+'/'});
    req.uuid.upf = 1; form.parse(req, function(err, fld, fle){  
        if(err){
            req.uuid.err = true; req.uuid.msg = err;    
            res.jsonp({"code": -300000, "success": false,
                "message": 'form parse err'
            }); 

            logger('main').error("file-err [%s][%s][%s]", new Date().getTime() - req.uuid.tim.getTime(), 
                JSON.stringify(req.uuid), JSON.stringify(err));            
            return;
        } 

        var cfg = loadConf(req.conf.dcfg); 
        if(!cfg.itfs || cfg.itfs.length <= 0) {
            loadLast(cfg, req, res, fld, fle);
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
                logger('main').error("file-call-err [%s][%s][%s]", new Date().getTime() - req.uuid.tim.getTime(), 
                    JSON.stringify(err), JSON.stringify(req.uuid));
            }

            if(req.uuid.cur >= req.uuid.max){
                if(req.uuid.jum) {
                    if (conf.debug && req.uuid.moc) {
                        loadLast(cfg, req, res, fld, fle);
                    }
                }else{          
                    loadLast(cfg, req, res, fld, fle);
                }
            }
        });                      
    });
};

