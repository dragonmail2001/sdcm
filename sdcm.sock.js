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
var fs = require('fs'); 
var ftp = require('ftp');
var http = require('http');
var util = require('util');
var async = require('async');

var conf = require('./sdcm.conf.js');
var logj = require('./sdcm.logj.js');
var load = require('./sdcm.util.js').loadConf;
var last = require('./sdcm.util.js').loadLast;

var sock = exports = module.exports = {};

sock.next = function(cfg, itf, req, res, fld, fle,fuc) {
    var call = []; itf.forEach(function(citf){
        req.uuid.max = req.uuid.max + 1; 
        call.push(function(func) {          
            this.request(cfg, itf, req, res, fld, fle, fuc);
        });
    });

    async.parallel(call, function(err) {
        if (err) {
            logj.reqerr("call-sock-err", req, err);
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
}

sock.prepare = function (citf, param, user) {
     var options = {hostname: citf.host, port: citf.port, path: citf.iurl, method: citf.meth,headers:null};
     options.headers={'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8','user':user};
     if(citf.meth.toUpperCase() == 'GET' && itf.type=='http'){
        options.path = citf.iurl+"?"+qs.stringify(param);    
     }else if(citf.type != 'http') {
        options.headers={'claz':param.claz};
     }

     options.agent = false;
     return options;    
}

sock.isBrace = function (obj) {
    var bRet = true;
    for(var o in obj) {
        bRet = false;
        break;
    }

    return bRet;
}

function calfuc(req, fuc, cur, jum, err, msg) {
    req.uuid.cur = cur;  
    req.uuid.jum = jum;  
    req.uuid.msg = msg;
    req.uuid.err = err;

    fuc(err); 
}

function allow(filename) {
    if(filename == null){
        return false;
    }

    var pos = filename.lastIndexOf('.');
    if(pos < 0){
        return false;
    }

    try{
        return conf.fext[filename.substr(pos+1)];
    }catch(err){

    }

    return false;
}

function remove(path) {
    fs.exists(path, function (exists) {
        if(exists){
            fs.unlink(path);
        }
    });   
}

sock.file = function(req,res, fld, fle) {
    if(conf.cftp != null) {
        var array = [];
        for(var the in fle){  
            if(!allow(fle[the][0].originalFilename)) {
                res.jsonp({"code": -300000,
                    "message": 'fileerr',
                    "success": false
                }); 

                remove(fle[the][0].path);

                logj.strerr("call-ftp-err1", fle[the][0].path, null); 
                return false;
            }           
            array.push(fle[the][0]);
        }   

        array.forEach(function(the){
            var ftpClient = new ftp();  
            ftpClient.on('ready', function() {
                ftpClient.put(the.path, conf.cftp.path+the.originalFilename, function(err) {
                    if (err) {
                        logj.strerr("call-ftp-err2", the.path, err);                      
                    }

                    ftpClient.end();
                    ftpClient.destroy();

                    remove(the.path);                             
                });
            });         
            ftpClient.connect(conf.cftp);  
        }); 
    }

    return true;
}

sock.request = function(cfg, itf, req, res, fld, fle, fuc) {
    var param = itf.func (req, res, fld, fle);
    if(fle != null && !this.file(req, res, fld, fle)) {
        calfuc(req, fuc, req.uuid.cur + 1, true, false, 
            'cfg.itf.func ftp err');                 
        return;
    }    

    if(!param || this.isBrace(param)) { 
        calfuc(req, fuc, req.uuid.cur + 1, true, false, 
        	'cfg.itf.func return err');
        return; 
    }

    var body = '',sody = null;
    var option = this.prepare (itf, param, req.user);
    var object = http.request(option, function(output){
         output.setEncoding('utf8');
         output.on('data',function(d){
             body += d;
         }).on('end', function() {
            if(!output.headers['errs']) {
                try{
                    sody = JSON.parse(body);//  eval('(' + body + ')');
                }catch(err){
	                calfuc(req, fuc, req.uuid.cur + 1, false, true, err);            
                    return;
                }

                req.rslt[itf.uuid] = sody ;
                req.uuid.cur = req.uuid.cur + 1;
                if(!req.uuid.err && itf.next && itf.next.length > 0){
                    this.next(cfg, itf.next, req, res, fld, fle,fuc);
                }else{
                    fuc(false);
                }
            }else{
                req.rslt[itf.uuid]=null;
	            calfuc(req, fuc, req.uuid.cur + 1, false, true, output.headers);  
                logj.strerr("call-dscm-err", option.path, body);            
            }
         }); 
     });    

     object.on ('error', function(err) {
        calfuc(req, fuc, req.uuid.cur + 1, false, true, err);   
     });

     object.setNoDelay(true);
     object.setSocketKeepAlive(false);
     object.setTimeout(conf.timeout, function(){
     	object.abort();
     });

     if(itf.meth.toUpperCase() == 'POST' || citf.type != 'http'){
        if(itf.type != 'http') {
            object.write(JSON.stringify(!param.json ? null : param.json));
        }else{
            object.write(qs.stringify(param));
        }
     }   
     object.end();
}

sock.code = function(req, res, tex) {
    var body = '', option = {
        hostname: conf.code.hostname,
        path: conf.code.path,
        port: conf.code.port,
        method: 'post',
        agent: false
    }; 

    option.headers = {'claz':'["java.lang.String"]'};
    if(conf.code.type != 'dscm') {
        option.method = 'get';
        option.headers = null;
        option.path = option.path + '?code='+tex;
    }
    
    var object = http.request(option, function(output){
        output.setEncoding(conf.code.type == 'dscm' ? 'UTF-8' : 'binary');
        output.on('data',function(d){
            body += d;
        }).on('end', function() {
            if(!output.headers['errs']) {
                var encode = conf.code.type == 'dscm' ? 'base64' : 'binary'
                , buffer = new Buffer(body, encode);
                res.writeHead(200, {
                  'Content-Length': buffer.length,
                  'Content-Type': 'image/jpeg'
                });
                res.write(buffer); 
            }else{
                logj.strerr("call-code-err", conf.code.path, body);          
            }
            res.end();
        }); 
    });    

    object.setNoDelay(true);
    object.setSocketKeepAlive(false);
    object.setTimeout(conf.timeout, function(){
        object.abort();
    });

    object.on ('error', function(err) {
        res.writeHead(200, {'Content-Type': 'image/jpeg'});
        res.end('sys-code-err');
    });     

    if(conf.code.type == 'dscm') {
        object.write(JSON.stringify([tex]));  
    }
    object.end();
}

sock.load = function(req, res, fld, fle) {
    var cfg = load(req.conf.dcfg);
    if(!cfg) {
        res.jsonp({"code": -800000,
            "message": 'cfgerr',
            "success": false
        });  

        logj.reqerr("call-file-err4", req, 'cfgerr');              
        return null;
    }

    if(!cfg.itfs || cfg.itfs.length <= 0) {
        last(cfg, req, res, null,null);
        return null;
    }  

    return cfg;    
}