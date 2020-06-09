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
var ghost = require(process.argv[2]+'/aaaa-test-api/cfg/ghost.js'); 
var cjwt = require(process.cwd()+'/sdcm.cjwt.js'); 

class Objc {
    constructor() {
        //为了保证在func中使用this
        this.conf = this.conf.bind(this);
        this.func = this.func.bind(this);
        this.outc = this.outc.bind(this);  

        this._fun = this._fun.bind(this);
        this._out = this._out.bind(this);     
    }  

    async func(req,res,fld,fle) {
		var blade = (req.headers && req.headers[ghost.cjwt.blade.name]);//passing traveller
		if(blade != null) {
			try {
		    	var tstr = blade.split(' ')[1];
		    	var cstr = cjwt.decode(tstr, ghost.cjwt.pkey);
		    	req._$cjwt$_.head.push({line:tstr, objv: cstr,
		    		name:ghost.cjwt.blade.name, 
		    		memo:ghost.cjwt.basic.memo
		    	});
			}catch(err) {
		        res.status(401).jsonp({"code": 401,
		            "message": 'auth err!!!',
		            "success": false
		        });  
				return {exit: true};
			}
		}

		if(req._$sdcm$_.conf.auth) {
			if(req._$cjwt$_.blade == null) {
		        res.status(401).jsonp({"code": 401,
		            "message": 'login need!!!',
		            "success": false
		        });  
				return {exit: true};
			}
		}

		//let value = await this.redis.getByKey("zjl");
		//console.log(value);
		return await this._fun(req,res,fld,fle);
    }

    async outc(req,res,fld,fle) {
        var rslt = await this._out(req,res,fld,fle);
        if(req._$cjwt$_.blade != null) {
        	var tstr = cjwt.encode(req._$cjwt$_.blade, ghost.cjwt.pkey);
        	tstr = ghost.cjwt.blade.memo +" "+tstr;
        	res.setHeader(ghost.cjwt.blade.name, tstr);
        }

        if(!rslt) {
        	res.status(500).jsonp("out null !!");
        	return rslt;
        }

        var code = req._$sdcm$_.conf.code;
        if(!req._$sdcm$_.conf.code) {
        	code = 200;
        }

        if(req._$sdcm$_.conf.type == 'json') {
            res.status(code).jsonp(rslt);
        } else {
        	var encode = 'utf-8';
        	if(req._$sdcm$_.conf.type == 'file'){
        		encode = 'binary';
        	}

        	var head = {}, buffer = Buffer.from(rslt, encode);
        	head['Content-Length'] = buffer.length,
            head['Content-Type'] = "image/jpeg"; 
            //head['Content-Disposition'] = "attachment; filename=test.jpg"; 
            res.writeHead(code, head); 
            res.write(buffer);          
            res.end();
        }
       
        return rslt;
	}

	//=================system function begin===================
	rslt(req, uuid) {
		return req._$sdcm$_.rslt[uuid];
	}

	log(conf, dat, uuid) {
		let param = {};
		let ccurl = dat.ctx.baseUrl;
		let cconf = JSON.stringify(conf);
		let ccreq = Object.keys(dat.ctx.query).length ? dat.ctx.query : dat.ctx.body;
		param = JSON.stringify([ccreq, dat.dat]);
		ghost.getLogger().info(`${ccurl} => ${uuid}::${cconf}::${param}`);
	}

	getParameter(req,name) {
	    var value = req.query[name];
	    if(value == null){
	        value = req.body[name];
	    }

	    return value;
	}	
}

//必须放在文件的最后面
exports = module.exports = { Objc } ;