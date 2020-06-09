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

var logj = require('./sdcm.logj.js');
var sock = require('./sdcm.sock.js');

exports = module.exports = async function form(req, res, next) {
    var cfg = req._$sdcm$_.conf.ocfg;
    for(var i=0; i<cfg.itfs.length; i++){
        req._$sdcm$_.uuid.max = req._$sdcm$_.uuid.max + 1; 
        
        var bret = await sock.httpimpl(cfg, cfg.itfs[i], 
            req, res, null,null);
        if(!bret || req._$sdcm$_.exit) {
            break;
        }
    }
};

