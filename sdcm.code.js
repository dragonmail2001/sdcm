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
var sock = require('./sdcm.sock.js');

function generate1(){
    var rstr='', cstr = ['0','1','2','3','4','5','6','7','8','9','A','B','C', 'D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
    for(var i = 0; i < 4 ; i ++) {
         var id = Math.ceil(Math.random()*36)%36;
         rstr += cstr[id];
     }
    return rstr;
}


function generate2(){
    var rstr='', cstr = ['0','1','2','3','4','5','6','7','8','9'];
    for(var i = 0; i < 4 ; i ++) {
         var id = Math.ceil(Math.random()*10)%10;
         rstr += cstr[id];
     }
    return rstr;
}

exports = module.exports = function code(req, res, next) {
    req.session.code = generate2();
    sock.code(req, res, req.session.code);
    // req.session.code = "1111";
    // sock.code(req, res, "1111");
}