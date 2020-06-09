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

var config  = {   
    cookie: "sdcm.cookie",
    cluster: false,
    httpport: 8001,
    timeout: 20000,
    debug: true
};

module.exports = config;
//node sdcm.main.js /Workspace/jxg/cloudy-sdcm-jwt/webapp /Workspace/jxg/logger /Workspace/jxg/upload