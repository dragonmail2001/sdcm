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

var conf = require('./cfg'); 
var logj = require('./sdcm.logj.js').getLogger;
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
            logj('main').error("call-sock-err [%s] [%s] [%s]", new Date().getTime() - req.uuid.tim.getTime(),
                JSON.stringify(req.conf), JSON.stringify(req.uuid));
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

sock.file = function(req,res, fld, fle) {
    req.uuid.upf = 2;
    if(conf.cftp != null) {
        var array = [];
        for(var the in fle){                 
            array.push(fle[the][0]);
        }   

        array.forEach(function(the){
            var ftpClient = new ftp();  
            ftpClient.on('ready', function() {
                ftpClient.put(the.path, conf.cftp.path+the.originalFilename, function(err) {
                    if (err) {
                        logger('main').error("file upload err by [%s]", the.path, JSON.stringify(err));                      
                    }

                    ftpClient.end();
                    ftpClient.destroy();

                    fs.exists(the.path, function (exists) {
                        if(exists){
                            logger('main').error('ftp suc and delete file [%s]', the.path);
                            fs.unlink(the.path);
                        }
                    });                             
                });
            });         
            ftpClient.connect(conf.cftp);  
        }); 
    }
}

sock.request = function(cfg, itf, req, res, fld, fle, fuc) {
    var param = itf.func (req, res, fld, fle);
    if(req.uuid.upf == 1) {
        this.file(req, res, fld, fle);
    }

    if(!param || this.isBrace(param)) { 
        calfuc(req, fuc, req.uuid.cur + 1, true, true, 
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
    var body = '', option = conf.code; option.agent = false;
    option.headers = {'claz':'["java.lang.String"]'};

    var object = http.request(option, function(output){
         output.setEncoding('utf8');
         output.on('data',function(d){
             body += d;
         }).on('end', function() {
            if(!output.headers['errs']) {
                var buffer = new Buffer(body, 'base64');
                res.writeHead(200, {
                  'Content-Length': buffer.length,
                  'Content-Type': 'image/jpeg'
                });
                res.write(buffer); 
                res.end();
            }else{
                logger('main').error("call-code-err [%s]", JSON.stringify(output.headers));          
            }
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

     object.write(JSON.stringify([tex]));  
     object.end();
}