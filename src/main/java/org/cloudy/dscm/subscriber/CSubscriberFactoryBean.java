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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.cloudy.dscm.common.CConf;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class CSubscriberFactoryBean implements FactoryBean<Object>, InitializingBean {
	
	private String interfaceName;
	
	private String interfaceUrl;

	private Object proxyObj;
	
	private String sync = CConf.SYNC;

	public void setSync(String sync) {
		this.sync = sync;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	
	public void setInterfaceUrl(String interfaceUrl) {
		this.interfaceUrl = interfaceUrl;
	}	

	public Object getObject() throws Exception {
		return proxyObj;
	}

	public Class<?> getObjectType() {
		return proxyObj == null ? Object.class : proxyObj.getClass();
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		this.proxyObj = Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class[] { Class.forName(interfaceName) }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if(method.getName().equals("toString") || method.getName().equals("hashCode") ||
								method.getName().equals("getClass") || method.getName().equals("notify") ||
								method.getName().equals("notifyAll") || method.getName().equals("wait")) {
							throw new java.lang.NoSuchMethodError();
						}
						
						return CConnectionExecutorImpl.connectionExecutor().execute(proxy, method, args, interfaceUrl, sync);
					}
				});		
	}	
}
