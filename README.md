总体架构说明【遵循约定大于配置】
一、sdcm是服务端数据转化矩阵的缩写；是为前端同学面向服务编程模式的实现提供中间件容器
二、前端浏览器请求直接命中sdcm服务，然后sdcm服务在映射到后端具体的一个或者多个服务上
三、一个前端请求可以映射到多个后端服务，sdcm允许在调用后端服务之前和之后进行适当的逻辑处理
四、具体原理就是根据请求中baseUrl信息映射找到相应的配置文件*.cfg，在*.cfg中声明了class，类中必须定义conf、func、outc
    sdcm根据conf获取后端服务接口的配置信息，以及当前请求的配置参数等，然后通过递归方式逐个调用后端服务，并且在调用每个
    后端服务之前激活接口配置的func函数，然后在所有服务接口调用完毕后激活outc函数，也就是说再一次请求中func可能是多次执行
    但是outc是只执行一次
五、*.cfg具体说明如下：
/**
 * 关于模块加载的路径说明
 * --------------------------------------------------------------------
 * 这里如果需要引入webapp目录下的相关js文件需要使用process.argv[2]+*.js的模式
 * process.argv[2]是程序启动时候传入的第一个参数“/Workspace/jxg/cloudy-sdcm/webapp”
 * 以node sdcm.main.js /Workspace/jxg/cloudy-sdcm/webapp /Workspace/jxg/logger /Workspace/jxg/upload为例
 * 如果需要引入容器目录下的文件则需要使用process.cwd()+*.js的模式，其中process.cwd() 代表node命令启动的目录
 */
var ghost = require(process.argv[2]+'/aaaa-test-api/cfg/ghost.js'); 
var cjwt = require(process.cwd()+'/sdcm.cjwt.js'); 

/**
 * 关于在请求过程中四个对象req、res、fld、fle的说明
 * 1、req是请求对象，这个对象包含了几乎所有关于请求的数据，req._$sdcm$_这个变量
 *    是容器保留，界定在业务系统中不要使用，如果使用也只能是只读模式，不可以更改
 *    但是可以在req中自定定义其它变量，再一次请求过后req中的变量会被容器自动销毁
 *    并且不同请求的req变量是互相独立的，不会互相之间出现资源竞争的情况；
 * 2、res是响应对象，用来响应请求；
 * 3、fld是请求对象，如果当前请求是上传文件的话会用到这个对象；
 * 4、fle是文件对象，如果当前请求是上传文件的话代表当前请求中的文件流；
 *
 * 关于*.cfg文件中声明的类在请求过程中的生命周期说明【容器运行原理】；
 * 1、*.cfg文件是在浏览器端请求到达网关后，根据请求中的路径req.baseUrl进行定位
 *    匹配那个*.cfg文件，然后通过nodejs的模块加载方式require(...)加载*.cfg中
 *    声明的class对象，然后根据class对象中定义的conf获取相关配置信息递归调用后
 *    端服务接口，在调用后端服务接口之前触发fun并且在所有服务端接口调用后触发out函数；
 * 2、关于func函数的参数和返回值说明参见func的说明；
 * 3、关于outc函数的返回值和参数说明参见outc的说明；
 */
class Objc {
    //对象的构造函数
    constructor() {
        //为了保证在func中使用this
        this.conf = this.conf.bind(this);
        this.func = this.func.bind(this);
        this.outc = this.outc.bind(this);  

        this._fun = this._fun.bind(this);
        this._out = this._out.bind(this);     
    }  

    /**
     * 返回当前服务的总配置，这个配置信息是key-value模式会被容器加载到req._$sdcm$_.conf.ocfg对象中
     * 后续只要能够访问到req就可以访问这个总配置对象；*号标注的是容器使用的，作为应用要避开这些名字并且
     * 在业务逻辑中只能读取不可写入或者变更这个req._$sdcm$_对象，原则上容器使用的key之外可
     * 以随意定义自己的k-v模式配置信息；
     * ----------------------------------------------------------
     * itfs是树状结构也就是说可以配置多个同级别接口或者父子接口
     * 后端接口被触发是按照在配置文件中的先后顺序触发规则如下：
     * 同级别的排在前面的先触发，父子关系的先后触发同时要看父节点的顺序
     * 如果父节点在前则会先于其它和父节点同级的节点之前触发【执行时间在父节点同级别的节点之前】
     */
    conf() {
        return {
            /**
             * 非容器使用的关键字
             * 容器不使用此参数，一般应用使用它来确定当前接口是否需要鉴权才能访问
             */
            auth : false,
            //代表返回的内容是什么格式：json、text、file等
            *type : "json|text|file", 
            //itfs是一个对象数组，其中的每个对象都代表一个服务的配置
            *itfs : [{
                /**
                 * 容器保留关键字
                 * meth可以是get、post或者
                 */
                *meth : "get|post",

                /**
                 * 容器保留关键字
                 * type可以http、https和sdcm，其中如果是sdcm的话上面的meth就只能是post
                 */                
                *type : "http|https|sdcm",

                /**
                 * 容器保留关键字
                 * uuid是后端服务接口在当前整个请求的声明过程中的唯一标识，如果有多个后端服务接口
                 * 则要求每个后端服务接口的uuid必须是唯一的【换句话说就是在这个js文件中必须唯一】
                 */                 
                *uuid : "Formget",  


                /**
                 * 容器保留关键字
                 * host是后端服务的接口ip地址 
                 * port是后端服务的端口号
                 * iurl是后端服务的请求路径 
                 */                                             
                *host : "127.0.0.1",                          
                *port : 8001,                                   
                *iurl : "/sdcm-test-api/api_mock.cgi", 

                /**
                 * 容器保留关键字
                 * func是调用后端服务之前要触发的函数  
                 */                                 
                *func : this.fun,

                /**
                 * 容器保留关键字
                 * next是下一级后端服务接口的配置 
                 */                  
                *next : []，

                /**
                 * 容器保留关键字
                 * 一下四项是上传文件时候按需要配置的，在容器中使用
                 * coss和cftp是必须二选一的，代表文件上传到oss或者ftp
                 * fext是文件扩展名控制，fmax是单个文件大小限制字节为单位
                 */                  
                *coss : {},
                *cftp : {},
                *fext : {},
                *fmax : 234234,

                /**
                 * 容器保留关键字
                 * 如果resf=null或者resf=json代表当前接口返回的内容格式为json
                 * 如果resf=text代表其它格式
                 */                   
                *resf : json|null|file
            }]
        };
    }    

