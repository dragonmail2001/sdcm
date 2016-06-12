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
var util = require("util"); 
var ioredis = require('ioredis');
var conf = require('./configure'); 
var noop = function(){};

var oneDay = 86400;

function getTTL(store, sess) {
  var maxAge = sess.cookie.maxAge;
  return store.ttl || (typeof maxAge === 'number'
    ? Math.floor(maxAge / 1000)
    : oneDay);
}

module.exports = function (session) {

    var Store = session.Store;
    var cluster = null;//new ioredis.Cluster(conf.cach);    

    function CachStore (options) {
        if (!(this instanceof CachStore)) {
            throw new TypeError('Cannot call CachStore constructor as a function');
        }

        var self = this;

        options = options || {};
        Store.call(this, options);

        if(!cluster) {
            cluster = new ioredis.Cluster(options); 
        }

        this.serializer = options.serializer || JSON;
        this.prefix = conf.sess.name;
    }

    util.inherits(CachStore, Store);

    CachStore.prototype.get = function (sid, fn) {
        var store = this;
        var psid = store.prefix + sid;
        if (!fn) fn = noop;

        cluster.get(psid, function (err, data) {
            if(err) return fn(er);
            if (!data) return fn();

            var result;
            data = data.toString();

            try {
                result = store.serializer.parse(data);
            }
            catch (exc) {
              return fn(exc);
            }           

            return fn(null, result);
        });
    };

    CachStore.prototype.set = function (sid, sess, fn) {
        var store = this;
        var psid = store.prefix + sid;
        var args = [store.prefix + sid];

        if (!fn) fn = noop;

        try {
          var jsess = store.serializer.stringify(sess);
        }
        catch (er) {
          return fn(er);
        }

        args.push(jsess);

        var ttl = getTTL(store, sess);
        args.push('EX', ttl);


        cluster.set(psid, jsess, 'EX', ttl);
        fn.apply(null, arguments); 
        //fn.apply(this);
    };

    CachStore.prototype.destroy = function (sid, fn) {
        sid = this.prefix + sid;
        if (!fn) fn = noop;
        cluster.del(sid, fn);
    };

    CachStore.prototype.touch = function (sid, sess, fn) {
        var store = this;
        var psid = store.prefix + sid;
        if (!fn) fn = noop;
        if (store.disableTTL) return fn();

        var ttl = getTTL(store, sess);
        cluster.expire(psid, ttl, function (er) {
            if (er) return fn(er);
            fn.apply(this, arguments);
        });
    };

    return CachStore;
};
