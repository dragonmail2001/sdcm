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
var ejs = require('./ejs/ejs.js'); 
var conf = require('./configure');
var logj = require('./sdcm.logj.js');

exports.getParameter = function getParameter(req,name) {
    var value = req.query[name];
    if(value == null){
        value = req.body[name];
    }

    return value;
}

exports.getClientIp = function getClientIp(req) {
    return req.headers['x-forwarded-for'] ||
    req.connection.remoteAddress ||
    req.socket.remoteAddress ||
    req.connection.socket.remoteAddress;
}

exports.getContextName = function getContextName(ctx) {
    if(!ctx || ctx.length <= 0) {
        return '';
    }

    var pos = 0, cur = 0;
    for (var i=0;i<ctx.length;i++){
        var c = ctx.charAt(i);
        if(c == '.') {
            pos = cur;
        } 

        cur++;
    } 

    return ctx.substr(0,pos);
}


exports.loadConf = function loadConf(module) {
    var result = null;
    try {
        if(conf.debug){
            delete require.cache[require.resolve(module)];
        }
        result = require(module).itfconf();
    } catch (exc) {
        logj.getLogger('main').error("load-url-err [%s][%s][%s][%s][%s][%s]", module, 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack);        
        result=null;
    }
    return result;
}

function renderHtml(rslt, req, res, fld, fle) {
    var name = req.conf.dtpl;
    var html = null;
    try {
        if(conf.debug){
            html = ejs.render(null, {user:req.user,rslt:req.rslt},{cache:false,filename: req.conf.dtpl}); 
        }else{
            html = ejs.render(null, {user:req.user,rslt:req.rslt},{cache:true,filename: req.conf.dtpl}); 
        }

        res.writeHead(200, {'Content-Type': 'text/html;encode=UTF-8'});
    }catch(exc) {
        res.writeHead(500, {'Content-Type': 'text/html;encode=UTF-8'});
        logj.getLogger('main').error("load-mod-err [%s] [%s] [%s] [%s] [%s]", 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack); 
        html = "name: " + exc.name + 
            "message: " + exc.message + 
            "lineNumber: " + exc.lineNumber + 
            "fileName: " + exc.fileName + 
            "stack: " + exc.stack;
        if(conf.debug) {
            res.write('err:'+html);
            res.write('<br/><br/>');            
            res.write('err:'+JSON.stringify(rslt,null,4));
            res.write('<br/><br/>');
        }    
    }

    res.end(html); 
}

exports.loadLast = function loadLast(cfg, req, res, fld, fle) {
    var rslt = null;
    try {
        if(conf.debug) {
            delete require.cache[require.resolve(req.conf.dcfg)];
        }            
        rslt = require(req.conf.dcfg).itfleft(req, res, fld, fle);
    } catch (exc) {
        logj.getLogger('main').error("load-url-err [%s] [%s] [%s] [%s] [%s]", 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack);        
        rslt = exc;
    }

    if(rslt) {
        if(cfg.type != 'json') {
            renderHtml(rslt, req, res, fld, fle);
        }else{
            res.jsonp(rslt);
        }
    }
}