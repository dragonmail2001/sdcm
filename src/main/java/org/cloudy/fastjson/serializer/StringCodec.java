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
package org.cloudy.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.JSONLexer;
import org.cloudy.fastjson.parser.JSONToken;
import org.cloudy.fastjson.parser.deserializer.ObjectDeserializer;

public class StringCodec implements ObjectSerializer, ObjectDeserializer {

    public static StringCodec instance = new StringCodec();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
                                                                                                               throws IOException {
        write(serializer, (String) object);
    }

    public void write(JSONSerializer serializer, String value) {
        SerializeWriter out = serializer.getWriter();

        if (value == null) {
            if (out.isEnabled(SerializerFeature.WriteNullStringAsEmpty)) {
                out.writeString("");
            } else {
                out.writeNull();
            }
            return;
        }

        out.writeString(value);
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        if (clazz == StringBuffer.class) {
            final JSONLexer lexer = parser.getLexer();
            if (lexer.token() == JSONToken.LITERAL_STRING) {
                String val = lexer.stringVal();
                lexer.nextToken(JSONToken.COMMA);

                return (T) new StringBuffer(val);
            }

            Object value = parser.parse();

            if (value == null) {
                return null;
            }

            return (T) new StringBuffer(value.toString());
        }

        if (clazz == StringBuilder.class) {
            final JSONLexer lexer = parser.getLexer();
            if (lexer.token() == JSONToken.LITERAL_STRING) {
                String val = lexer.stringVal();
                lexer.nextToken(JSONToken.COMMA);

                return (T) new StringBuilder(val);
            }

            Object value = parser.parse();

            if (value == null) {
                return null;
            }

            return (T) new StringBuilder(value.toString());
        }

        return (T) deserialze(parser);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialze(DefaultJSONParser parser) {
        final JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.LITERAL_STRING) {
            String val = lexer.stringVal();
            lexer.nextToken(JSONToken.COMMA);
            return (T) val;
        }

        if (lexer.token() == JSONToken.LITERAL_INT) {
            String val = lexer.numberString();
            lexer.nextToken(JSONToken.COMMA);
            return (T) val;
        }

        Object value = parser.parse();

        if (value == null) {
            return null;
        }

        return (T) value.toString();
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}
