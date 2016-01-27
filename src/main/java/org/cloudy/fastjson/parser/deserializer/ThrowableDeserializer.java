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
package org.cloudy.fastjson.parser.deserializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.cloudy.fastjson.JSON;
import org.cloudy.fastjson.JSONException;
import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.Feature;
import org.cloudy.fastjson.parser.JSONLexer;
import org.cloudy.fastjson.parser.JSONToken;
import org.cloudy.fastjson.parser.ParserConfig;
import org.cloudy.fastjson.util.TypeUtils;

public class ThrowableDeserializer extends JavaBeanDeserializer {

    public ThrowableDeserializer(ParserConfig mapping, Class<?> clazz){
        super(mapping, clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONLexer lexer = parser.getLexer();
        
        if (lexer.token() == JSONToken.NULL) {
            lexer.nextToken();
            return null;
        }

        if (parser.getResolveStatus() == DefaultJSONParser.TypeNameRedirect) {
            parser.setResolveStatus(DefaultJSONParser.NONE);
        } else {
            if (lexer.token() != JSONToken.LBRACE) {
                throw new JSONException("syntax error");
            }
        }

        Throwable cause = null;
        Class<?> exClass = null;
        
        if (type != null && type instanceof Class) {
        	Class<?> clazz = (Class<?>) type;
        	if (Throwable.class.isAssignableFrom(clazz)) {
        		exClass = clazz;
        	}
        }
        
        String message = null;
        StackTraceElement[] stackTrace = null;
        Map<String, Object> otherValues = new HashMap<String, Object>();

        for (;;) {
            // lexer.scanSymbol
            String key = lexer.scanSymbol(parser.getSymbolTable());

            if (key == null) {
                if (lexer.token() == JSONToken.RBRACE) {
                    lexer.nextToken(JSONToken.COMMA);
                    break;
                }
                if (lexer.token() == JSONToken.COMMA) {
                    if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                        continue;
                    }
                }
            }

            lexer.nextTokenWithColon(JSONToken.LITERAL_STRING);

            if (JSON.DEFAULT_TYPE_KEY.equals(key)) {
                if (lexer.token() == JSONToken.LITERAL_STRING) {
                    String exClassName = lexer.stringVal();
                    exClass = TypeUtils.loadClass(exClassName);
                } else {
                    throw new JSONException("syntax error");
                }
                lexer.nextToken(JSONToken.COMMA);
            } else if ("message".equals(key)) {
                if (lexer.token() == JSONToken.NULL) {
                    message = null;
                } else if (lexer.token() == JSONToken.LITERAL_STRING) {
                    message = lexer.stringVal();
                } else {
                    throw new JSONException("syntax error");
                }
                lexer.nextToken();
            } else if ("cause".equals(key)) {
                cause = deserialze(parser, null, "cause");
            } else if ("stackTrace".equals(key)) {
                stackTrace = parser.parseObject(StackTraceElement[].class);
            } else {
                // TODO
                otherValues.put(key, parser.parse());
            }

            if (lexer.token() == JSONToken.RBRACE) {
                lexer.nextToken(JSONToken.COMMA);
                break;
            }
        }

        Throwable ex = null;
        if (exClass == null) {
            ex = new Exception(message, cause);
        } else {
            try {
                ex = createException(message, cause, exClass);
                if (ex == null) {
                    ex = new Exception(message, cause);
                }
            } catch (Exception e) {
                throw new JSONException("create instance error", e);
            }
        }

        if (stackTrace != null) {
            ex.setStackTrace(stackTrace);
        }

        return (T) ex;
    }

    private Throwable createException(String message, Throwable cause, Class<?> exClass) throws Exception {
        Constructor<?> defaultConstructor = null;
        Constructor<?> messageConstructor = null;
        Constructor<?> causeConstructor = null;
        for (Constructor<?> constructor : exClass.getConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                defaultConstructor = constructor;
                continue;
            }

            if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0] == String.class) {
                messageConstructor = constructor;
                continue;
            }

            if (constructor.getParameterTypes().length == 2 && constructor.getParameterTypes()[0] == String.class
                && constructor.getParameterTypes()[1] == Throwable.class) {
                causeConstructor = constructor;
                continue;
            }
        }

        if (causeConstructor != null) {
            return (Throwable) causeConstructor.newInstance(message, cause);
        }

        if (messageConstructor != null) {
            return (Throwable) messageConstructor.newInstance(message);
        }

        if (defaultConstructor != null) {
            return (Throwable) defaultConstructor.newInstance();
        }

        return null;
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
