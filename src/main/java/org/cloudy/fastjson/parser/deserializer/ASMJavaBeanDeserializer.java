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
import java.util.Map;

import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.JSONLexer;
import org.cloudy.fastjson.parser.ParserConfig;
import org.cloudy.fastjson.util.FieldInfo;

public abstract class ASMJavaBeanDeserializer implements ObjectDeserializer {

    protected InnerJavaBeanDeserializer serializer;

    public ASMJavaBeanDeserializer(ParserConfig mapping, Class<?> clazz){
        serializer = new InnerJavaBeanDeserializer(mapping, clazz);

        serializer.getFieldDeserializerMap();
    }

    public abstract Object createInstance(DefaultJSONParser parser, Type type);

    public InnerJavaBeanDeserializer getInnterSerializer() {
        return serializer;
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return (T) serializer.deserialze(parser, type, fieldName);
    }

    public int getFastMatchToken() {
        return serializer.getFastMatchToken();
    }

    public Object createInstance(DefaultJSONParser parser) {
        return serializer.createInstance(parser, serializer.getClazz());
    }

    public FieldDeserializer createFieldDeserializer(ParserConfig mapping, Class<?> clazz, FieldInfo fieldInfo) {
        return mapping.createFieldDeserializer(mapping, clazz, fieldInfo);
    }

    public FieldDeserializer getFieldDeserializer(String name) {
        return serializer.getFieldDeserializerMap().get(name);
    }

    public Type getFieldType(String name) {
        return serializer.getFieldDeserializerMap().get(name).getFieldType();
    }

    public boolean parseField(DefaultJSONParser parser, String key, Object object, Type objectType,
                              Map<String, Object> fieldValues) {
        JSONLexer lexer = parser.getLexer(); // xxx

        Map<String, FieldDeserializer> feildDeserializerMap = serializer.getFieldDeserializerMap();
        FieldDeserializer fieldDeserializer = feildDeserializerMap.get(key);

        if (fieldDeserializer == null) {
            for (Map.Entry<String, FieldDeserializer> entry : feildDeserializerMap.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    fieldDeserializer = entry.getValue();
                    break;
                }
            }
        }

        if (fieldDeserializer == null) {
            this.serializer.parseExtra(parser, object, key);
            return false;
        }

        lexer.nextTokenWithColon(fieldDeserializer.getFastMatchToken());
        fieldDeserializer.parseField(parser, object, objectType, fieldValues);
        return true;
    }

    public final class InnerJavaBeanDeserializer extends JavaBeanDeserializer {

        private InnerJavaBeanDeserializer(ParserConfig mapping, Class<?> clazz){
            super(mapping, clazz);
        }

        public boolean parseField(DefaultJSONParser parser, String key, Object object, Type objectType,
                                  Map<String, Object> fieldValues) {
            return ASMJavaBeanDeserializer.this.parseField(parser, key, object, objectType, fieldValues);
        }

        public FieldDeserializer createFieldDeserializer(ParserConfig mapping, Class<?> clazz, FieldInfo fieldInfo) {
            return ASMJavaBeanDeserializer.this.createFieldDeserializer(mapping, clazz, fieldInfo);
        }
    }
    
    public boolean isSupportArrayToBean(JSONLexer lexer) {
        return serializer.isSupportArrayToBean(lexer);
    }

    public Object parseRest(DefaultJSONParser parser, Type type, Object fieldName, Object instance) {
//        serializer.parseField(parser, key, object, objectType, fieldValues)
        Object value = serializer.deserialze(parser, type, fieldName, instance);
        
        return value;
    }
}