    /**
     * 调用后端接口之前会触发此函数
     * req,res,fld,fle分别对应请求对象、响应对象、字段对象、文件流对象
     * 本函数返回值格式如下
     * {
     *  //如果fun函数中没有处理异常，但是发生异常的情况下exit会被自动设置成true
     *  //如果fun函数中用户设置了exit:true代表从此以后的所有下级接口或者同级接口
     *  //不在执行直接触发out函数，所以用户可能需要out接着处理相关逻辑【可通过设置类变量的方式实现】
     *  exit:true|false

     *  //如果fun函数中用户设置了jump:true代表当前后端接口不会被调用直接触发out函数
     *  //同时会继续执行父子接口和同级接口
     *  jump:true|false

     *  //如果fun函数中用户设置了stop:true代表从此以后的所有下级接口不在执行
     *  //同级的接口会继续执行        
     *  stop:true|false

     *  //如果fun函数中用户设置了iurl:**代表应用可以灵活控制服务端地址       
     *  iurl:***

     *  //后端服务请求头设置的特殊内容key-value模式
     *  head:
     *  //后端服务的请求内容，可以是key-value模式也可以是一个对象或者数组
     *  objv:
     * }
     */
     //本例中的逻辑首先从当前浏览器请求头中获取jwt信息并解析，然后在调用继承类的func函数
     //其它逻辑直接继承本类就可以了，是一个业务逻辑的简单封装
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

    /**
     * 调用后端接口之后会触发此函数
     * req,res,fld,fle分别对应请求对象、响应对象、字段对象、文件流对象
     * 
     * 容器只是触发此函数，对前端浏览器的响应需要应用自己实现，类似res.write(...)和res.end()
     * 后端服务接口的调用结果保存在req._$sdcm$_.rslt中，是一个数组根据每个接口配置红的uuid值
     * 来获取例如：req._$sdcm$_.rslt[uuid]，本类中定义了一个通用的工具函数rslt(req, uuid)
     * 具体例子代码如下：var retValue = rslt(req, "Formget");
     * 返回值是一个对象格式如下: {succ:true|false,objv:*}, 其中succ是false的情况代表后端接口
     * 调用失败，objv中保存的后端接口返回值，这个值可以是一个json对象【接口的配置中resf是null|json】
     * 也可以是其它值【接口的配置中resf不是null也不是json】具体参见conf函数的说明
     */
     //本例中的逻辑首先执行子类的outc函数，然后进行jwt的通用逻辑
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
            //如果浏览器请求下载文件的话封装文件流
            var encode = 'utf-8';
            if(req._$sdcm$_.conf.type == 'file'){
                encode = 'binary';
            }

            var head = {}, buffer = Buffer.from(rslt, encode);
            head['Content-Length'] = buffer.length,
            head['Content-Type'] = req._$sdcm$_.conf.ocfg.ctte; 
            if(req._$sdcm$_.conf.ocfg.ctdn) {
                head['Content-Disposition'] = req._$sdcm$_.conf.ocfg.ctdn; 
            }
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

    //日志逻辑
    log(conf, data, type) {
        let param = {};
        let ccurl = data.baseUrl;
        let cconf = JSON.stringify(conf);
        let ccreq = {}
        if(type != 'req') {
            ccurl = data.ctx.baseUrl;
            ccreq  = Object.keys(data.ctx.query).length ? data.ctx.query : data.ctx.body;
        }else{
            ccreq = Object.keys(data.query).length ? data.query : data.body;
            ccurl = data.baseUrl;
        }
        param = JSON.stringify([ccreq, data.dat]);
        ghost.getLogger().info(`${ccurl} => ${type}::${cconf}::${param}`);
    }

    //从请求中获取参数
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

六、容器的安装和启动
    在sdcm-cloudy-jwt目录下执行npm install 和npm install log4js@0.6.26 -g 视需要执行npm install jwt-simple
    启动命令
    node sdcm.main.js /Workspace/jxg/cloudy-sdcm/webapp /Workspace/jxg/logger /Workspace/jxg/upload

--------------------------------------sdcm end---------------------------------------------------------
