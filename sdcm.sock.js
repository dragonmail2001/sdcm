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
var co = require('co');
var fs = require('fs'); 
var ftp = require('ftp');
var util = require('util');
var http = require('http');
var hssl = require('https');
var async = require('async');
var qs = require("querystring");
var aliOss = require('ali-oss');

var conf = require('./sdcm.conf.js');
var logj = require('./sdcm.logj.js');
var load = require('./sdcm.util.js').loadConf;
var last = require('./sdcm.util.js').loadLast;

var main = require('./sdcm.main.js');

var sock = exports = module.exports = {};

sock.next = function(cfg, itf, req, res, fld, fle) {
    for(var i=0; i<itf.length; i++){
        req._$sdcm$_.uuid.max = req._$sdcm$_.uuid.max + 1; 

        var bret = sock.httpimpl(cfg, itf[i], 
            req, res, null,null, null);

        if(!bret) {
            break;
        }
    }
}

sock.prepare = function (citf, param, user, req, res) {
    //给一次可以动态控制调用那个接口的机会
    if(param.iurl) {
        citf.iurl = param.iurl;
    }

     var options = {hostname: citf.host, port: citf.port, path: citf.iurl, method: citf.meth,headers:null};
     options.headers={'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8','user':user};
     if(citf.type == 'sdcm') {
        options.headers['claz']=param.claz;
     } else {
        if(citf.reqf != null && citf.reqf.toUpperCase() == 'JSON') {
            options.headers['Content-Type'] = 'application/json; charset=UTF-8';
            options.headers['user'] = user;
        }

        if(citf.meth.toUpperCase() == 'GET'){
            options.path = citf.iurl+"?"+qs.stringify(param.objv);  
        }else{
            if(citf.type.toUpperCase() == 'SDCM') {
                options.headers['Content-Length'] = JSON.stringify(!param.json ? null : param.json).length;
            }else{
                if(citf.rfmt != null && citf.rfmt.toUpperCase() == 'JSON') {
                    options.headers['Content-Length'] = JSON.stringify(param.objv).length;
                }else{
                    options.headers['Content-Length'] = qs.stringify(param.objv).length;
                }
            }
        }
     }

     if(param.head && param.head.length > 0) {
        for(var j = 0; j < param.head.length; j++) {
            var line = param.head[j].line;
            var name = param.head[j].name;

            options.headers[name] = line;
        }
     }

     options.agent = false;
     return options;    
}

async function calfuc(req, res, msg,itf, exit) {
    req._$sdcm$_.uuid.msg = msg;

    if(req._$sdcm$_.uuid.cur >= req._$sdcm$_.uuid.max){
        if(exit == null || exit == false) {
            await last(itf, req, res, null,null);
        }
        req._$sdcm$_.objc = null;
        req._$sdcm$_.rslt = null;
        req._$sdcm$_ = null;
    }  
}

function remove(path) {
    fs.exists(path, function (exists) {
        if(exists){
            fs.unlink(path);
        }
    });   
}

sock.file = function(array, itf) {
    array.forEach(function(the){
        if(itf.cftp != null) {
            var ftpClient = new ftp();  
            ftpClient.on('ready', function() {
                ftpClient.put(the.path, itf.cftp.path+the.originalFilename, function(err) {
                    if (err) {
                        logj.strerr("call-ftp-err2", the.path, err);                      
                    }

                    ftpClient.end();
                    ftpClient.destroy();

                    remove(the.path);                             
                });
            });         
            ftpClient.connect(itf.cftp); 
        } else if(itf.coss != null) {
            var client = new aliOss(itf.coss.craa);
            co(function* () {
                client.useBucket(itf.coss.bucket);
                var rslt = yield client.put(itf.coss.object + "/" + the.originalFilename, the.path);
                logj.logger().info("call-oss-info path=[%s] rslt=[%s]", the.path, JSON.stringify(rslt));    
            }).catch(function (err) {
                logj.strerr("call-oss-err2", the.path, err);  
            });          
        }             
    });

    return true;
}

sock.object = function(itf, opt, fuc) {
    if(itf.type == 'hssl') {
        return hssl.request(opt, fuc);
    }

    return http.request(opt, fuc);
}

sock.cfunc = function (itf, req, res, fld, fle) {
    try{
        return itf.func (req, res, fld, fle); 
    }catch(err) {
        res.status(500).jsonp("inner3 err!!");        
        logj.reqerr("call-func-err1", req, err); 
        return {exit: true};
    }
}

