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

import java.lang.reflect.Type;

import org.cloudy.fastjson.JSONException;
import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.JSONLexer;
import org.cloudy.fastjson.parser.JSONScanner;
import org.cloudy.fastjson.parser.JSONToken;

public class TimeDeserializer implements ObjectDeserializer {

    public final static TimeDeserializer instance = new TimeDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        JSONLexer lexer = parser.getLexer();
        
        if (lexer.token() == JSONToken.COMMA) {
            lexer.nextToken(JSONToken.LITERAL_STRING);
            
            if (lexer.token() != JSONToken.LITERAL_STRING) {
                throw new JSONException("syntax error");
            }
            
            lexer.nextTokenWithColon(JSONToken.LITERAL_INT);
            
            if (lexer.token() != JSONToken.LITERAL_INT) {
                throw new JSONException("syntax error");
            }
            
            long time = lexer.longValue();
            lexer.nextToken(JSONToken.RBRACE);
            if (lexer.token() != JSONToken.RBRACE) {
                throw new JSONException("syntax error");
            }
            lexer.nextToken(JSONToken.COMMA);
            
            return (T) new java.sql.Time(time);
        }
        
        Object val = parser.parse();

        if (val == null) {
            return null;
        }

        if (val instanceof java.sql.Time) {
            return (T) val;
        } else if (val instanceof Number) {
            return (T) new java.sql.Time(((Number) val).longValue());
        } else if (val instanceof String) {
            String strVal = (String) val;
            if (strVal.length() == 0) {
                return null;
            }
            
            long longVal;
            JSONScanner dateLexer = new JSONScanner(strVal);
            if (dateLexer.scanISO8601DateIfMatch()) {
                longVal = dateLexer.getCalendar().getTimeInMillis();
            } else {
                longVal = Long.parseLong(strVal);
            }
            dateLexer.close();
            return (T) new java.sql.Time(longVal);
        }
        
        throw new JSONException("parse error");
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }
}
