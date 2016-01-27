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

import java.nio.charset.Charset;

import org.cloudy.fastjson.serializer.SerializeConfig;
import org.cloudy.fastjson.serializer.SerializerFeature;
import org.cloudy.fastjson.serializer.SimpleDateFormatSerializer;

public class CConf {
	public final static String DF = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String CTXNAME = "___ctx___";
	public static final String ACTN = "actn";
	public static final String CLAZ = "claz";
	public static final String SYNC = "sync";
	public static final String ERRS = "errs";
	public static final String UTF8 = "UTF-8";
	public static final Charset CUTF = Charset.forName("UTF-8");
	
	//public final static SerializerFeature[] FEATURE = { SerializerFeature.WriteClassName,SerializerFeature.WriteDateUseDateFormat};
	public final static SerializerFeature[] FEATURE = {SerializerFeature.WriteDateUseDateFormat, SerializerFeature.IgnoreNonFieldGetter};  
	
	static {  
		SerializeConfig.getGlobalInstance().put(java.util.Date.class, new SimpleDateFormatSerializer(CConf.DF)); 
		SerializeConfig.getGlobalInstance().put(java.sql.Date.class, new SimpleDateFormatSerializer(CConf.DF)); 	
	}
	
	public static Class<?> clazz(String name) {
		if(name.equals("int")) {
			return int.class;
		}
		
		if(name.equals("long")) {
			return long.class;
		}
		
		if(name.equals("float")) {
			return float.class;
		}
		
		if(name.equals("double")) {
			return double.class;
		}
		
		if(name.equals("byte")) {
			return byte.class;
		}
		
		if(name.equals("boolean")) {
			return boolean.class;
		}
		
		if(name.equals("char")) {
			return char.class;
		}
		
		if(name.equals("short")) {
			return short.class;
		}
		
		if(name.equals("void")) {
			return java.lang.Void.class;
		}
		return null;
	}
	
	public static int toInt(String str) {
		return Integer.parseInt(str);
	}
	
	public static long toLong(String str) {
		return Long.parseLong(str);
	}
	
	public static boolean toBool(String str) {
		return Boolean.valueOf(str);
	}
}
