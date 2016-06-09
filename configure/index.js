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
var fs = require('fs');
var path = require('path');

var config  = {
    enableCluster: false,
    httpport: 8001,
    timeout:20000,
    debug: true
};

var customConfig = path.join(__dirname, 'config.js');
if (fs.existsSync(customConfig)) {
  	var options = require(customConfig);
  	for (var k in options) {
    	config[k] = options[k];
  	}
}

module.exports = config;