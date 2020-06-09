/**
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
var conf = require('./sdcm.conf.js');  
var ioredis = require('ioredis'); 
var wildcard = require('wildcard2');
var util = require("util"); 
var noop = function(){};

function RedisStore (options, prefix, isCluster) {
    this.cluster = null;//new ioredis.Cluster(conf.cach);  

    options = options || {};
    if(!this.cluster) {
        if(isCluster) {
            this.cluster = new ioredis.Cluster(options); 
        }else{
            this.cluster = new ioredis(options[0]); 
        }
    }

    this.serializer = options.serializer || JSON;
    this.prefix = prefix;
}

RedisStore.prototype.set = function (key, val, ttl) {
    if(!ttl)
        this.cluster.set(key, val);
    else 
        this.cluster.set(key, val, 'EX', ttl);
};

RedisStore.prototype.get = function (key, fn) {
    if(fn != null) {
        this.cluster.get(key, function (err, data) {
            if(err) return fn(err);
            if (!data) return fn();

            return fn(null, data);
        });
    } else {
        return this.cluster.get(key).then(function (rslt) {
            return rslt;
        });
    }
};    

RedisStore.prototype.lpop = function (key, fn) {
    this.cluster.lpop(key, function (err, data) {
        if(err) return fn(err);
        if (!data) return fn();

        return fn(null, data);
    });
};  

RedisStore.prototype.rpush = function (key, val) {
    this.cluster.rpush(key, val);
};        

RedisStore.prototype.del = function (sid, fn) {
    sid = this.prefix + sid;
    if (!fn) fn = noop;
    this.cluster.del(sid, fn);
};

exports = module.exports = RedisStore;
//var cachObject = new RedisStore(conf.cach.host, conf.cach.prefix, conf.cach.cluster);
//module.exports = cachObject;

