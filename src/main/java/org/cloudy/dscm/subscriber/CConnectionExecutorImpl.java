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
package org.cloudy.dscm.subscriber;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.CodingErrorAction;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.cloudy.dscm.common.CConf;
import org.cloudy.dscm.common.CParameter;
import org.cloudy.dscm.common.CLogger;
import org.cloudy.fastjson.JSON;

public class CConnectionExecutorImpl implements CConnectionExecutor {
	
	private PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
	private static final CConnectionExecutor connectionExecutor = new CConnectionExecutorImpl();
	
	public static CConnectionExecutor connectionExecutor() {
		return connectionExecutor;
	}
	
	private CConnectionExecutorImpl() {
		setConfigure();
		
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .build();
		connectionManager.setDefaultSocketConfig(socketConfig);
		
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(CConf.CUTF)
                .setMessageConstraints(MessageConstraints.custom().build())
                .build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);		
	}
	
	private CLogger logger;
	private boolean debug;
	private void setConfigure() {
		try {
			InputStream is = CConnectionExecutor.class.getClassLoader().
					getResourceAsStream("dscm.properties");
			
			Properties properties=new Properties();
			properties.load(is);
			is.close();
			
			int capaticy = CConf.toInt(properties.getProperty("capaticy"));
			long delay = CConf.toLong(properties.getProperty("delay"));
			int fmax = CConf.toInt(properties.getProperty("fmax"));
			int lmax = CConf.toInt(properties.getProperty("lmax"));
			int pool = CConf.toInt(properties.getProperty("pool"));
			debug = CConf.toBool(properties.getProperty("debug")); 
			String path = properties.getProperty("path");
			
			int maxTotal = CConf.toInt(properties.getProperty("maxTotal"));
			int defaultMaxPerRoute = CConf.toInt(properties.getProperty("defaultMaxPerRoute"));
			
			this.connectionManager.setMaxTotal(maxTotal);
			this.connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
			
			logger = new CLogger(capaticy, delay, fmax, lmax, pool, path, "clt");
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	private CloseableHttpClient getHttpClient() {
		return HttpClients.custom().setConnectionManager(connectionManager).build();
	}	
	
	private CParameter post(CloseableHttpClient httpClient, String url, String method, String claz, String json, String sync) throws Throwable {
		String content = null;
		CParameter result = new CParameter();
		CloseableHttpResponse response = null;

		HttpPost httpPost = new HttpPost(new StringBuilder(url.length() + CConf.ACTN.length() + method.length() + 3).
				append(url).append("?").append(CConf.ACTN).append("=").append(method).toString());
		try {
			
			if(debug) {
				logger.log(httpPost.getURI().getQuery());
				logger.log(sync);
				logger.log(claz);
				logger.log(json);
			}
			
			httpPost.setHeader(CConf.CLAZ, claz);
			httpPost.setHeader(CConf.SYNC, sync);
			httpPost.setEntity(new StringEntity(json, CConf.UTF8));
			response = httpClient.execute(httpPost);
		
			HttpEntity httpEntity = response.getEntity();
			content = EntityUtils.toString(httpEntity, CConf.UTF8);
		} catch(Exception exc) {
			throw new RuntimeException(exc.getCause());
		} finally {
			if(response != null) {
				try {
					response.close();
				} catch (IOException exc) {
					throw new RuntimeException(exc.getCause());
				}
			}
		}
			
		Header[] errs = response.getHeaders("errs");
		Header[] type = response.getHeaders("claz");
		
		if(debug) {
			logger.log(errs.toString());
			logger.log(type.toString());

			logger.log(content);
		}
		
		if(type.length < 1) {
			throw new RuntimeException("dscm-claz-err");
		}

		Object object = null;
		try {
			Class<?> ctarg = CParameter.parse(type[0].getValue());
			object = JSON.parseObject(content, ctarg);
		}catch(Exception exc) {
			throw new RuntimeException(exc.getCause() == null ? exc : exc.getCause());
		}
		
		if(errs.length > 0) {
			if(object instanceof Throwable) {
				throw (Throwable)object;
			}else{
				throw new RuntimeException(object.toString());
			}
		}

		result.object(object);

		return result;
	}

	public Object execute(Object proxy, Method method, Object[] args, String url, String sync) throws Throwable {
		String clazz = null, cjson = null;
	    CloseableHttpClient httpClient = getHttpClient();
		try {
			clazz = JSON.toJSONString(method.getParameterTypes(),CConf.FEATURE);
			cjson = JSON.toJSONString(args,CConf.FEATURE);
		}catch(Throwable exc) {
			throw exc;
		}
		
		return post(httpClient, url, method.getName(), 
				clazz, cjson, sync).object();
	}
}
	
	

