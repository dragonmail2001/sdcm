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
var ftp = require('ftp'); 
var path = require('path');
var async = require('async');
var multiparty = require('multiparty');

var load = require('./sdcm.util.js').loadConf; 
var last = require('./sdcm.util.js').loadLast;
var logj = require('./sdcm.logj.js');
var conf = require('./sdcm.conf.js'); 
var sock = require('./sdcm.sock.js');
var iset = require('./sdcm.iset.js');


function allow(filename,cfg) {
    if(filename == null){
        return false;
    }

    var pos = filename.lastIndexOf('.');
    if(pos < 0){
        return false;
    }

    try{
        return cfg.fext[filename.substr(pos+1)];
    }catch(err){

    }

    return false;
}

exports = module.exports = function file (req, res, next) { 
    var cfg = req._$sdcm$_.conf.ocfg;
    var form = new multiparty.Form({uploadDir: process.argv[4]+'/', maxFieldsSize:conf.umfs});
    form.parse(req, async function(err, fld, fle){  
        if(err){
            res.status(!err.status ? 500 : err.status).
                jsonp({"err": err.message});              

            logj.reqerr("call-file-err1", req, err);            
            return;
        } 

        var array = [], total = 0; 
        var cfg = req._$sdcm$_.conf.ocfg;
        if(fle != null) {
            for(var the in fle){  
                for(var obj in fle[the]){
                    if(cfg.fext != null && !allow(fle[the][0].originalFilename, cfg)) {
                        sock.remove(fle[the][obj].path);
                        res.status(500).jsonp({"err": "file type err!!!"}); 
                        logj.strerr("call-file-err2", fle[the][obj].path, null); 
                        return null;
                    } 

                    if(cfg.fmax != null && fle[the][0].size >cfg.fmax) {
                        sock.remove(fle[the][obj].path);
                        res.status(500).jsonp({"err": "file size err!!!"}); 
                        logj.strerr("call-file-err3", fle[the][obj].path, null); 
                        return null;
                    }    

                    array.push(fle[the][obj]);
                }
            } 
        }

        for(var i=0; i<cfg.itfs.length; i++){
            req._$sdcm$_.uuid.max = req._$sdcm$_.uuid.max + 1;             
            var bret = await sock.httpimpl(cfg, cfg.itfs[i], 
                req, res, fld, fle, array);

            if(!bret || req._$sdcm$_.exit) {
                break;
            }
        }                                           
    });
};

