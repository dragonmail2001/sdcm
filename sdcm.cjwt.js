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

var cjwt = require('jwt-simple');
function CjwtObject() {

}

CjwtObject.prototype.decode = function (token, key, noVerify, algorithm) {
    return cjwt.decode(token, key, noVerify, algorithm);
};

CjwtObject.prototype.encode = function (token, key, noVerify, algorithm) {
    return cjwt.encode(token, key, noVerify, algorithm);
};

var cjwtObject = new CjwtObject();
module.exports = cjwtObject;