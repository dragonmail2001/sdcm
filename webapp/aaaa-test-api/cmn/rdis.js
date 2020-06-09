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
//redis 集群 相关配置，必须这样引入redis否则每个文件都引用可能会连接数过大
var rdis = require(process.cwd()+'/sdcm.rdis.js');
var ghost = require(process.argv[2]+'/aaaa-test-api/cfg/ghost.js'); 
var rdisObjc = new rdis(ghost.rdis.host, ghost.rdis.prefix, ghost.rdis.cluster);  
exports = module.exports = rdisObjc;