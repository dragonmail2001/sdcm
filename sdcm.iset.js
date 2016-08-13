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
var getContextName = require('./sdcm.util.js').getContextName; 
var getClientIp = require('./sdcm.util.js').getClientIp;  
var conf = require('./sdcm.conf.js');
var path = require('path');
var url = require('url');

var iset = exports = module.exports = {};

iset.set = function(req, res) {
    req.user = {};
    req.conf = {};
    req.rslt = {};
    req.uuid = {};

    req.uuid.max = 0;
    req.uuid.cur = 0;
    //req.uuid.upf = 0;
    req.uuid.msg = 'ok';
    req.uuid.err = false;
    req.uuid.jum = false;
    req.uuid.moc = false;
    req.uuid.tim = new Date();
    req.uuid.app = '';

    req.conf.btpl = false;

    req.user.code = req.session.code;
    req.user.user = req.session.user;
    req.user.addr = getClientIp(req); 

    var cctx = req.baseUrl.split('/');
    if(cctx.length <= 0) {
        res.jsonp({"code": -900000,
            "message": 'ctxerr',
            "success": false
        });
        return false;
    } 

    if(cctx.length > 2) { req.uuid.app = cctx[1]; }

    req.conf.name = getContextName(cctx[cctx.length-1]);
    cctx.splice(cctx.length-1, 1);
    req.conf.dcfg = path.join(conf.dcfg, cctx.join('/'), req.conf.name + '.cfg')
    req.conf.dtpl = path.join(conf.dcfg, cctx.join('/'), req.conf.name + '.htm'); 
    return true;      
}