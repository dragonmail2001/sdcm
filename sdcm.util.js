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
var http = require('http');
var hssl = require('https');

var conf = require('./sdcm.conf.js');
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

exports.loadConf = function loadConf(req, res) {
    var result = null; 
    var module = req._$sdcm$_.conf.dcfg;
    try {
        if(conf.debug){
            delete require.cache[require.resolve(module)];
        }
        //result = require(module).itfconf();
        var objc = require(module);      
        req._$sdcm$_.objc = new objc();  
        result = req._$sdcm$_.objc.conf();

        req._$sdcm$_.conf.type = result.type;
        req._$sdcm$_.conf.code = result.code;         
    } catch (exc) {
        //result=null;
        logj.strerr("call-loadConf-err", module, exc);                
    }  

    return result;
}

exports.loadLast = async function loadLast(cfg, req, res, fld, fle) {
    try {
        await req._$sdcm$_.objc.outc(req, res, fld, fle);
    } catch (exc) {
        res.status(500).jsonp("inner2 err!!");
        logj.reqerr("call-loadLast-err", req, exc);        
    }

    logj.reqinf('time compute', req, null);
}