sock.httpimpl = async function(cfg, itf, req, res, fld, fle, arr) {
    if(itf.resf == null) {  itf.resf = "json";  }
    req._$sdcm$_.rslt[itf.uuid] = {succ:false, objv:null};
    if(req._$sdcm$_.exit) {
        req._$sdcm$_.uuid.cur = req._$sdcm$_.uuid.cur + 1; 
        calfuc(req, res, itf.uuid + ': exit', itf, req._$sdcm$_.exit);        
        return false;
    }

    var param = null;
    try {
        param = await sock.cfunc(itf, req, res, fld, fle);
    }catch(err){
        if(!param) {
            param = {};
            res.status(500).
                jsonp("inner1 err!!");
        }
        param.exit = true;
        logj.reqerr("call-err", req, err); 
    }

    if(arr != null && !req._$sdcm$_.file) {
        req._$sdcm$_.file = true;
        sock.file(arr, itf);
    }

    if(param.stop || param.exit) {
        req._$sdcm$_.rslt[itf.uuid].succ = true;
        req._$sdcm$_.exit = param.exit ? true : false;
        req._$sdcm$_.uuid.cur = req._$sdcm$_.uuid.cur + 1; 

        calfuc(req, res, itf.uuid + ': func stop', itf, param.exit);
        return false;
    }

    if(param.jump) {
        req._$sdcm$_.rslt[itf.uuid].succ = true;
        req._$sdcm$_.uuid.cur = req._$sdcm$_.uuid.cur + 1;        
        
        if(itf.next != null && itf.next.length > 0) {
            sock.next(cfg, itf.next, req, res, fld, fle);
        }else{
            calfuc(req, res, itf.uuid + ': func jump', itf);
        }
        return true;
    }

    var body = '',sody = null;
    var option = sock.prepare (itf, param, req._$sdcm$_.user, req, res);
    var object = sock.object(itf, option, function(output){    
         let encode = 'utf8'; if(req._$sdcm$_.conf.type == 'file') {
            encode = 'binary';
         }
         output.setEncoding(encode);
         output.on('data',function(d){
             body += d;
         }).on('end', function() {
            if(!output.headers['errs']) {
                req._$sdcm$_.uuid.cur = req._$sdcm$_.uuid.cur + 1;
                if(req._$sdcm$_.conf.type == "file" || itf.resf != "json") {
                    sody = body;
                }else{
                    try{
                        sody = JSON.parse(body);
                    }catch(err){              
                        req._$sdcm$_.rslt[itf.uuid].objv = err;  
                        calfuc(req, res, itf.uuid + ": " + body, itf);  
                        logj.strerr("call-err", option.path, body);            
                        return;
                    }
                }

                req._$sdcm$_.rslt[itf.uuid].objv = sody;
                req._$sdcm$_.rslt[itf.uuid].succ = true;

                if(itf.next && itf.next.length > 0){
                    sock.next(cfg, itf.next, req, res, fld, fle);
                }else{
                    calfuc(req, res, itf.uuid, itf); 
                }
            }else{ 
                req._$sdcm$_.uuid.cur = req._$sdcm$_.uuid.cur + 1;
                logj.strerr("call-err", option.path, body, itf); 
                req._$sdcm$_.rslt[itf.uuid].objv = output.headers['errs']; 
	            calfuc(req, res, itf.uuid + ": " + output.headers);             
            }
         }); 
     });    

     object.on ('error', function(err) {  
        req._$sdcm$_.rslt[itf.uuid].objv = err;  
        req._$sdcm$_.uuid.cur = req._$sdcm$_.uuid.cur + 1; 
        calfuc(req, res, itf.uuid + ': server err', itf);   
        logj.strerr("call-err", option.path, "server err="+(!err ? '' : err.toString()));      
     });

     object.setNoDelay(true);
     object.setSocketKeepAlive(false);
     object.setTimeout(conf.timeout, function(){
     	object.abort();
     });

     if(itf.meth.toUpperCase() == 'POST' || itf.type.toUpperCase() == 'SDCM'){
        if(itf.type.toUpperCase() == 'SDCM') {
            object.write(JSON.stringify(!param.objv ? null : param.objv));
        }else{
            if(itf.reqf != null && itf.reqf.toUpperCase() == 'JSON') {
                object.write(JSON.stringify(param.objv));
            }else{
                object.write(qs.stringify(param.objv));
            }
        }
     }   
     object.end();

     return true;
}