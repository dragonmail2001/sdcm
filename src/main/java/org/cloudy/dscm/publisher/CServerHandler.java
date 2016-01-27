/*
 * Copyright 2015-2115 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @email   dragonmail2001@163.com
 * @author  jinglong.zhaijl
 * @date    2015-10-24
 *
 */
package org.cloudy.dscm.publisher;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudy.dscm.common.CConf;
import org.cloudy.dscm.common.CParameter;
import org.cloudy.fastjson.JSON;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.util.AttributeKey;

public class CServerHandler extends SimpleChannelInboundHandler<Object> {
	
	private CServerInitializer initializer;
	
	public CServerHandler(CServerInitializer httpserverInitializer) {
		this.initializer = httpserverInitializer;
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		initializer.addChannel(ctx.channel());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		super.channelRead(ctx, msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception {
		super.channelWritabilityChanged(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		super.exceptionCaught(ctx, cause);
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg)
			throws Exception {

		AttributeKey<Map<String,Object>> paramKey = AttributeKey.valueOf("param"); 
		AttributeKey<HttpRequest> requestKey = AttributeKey.valueOf("request"); 
		
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			if (!request.getMethod().equals(HttpMethod.POST)) {
				writeResponse(request.getUri(), ctx.channel(), request, RuntimeException.class.getName(), "err",
						JSON.toJSONString(new RuntimeException("dscm-only-post")));
				clear(ctx);
				return;
			}
			
    		Map<String,Object> param = ctx.attr(paramKey).get();
    		if(param == null) {
    			param = new HashMap<String,Object>();
    			ctx.channel().attr(paramKey).set(param);
    		}

            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
                param.put(attr.getKey(), attr.getValue().size() > 0 ? attr.getValue().get(0) : null);
            } 
            param.put(CConf.CTXNAME, decoderQuery.path().substring(1));
            io.netty.util.Attribute<HttpRequest> requestVal = ctx.channel().attr(requestKey);
            requestVal.set(request);         
		}
		
		HttpRequest request = ctx.channel().attr(requestKey).get();
		Map<String,Object> param = ctx.channel().attr(paramKey).get();

        if (msg instanceof HttpContent) {
            // New chunk is received
            HttpContent chunk = (HttpContent) msg;
            // example of reading only if at the end
            if (chunk instanceof LastHttpContent) {	
            	Object bean = null;
            	Method method= null;
            	List<?> args = null;
            	Class<?>[] types = null;            	
            	String actn = (String) param.get(CConf.ACTN);
            	String claz = request.headers().get(CConf.CLAZ);
//            	String sync = request.headers().get(CConf.SYNC);
            	try {
                	byte[] buf = new byte[chunk.content().readableBytes()];
                	chunk.content().readBytes(buf);
        			String json = new String(buf,CConf.UTF8); 
        			
        			if(actn == null || claz == null) {
        				throw new RuntimeException("actn or claz is null");
        			}
        			
        			if(CServer.getInstance().debug()) {
	        			CServer.logger().log(actn);
	        			CServer.logger().log(claz);
	        			CServer.logger().log(json);
        			}
        			
        			bean = CServer.getInstance().getBean((String)param.get(CConf.CTXNAME));
            		types = CParameter.parse(JSON.parseArray(claz, String.class));
            		method= bean.getClass().getMethod(actn, types);
 
            		args = JSON.parseArray(json, types);
            	}catch(Exception exc) {
            		writeResponse(actn, ctx.channel(), request, RuntimeException.class.getName(),
            				 "err", JSON.toJSONString(exc.getMessage()));
            		return;
            	}
            	
            	String body = null, err = null;
            	try {
            		Object object = method.invoke(bean, args == null ? null : args.toArray());
            		body = JSON.toJSONString(object, CConf.FEATURE);
            		claz = method.getReturnType().getName();
            	}catch(Exception exc) {
            		Throwable throwable = (exc.getCause() == null ? exc : exc.getCause());
            		CServer.logger().log(throwable.getMessage());
            		claz = throwable.getClass().getName();
            		body = JSON.toJSONString(throwable);
            		err = "err";
            	}
            	
        		writeResponse(actn, ctx.channel(), request, claz, err, body);
            	clear(ctx);    	
            }		
		}
	}
	
	private void clear(ChannelHandlerContext ctx) {
		AttributeKey<HttpPostRequestDecoder> decoderKey = AttributeKey.valueOf("decoder");  
		AttributeKey<Map<String,Object>> paramKey = AttributeKey.valueOf("param"); 
		AttributeKey<HttpRequest> requestKey = AttributeKey.valueOf("request");
		
        ctx.channel().attr(decoderKey).set(null);
        ctx.channel().attr(requestKey).set(null);	
        ctx.channel().attr(paramKey).set(null);
	}

    private void writeResponse(String actn, Channel channel,HttpRequest request, String claz, String errs, String body) {
        // Convert the response content to a ChannelBuffer.
    	ByteBuf buf = Unpooled.copiedBuffer(body, CConf.CUTF);
    
        // Decide whether to close the connection or not.
        boolean close = request.headers().contains(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE, true)
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !request.headers().contains(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);
 
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset="+CConf.UTF8);
        if(claz != null) response.headers().set(CConf.CLAZ, claz);
        if(errs != null) response.headers().set(CConf.ERRS, errs);
 
        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        }
 
//        Set<Cookie> cookies;
//        String value = request.headers().get(COOKIE);
//        if (value == null) {
//            cookies = Collections.emptySet();
//        } else {
//            cookies = CookieDecoder.decode(value);
//        }
//        if (!cookies.isEmpty()) {
//            // Reset the cookies if necessary.
//            for (Cookie cookie : cookies) {
//                response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
//            }
//        }
        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
        
        if(CServer.getInstance().debug()) {
        	CServer.logger().log(new StringBuilder(body.length() + actn.length() + 3).
        			append(actn).append("::").append(body));
        }
    }	
}
