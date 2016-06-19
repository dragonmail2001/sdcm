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
var http = require('http');
var express = require('express');
var cluster = require('cluster');
var graceful = require('graceful');
var bodyParser = require('body-parser'); 
var session = require('express-session');
var cookieParser = require('cookie-parser');

var conf = require('./sdcm.conf.js');
var logj = require('./sdcm.logj.js');
var code = require('./sdcm.code.js');
var form = require('./sdcm.form.js');
var file = require('./sdcm.file.js');
var html = require('./sdcm.html.js');

var numCPUs = require('os').cpus().length;
var cach = require('./sdcm.cach.js')(session);

function createSdcmObject() {
    var app = express();
    app.use(session({
        store: new cach(conf.cach),        
        saveUninitialized: false,
        secret: conf.sess.key,
        name: conf.sess.name,
        resave:true,
        cookie: {
            domain: conf.sess.domain,
            maxAge: conf.sess.time
        }
    }));
    app.use(cookieParser(conf.sess.key));
    app.use(bodyParser.urlencoded({ extended: true }));
    app.use(bodyParser.json());
    app.use('*.cci', code);
    app.use('*.cgi', form);
    app.use('*.cfi', file);
    app.use('*.htm', html);
    app.use(express.static(conf.dcfg)); 
    return app; 
}

if (!conf.cluster) {
    var app = createSdcmObject();
    app.listen(conf.httpport);
    console.log('[%s] [worker:%d] Server started, listen at %d', new Date(), process.pid, conf.httpport);
    logj.logger().info('[worker:%d] Server started, listen at %d', process.pid, conf.httpport);

    graceful({
        server: [app],
        error: function (err, throwErrorCount) {
            if (err.message) {
                err.message += ' (uncaughtException throw ' + throwErrorCount + ' times on pid:' + process.pid + ')';
            }

            console.error("[%s] [worker:%d] stack [%s] err[%s]"+err.stack,new Date(), process.pid, err.stack, err);
            logj.logger().error('[worker %d failed], stack [%s] err[%s]', process.pid, err.stack, err);
        }
    });
} else {
    if (cluster.isMaster) {
        console.log('[%s] [master:%d] Master started, listen at %d', new Date(), process.pid, conf.httpport);
        logj.logger().info('[master:%d] Master started, listen at %d', process.pid, conf.httpport);

        for (var i = 0; i < numCPUs; i++) {
            cluster.fork();
        }
   
        cluster.on('listening', function (worker, address) {
            console.log('[listening] worker id=%d,pid=%d, port=%d', worker.id ,worker.process.pid, address.port);
            logj.logger().info('[listening] worker id=%d,pid=%d, port=%d', worker.id ,worker.process.pid, address.port);
        });
   
    } else if (cluster.isWorker) {
        var app = createSdcmObject();
        app.listen(conf.httpport);
        console.log('[worker:%d] Worker started, listen at %d', cluster.worker.id, conf.httpport);
        logj.logger().info('[worker:%d] Worker started, listen at %d', cluster.worker.id, conf.httpport);
    }
}