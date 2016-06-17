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
var ejs = require('./sdcm.eejs.js');  
var iset = require('./sdcm.iset.js');
var conf = require('./sdcm.conf.js');
var logj = require('./sdcm.logj.js').getLogger;

exports = module.exports = function html(req, res, next) {
    if(!iset.set(req, res)) {
        logj('main').error("set-html-err [%s][%s][%s]", new Date().getTime() - req.uuid.tim.getTime(),
            JSON.stringify(req.conf), html);
        return;
    }

    var html = null;
    try {
        if(conf.debug){
            html = ejs.render(null, {user:req.user,rslt:req.rslt},
                {cache:false,filename: req.conf.dtpl});
        }else{
            html = ejs.render(null, {user:req.user,rslt:req.rslt},
                {cache:true,filename: req.conf.dtpl});
        }
    }catch(exc) {
        html = "name: " + exc.name + "message: " + exc.message +
            "lineNumber: " + exc.lineNumber + "fileName: " + exc.fileName +
            "stack: " + exc.stack;

        logj('main').error("call-html-err [%s][%s][%s]", new Date().getTime() - req.uuid.tim.getTime(),
            JSON.stringify(req.conf));
    }

    res.writeHead(200, {
      'Content-Length': new Buffer(html,'utf-8').length,
      'Content-Type': 'text/html;encode=UTF-8'
    });
    res.end(html);
};

