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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.cloudy.fastjson.JSONException;
import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.JSONLexer;
import org.cloudy.fastjson.parser.JSONToken;

@SuppressWarnings("rawtypes")
public class EnumDeserializer implements ObjectDeserializer {

    private final Class<?>           enumClass;

    private final Map<Integer, Enum> ordinalMap = new HashMap<Integer, Enum>();
    private final Map<String, Enum>  nameMap    = new HashMap<String, Enum>();

    public EnumDeserializer(Class<?> enumClass){
        this.enumClass = enumClass;

        try {
            Method valueMethod = enumClass.getMethod("values");
            Object[] values = (Object[]) valueMethod.invoke(null);
            for (Object value : values) {
                Enum e = (Enum) value;
                ordinalMap.put(e.ordinal(), e);
                nameMap.put(e.name(), e);
            }
        } catch (Exception ex) {
            throw new JSONException("init enum values error, " + enumClass.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        try {
            Object value;
            final JSONLexer lexer = parser.getLexer();
            if (lexer.token() == JSONToken.LITERAL_INT) {
                value = lexer.intValue();
                lexer.nextToken(JSONToken.COMMA);

                T e = (T) ordinalMap.get(value);
                if (e == null) {
                    throw new JSONException("parse enum " + enumClass.getName() + " error, value : " + value);
                }
                return e;
            } else if (lexer.token() == JSONToken.LITERAL_STRING) {
                String strVal = lexer.stringVal();
                lexer.nextToken(JSONToken.COMMA);

                if (strVal.length() == 0) {
                    return (T) null;
                }

                value = nameMap.get(strVal);

                return (T) Enum.valueOf((Class<Enum>) enumClass, strVal);
            } else if (lexer.token() == JSONToken.NULL) {
                value = null;
                lexer.nextToken(JSONToken.COMMA);

                return null;
            } else {
                value = parser.parse();
            }

            throw new JSONException("parse enum " + enumClass.getName() + " error, value : " + value);
        } catch (JSONException e) {
            throw e;
        } catch (Throwable e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }
}
