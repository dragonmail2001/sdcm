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
const ioredis = require('ioredis');
const Server = require('socket.io')

const conf = require("./sdcm.conf.js");

/**
 * Module dependencies.
 */

var uid2 = require('uid2');
var msgpack = require('msgpack-lite');
var Adapter = require('socket.io-adapter');
var debug = require('debug')('socket.io-redis');
var async = require('async');

/**
 * Module exports.
 */

module.exports = adapter;

/**
 * Request types, for messages between nodes
 */

var requestTypes = {
    clients: 0,
    clientRooms: 1,
};

/**
 * Returns a redis Adapter class.
 *
 * @param {String} optional, redis uri
 * @return {RedisAdapter} adapter
 * @api public
 */

// init clients if needed
function createClient() {
    if (conf.ccps.cluster)
        return new ioredis.Cluster(conf.cach);
    else
        return new ioredis(conf.cach[0]);
}

function adapter(uri, opts) {
    opts = opts || {};

    // handle options only
    if ('object' == typeof uri) {
        opts = uri;
        uri = null;
    }

    // opts
    var pub = opts.pubClient;
    var sub = opts.subClient;

    var prefix = opts.key || 'socket.io';
    var subEvent = opts.subEvent || 'messageBuffer';
    var requestsTimeout = opts.requestsTimeout || 1000;
    var withChannelMultiplexing = false !== opts.withChannelMultiplexing;

    if (!pub) pub = createClient();
    if (!sub) sub = createClient();

    // this server's key
    var uid = uid2(6);

    /**
     * Adapter constructor.
     *
     * @param {String} namespace name
     * @api public
     */

    function Redis(nsp) {
        Adapter.call(this, nsp);

        this.uid = uid;
        this.prefix = prefix;
        this.requestsTimeout = requestsTimeout;
        this.withChannelMultiplexing = withChannelMultiplexing;

        this.channel = prefix + '#' + nsp.name + '#';
        this.requestChannel = prefix + '-request#' + this.nsp.name + '#';
        this.responseChannel = prefix + '-response#' + this.nsp.name + '#';
        this.requests = {};

        if (String.prototype.startsWith) {
            this.channelMatches = function (messageChannel, subscribedChannel) {
                return messageChannel.startsWith(subscribedChannel);
            }
        } else { // Fallback to other impl for older Node.js
            this.channelMatches = function (messageChannel, subscribedChannel) {
                return messageChannel.substr(0, subscribedChannel.length) === subscribedChannel;
            }
        }
        this.pubClient = pub;
        this.subClient = sub;

        var self = this;

        sub.subscribe([this.channel, this.requestChannel, this.responseChannel], function (err) {
            if (err) self.emit('error', err);
        });

        sub.on(subEvent, this.onmessage.bind(this));
    }

    /**
     * Inherits from `Adapter`.
     */

    Redis.prototype.__proto__ = Adapter.prototype;

    /**
     * Called with a subscription message
     *
     * @api private
     */

    Redis.prototype.onmessage = function (channel, msg) {
        channel = channel.toString();

        if (this.channelMatches(channel, this.requestChannel)) {
            return this.onrequest(channel, msg);
        } else if (this.channelMatches(channel, this.responseChannel)) {
            return this.onresponse(channel, msg);
        } else if (!this.channelMatches(channel, this.channel)) {
            return debug('ignore different channel');
        }

        var args = msgpack.decode(msg);
        var packet;

        if (uid == args.shift()) return debug('ignore same uid');

        packet = args[0];

        if (packet && packet.nsp === undefined) {
            packet.nsp = '/';
        }

        if (!packet || packet.nsp != this.nsp.name) {
            return debug('ignore different namespace');
        }

        args.push(true);

        this.broadcast.apply(this, args);
    };

    /**
     * Called on request from another node
     *
     * @api private
     */

    Redis.prototype.onrequest = function (channel, msg) {
        var self = this;
        var request;

        try {
            request = JSON.parse(msg);
        } catch (err) {
            self.emit('error', err);
            return;
        }

        debug('received request %j', request);

        switch (request.type) {

            case requestTypes.clients:
                Adapter.prototype.clients.call(self, request.rooms, function (err, clients) {
                    if (err) {
                        self.emit('error', err);
                        return;
                    }

                    var response = JSON.stringify({
                        requestid: request.requestid,
                        clients: clients
                    });

                    pub.publish(self.responseChannel, response);
                });
                break;

            case requestTypes.clientRooms:
                Adapter.prototype.clientRooms.call(self, request.sid, function (err, rooms) {
                    if (err) {
                        self.emit('error', err);
                        return;
                    }

                    if (!rooms) { return; }

                    var response = JSON.stringify({
                        requestid: request.requestid,
                        rooms: rooms
                    });

                    pub.publish(self.responseChannel, response);
                });
                break;

            default:
                debug('ignoring unknown request type: %s', request.type);
        }
    };

    /**
     * Called on response from another node
     *
     * @api private
     */

    Redis.prototype.onresponse = function (channel, msg) {
        var self = this;
        var response;

        try {
            response = JSON.parse(msg);
        } catch (err) {
            self.emit('error', err);
            return;
        }

        if (!response.requestid || !self.requests[response.requestid]) {
            debug('ignoring unknown request');
            return;
        }

        debug('received response %j', response);

        var request = self.requests[response.requestid];

        switch (request.type) {

            case requestTypes.clients:
                request.msgCount++;

                // ignore if response does not contain 'clients' key
                if (!response.clients || !Array.isArray(response.clients)) return;

                for (var i = 0; i < response.clients.length; i++) {
                    request.clients[response.clients[i]] = true;
                }

                if (request.msgCount === request.numsub) {
                    clearTimeout(request.timeout);
                    if (request.callback) process.nextTick(request.callback.bind(null, null, Object.keys(request.clients)));
                    delete self.requests[request.requestid];
                }
                break;

            case requestTypes.clientRooms:
                clearTimeout(request.timeout);
                if (request.callback) process.nextTick(request.callback.bind(null, null, response.rooms));
                delete self.requests[request.requestid];
                break;

            default:
                debug('ignoring unknown request type: %s', request.type);
        }
    };

    /**
     * Broadcasts a packet.
     *
     * @param {Object} packet to emit
     * @param {Object} options
     * @param {Boolean} whether the packet came from another node
     * @api public
     */

    Redis.prototype.broadcast = function (packet, opts, remote) {
        packet.nsp = this.nsp.name;
        if (!(remote || (opts && opts.flags && opts.flags.local))) {
            var self = this;
            var msg = msgpack.encode([uid, packet, opts]);
            if (self.withChannelMultiplexing && opts.rooms && opts.rooms.length === 1) {
                pub.publish(self.channel + opts.rooms[0] + '#', msg);
            } else {
                pub.publish(self.channel, msg);
            }
        }
        Adapter.prototype.broadcast.call(this, packet, opts);
    };

    /**
     * Subscribe client to room messages.
     *
     * @param {String} client id
     * @param {String} room
     * @param {Function} callback (optional)
     * @api public
     */

    Redis.prototype.add = function (id, room, fn) {
        debug('adding %s to %s ', id, room);
        var self = this;
        Adapter.prototype.add.call(this, id, room);

        if (!this.withChannelMultiplexing) {
            if (fn) fn(null);
            return;
        }
        var channel = this.channel + room + '#';
        sub.subscribe(channel, function (err) {
            if (err) {
                self.emit('error', err);
                if (fn) fn(err);
                return;
            }
            if (fn) fn(null);
        });
    };

    /**
     * Unsubscribe client from room messages.
     *
     * @param {String} session id
     * @param {String} room id
     * @param {Function} callback (optional)
     * @api public
     */

    Redis.prototype.del = function (id, room, fn) {
        debug('removing %s from %s', id, room);

        var self = this;
        var hasRoom = this.rooms.hasOwnProperty(room);
        Adapter.prototype.del.call(this, id, room);

        if (this.withChannelMultiplexing && hasRoom && !this.rooms[room]) {
            var channel = this.channel + room + '#';
            sub.unsubscribe(channel, function (err) {
                if (err) {
                    self.emit('error', err);
                    if (fn) fn(err);
                    return;
                }
                if (fn) fn(null);
            });
        } else {
            if (fn) process.nextTick(fn.bind(null, null));
        }
    };

    /**
     * Unsubscribe client completely.
     *
     * @param {String} client id
     * @param {Function} callback (optional)
     * @api public
     */

    Redis.prototype.delAll = function (id, fn) {
        debug('removing %s from all rooms', id);

        var self = this;
        var rooms = this.sids[id];

        if (!rooms) {
            if (fn) process.nextTick(fn.bind(null, null));
            return;
        }

        async.each(Object.keys(rooms), function (room, next) {
            self.del(id, room, next);
        }, function (err) {
            if (err) {
                self.emit('error', err);
                if (fn) fn(err);
                return;
            }
            delete self.sids[id];
            if (fn) fn(null);
        });
    };

    /**
     * Gets a list of clients by sid.
     *
     * @param {Array} explicit set of rooms to check.
     * @param {Function} callback
     * @api public
     */

    Redis.prototype.clients = function (rooms, fn) {
        if ('function' == typeof rooms) {
            fn = rooms;
            rooms = null;
        }

        rooms = rooms || [];

        var self = this;
        var requestid = uid2(6);

        pub.send_command('pubsub', ['numsub', self.requestChannel], function (err, numsub) {
            if (err) {
                self.emit('error', err);
                if (fn) fn(err);
                return;
            }

            numsub = parseInt(numsub[1], 10);

            var request = JSON.stringify({
                requestid: requestid,
                type: requestTypes.clients,
                rooms: rooms
            });

            // if there is no response for x second, return result
            var timeout = setTimeout(function () {
                var request = self.requests[requestid];
                if (fn) process.nextTick(fn.bind(null, new Error('timeout reached while waiting for clients response'), Object.keys(request.clients)));
                delete self.requests[requestid];
            }, self.requestsTimeout);

            self.requests[requestid] = {
                type: requestTypes.clients,
                numsub: numsub,
                msgCount: 0,
                clients: {},
                callback: fn,
                timeout: timeout
            };

            pub.publish(self.requestChannel, request);
        });
    };

    /**
     * Gets the list of rooms a given client has joined.
     *
     * @param {String} client id
     * @param {Function} callback
     * @api public
     */

    Redis.prototype.clientRooms = function (id, fn) {

        var self = this;
        var requestid = uid2(6);

        var rooms = this.sids[id];

        if (rooms) {
            if (fn) process.nextTick(fn.bind(null, null, Object.keys(rooms)));
            return;
        }

        var request = JSON.stringify({
            requestid: requestid,
            type: requestTypes.clientRooms,
            sid: id
        });

        // if there is no response for x second, return result
        var timeout = setTimeout(function () {
            if (fn) process.nextTick(fn.bind(null, new Error('timeout reached while waiting for rooms response')));
            delete self.requests[requestid];
        }, self.requestsTimeout);

        self.requests[requestid] = {
            type: requestTypes.clientRooms,
            callback: fn,
            timeout: timeout
        };

        pub.publish(self.requestChannel, request);
    };

    Redis.link = function (link, io) {
        Redis.io = io;
        Redis.subClient.subscribe(conf.ccps.link);
        Redis.subClient.on("message", function(channel, data) {
            if (channel != conf.ccps.link){
                return;
            }
            var message = JSON.parse(data);
            var socket = Redis.io.connected[message.id];
            if(socket != null) {
                socket.join(message.room);
            }
        });
    };

    Redis.sock = function (link, id, room) {
        this.pubClient.publish(link, JSON.stringify({"id":id,"room":room}));
    };

    Redis.uid = uid;
    Redis.pubClient = pub;
    Redis.subClient = sub;
    Redis.prefix = prefix;
    Redis.requestsTimeout = requestsTimeout;

    return Redis;

}

module.exports = function(server)  {
    var tcp = Server(server).adapter(adapter());
    var io = tcp.of(conf.ccps.namespace);  

    tcp.adapter().link(conf.ccps.link, io);  

    //socket部分
    io.on('connection', function (socket) {
        socket.emit(conf.ccps.id, socket.id);

        socket.on("disconnect", function() {
            //store.lrem("users", 0, socket.id);
            //delete sockets[socket.id];
        });

        socket.on(conf.ccps.join, function (frid, toid, room) {
            console.log("=========="+frid+"::"+toid+"::"+room);
            tcp.adapter().sock(conf.ccps.link, frid, room);
            tcp.adapter().sock(conf.ccps.link, toid, room);
        });        

        socket.on(conf.ccps.room, function (room, message) {
            io.to(room).emit('room', room, message);
        });
    });
}