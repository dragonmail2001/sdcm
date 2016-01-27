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
package org.cloudy.dscm.context;


public interface CContextLoaderListener {
	public Object classByName(String name);
}
//
//import javax.servlet.ServletContextEvent;
//
//import org.apache.log4j.Logger;
//import org.cloudy.dscm.publisher.CServer;
//import org.springframework.web.context.ContextLoaderListener;
//
//public class CContextLoaderListener extends ContextLoaderListener {
//	
//	private static Logger logger = Logger.getLogger("dscm");
//	
//	public void contextInitialized(ServletContextEvent event) {
//		try {
//			CServer.getInstance().startHttpserver();
//		} catch (Exception exc) {
//			throw new RuntimeException(exc.getCause());
//		}
//		
//		super.contextInitialized(event);
//		
//		logger.info("dscm-server start");
//	}
//	
//	
//	public void contextDestroyed(ServletContextEvent event) {
//		CServer.getInstance().stopHttpserver();
//		
//		logger.info("dscm-server destroy");
//		super.contextDestroyed(event);
//	}
//}
