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
package org.cloudy.dscm.common;

import java.util.List;

public class CParameter {
	private String clazz;
	private Object object;

	public Object object() {
		return object;
	}

	public void object(Object object) {
		this.object = object;
	}
	
	public String clazz(){
		return this.clazz;
	}
	
	public void clazz(String clazz) {
		this.clazz = clazz;
	}
	
	public static Class<?> parse(String types) throws ClassNotFoundException {
		Class<?> the = CConf.clazz(types);
		if(the == null) {
			the = Class.forName(types);
		}
		return the;
	}
	
	public static Class<?>[] parse(List<String> types) throws ClassNotFoundException {
		Class<?>[] clazz = new Class<?>[types.size()];
		for(int i=0; i<types.size(); i++) {
			clazz[i] = CConf.clazz(types.get(i));
			if(clazz[i] == null) {
				clazz[i] = Class.forName(types.get(i));
			}
		}
		return clazz;
	}
}
