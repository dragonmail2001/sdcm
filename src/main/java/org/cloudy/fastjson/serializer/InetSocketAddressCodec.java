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
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.cloudy.fastjson.JSONException;
import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.JSONLexer;
import org.cloudy.fastjson.parser.JSONToken;
import org.cloudy.fastjson.parser.deserializer.ObjectDeserializer;

public class InetSocketAddressCodec implements ObjectSerializer, ObjectDeserializer {

    public static InetSocketAddressCodec instance = new InetSocketAddressCodec();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            serializer.writeNull();
            return;
        }

        SerializeWriter out = serializer.getWriter();
        InetSocketAddress address = (InetSocketAddress) object;

        InetAddress inetAddress = address.getAddress();

        out.write('{');
        if (inetAddress != null) {
            out.writeFieldName("address");
            serializer.write(inetAddress);
            out.write(',');
        }
        out.writeFieldName("port");
        out.writeInt(address.getPort());
        out.write('}');
    }
    
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        JSONLexer lexer = parser.getLexer();

        if (lexer.token() == JSONToken.NULL) {
            lexer.nextToken();
            return null;
        }

        parser.accept(JSONToken.LBRACE);

        InetAddress address = null;
        int port = 0;
        for (;;) {
            String key = lexer.stringVal();
            lexer.nextToken(JSONToken.COLON);
           

            if (key.equals("address")) {
                parser.accept(JSONToken.COLON);
                address = parser.parseObject(InetAddress.class);
            } else if (key.equals("port")) {
                parser.accept(JSONToken.COLON);
                if (lexer.token() != JSONToken.LITERAL_INT) {
                    throw new JSONException("port is not int");
                }
                port = lexer.intValue();
                lexer.nextToken();
            } else {
                parser.accept(JSONToken.COLON);
                parser.parse();
            }

            if (lexer.token() == JSONToken.COMMA) {
                lexer.nextToken();
                continue;
            }

            break;
        }

        parser.accept(JSONToken.RBRACE);

        return (T) new InetSocketAddress(address, port);
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
