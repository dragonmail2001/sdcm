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
package org.cloudy.fastjson.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.JSONLexer;
import org.cloudy.fastjson.parser.JSONToken;
import org.cloudy.fastjson.parser.deserializer.ObjectDeserializer;

public class ASMUtils {

    public static final String JAVA_VM_NAME = System.getProperty("java.vm.name");
	
    public static boolean isAndroid(String vmName) {
        if (vmName == null) { // default is false
            return false;
        }
        
        String lowerVMName = vmName.toLowerCase();
        
        return lowerVMName.contains("dalvik") //
               || lowerVMName.contains("lemur") // aliyun-vm name
        ;
    }

    public static boolean isAndroid() {
        return isAndroid(JAVA_VM_NAME);
    }

    public static String getDesc(Method method) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        java.lang.Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; ++i) {
            buf.append(getDesc(types[i]));
        }
        buf.append(")");
        buf.append(getDesc(method.getReturnType()));
        return buf.toString();
    }

    public static String getDesc(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            return getPrimitiveLetter(returnType);
        } else if (returnType.isArray()) {
            return "[" + getDesc(returnType.getComponentType());
        } else {
            return "L" + getType(returnType) + ";";
        }
    }

    public static String getType(Class<?> parameterType) {
        if (parameterType.isArray()) {
            return "[" + getDesc(parameterType.getComponentType());
        } else {
            if (!parameterType.isPrimitive()) {
                String clsName = parameterType.getName();
                return clsName.replaceAll("\\.", "/");
            } else {
                return getPrimitiveLetter(parameterType);
            }
        }
    }

    public static String getPrimitiveLetter(Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        } else if (Void.TYPE.equals(type)) {
            return "V";
        } else if (Boolean.TYPE.equals(type)) {
            return "Z";
        } else if (Character.TYPE.equals(type)) {
            return "C";
        } else if (Byte.TYPE.equals(type)) {
            return "B";
        } else if (Short.TYPE.equals(type)) {
            return "S";
        } else if (Float.TYPE.equals(type)) {
            return "F";
        } else if (Long.TYPE.equals(type)) {
            return "J";
        } else if (Double.TYPE.equals(type)) {
            return "D";
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    public static Type getMethodType(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName);

            return method.getGenericReturnType();
        } catch (Exception ex) {
            return null;
        }
    }

    public static Type getFieldType(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);

            return field.getGenericType();
        } catch (Exception ex) {
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void parseArray(Collection collection, //
                                  ObjectDeserializer deser, //
                                  DefaultJSONParser parser, //
                                  Type type, //
                                  Object fieldName) {

        final JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.NULL) {
            lexer.nextToken(JSONToken.COMMA);
        }

        parser.accept(JSONToken.LBRACKET, JSONToken.LBRACKET);

        int index = 0;
        for (;;) {
            Object item = deser.deserialze(parser, type, index);
            collection.add(item);
            index++;
            if (lexer.token() == JSONToken.COMMA) {
                lexer.nextToken(JSONToken.LBRACKET);
            } else {
                break;
            }
        }
        parser.accept(JSONToken.RBRACKET, JSONToken.COMMA);
    }
    
    public static boolean checkName(String name) {
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (c < '\001' || c > '\177') {
                return false;
            }
        }
        
        return true;
    }
}
