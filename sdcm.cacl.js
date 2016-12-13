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

var logj = require('./sdcm.logj.js');
var iset = require('./sdcm.iset.js');
var conf = require('./sdcm.conf.js'); 
var wildcard = require('wildcard2');

exports = module.exports = function cacl(req, res, next) {
    if(!iset.set(req, res)) { 
        res.jsonp({"code": -500000,
            "message": 'enverr',
            "success": false
        });  

        logj.reqerr("call-cacl-err0", req, 'enverr');          
        return; 
    }
    var baseUrl = req.baseUrl;
    var host = req.headers.host;
    baseUrl = baseUrl.substring(1);
    var context = baseUrl.substring(0,baseUrl.indexOf("/"));
    var rule = conf.cacl[context];
    
    if (rule) {
        var access = true;
        if(rule.allow&&rule.allow.length>0){
            access = false;
            for (var i = 0; i < rule.allow.length; i++) {
                if(wildcard(host,rule.allow[i])){
                    access = true;
                    break;
                }
            }
        }
        if(access&&rule.deny&&rule.deny.length>0){
            for (var i = 0; i < rule.deny.length; i++) {
                if(wildcard(host,rule.deny[i])){
                    access = false;
                    break;
                }
            }
        }
        if(!access){
            res.status(403).jsonp({"code": -400000,
                "message": 'caclforbidden',
                "success": false
            });
            return;
        }
    }
    next();
};

