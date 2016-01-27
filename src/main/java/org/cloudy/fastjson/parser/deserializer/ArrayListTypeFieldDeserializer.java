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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.cloudy.fastjson.JSONException;
import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.Feature;
import org.cloudy.fastjson.parser.JSONLexer;
import org.cloudy.fastjson.parser.JSONToken;
import org.cloudy.fastjson.parser.ParseContext;
import org.cloudy.fastjson.parser.ParserConfig;
import org.cloudy.fastjson.util.FieldInfo;

public class ArrayListTypeFieldDeserializer extends FieldDeserializer {

    private final Type         itemType;
    private int                itemFastMatchToken;
    private ObjectDeserializer deserializer;

    public ArrayListTypeFieldDeserializer(ParserConfig mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);

        Type fieldType = getFieldType();
        if (fieldType instanceof ParameterizedType) {
            this.itemType = ((ParameterizedType) getFieldType()).getActualTypeArguments()[0];
        } else {
            this.itemType = Object.class;
        }
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACKET;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parseField(DefaultJSONParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        if (parser.getLexer().token() == JSONToken.NULL) {
            setValue(object, null);
            return;
        }

        ArrayList list = new ArrayList();

        ParseContext context = parser.getContext();

        parser.setContext(context, object, fieldInfo.getName());
        parseArray(parser, objectType, list);
        parser.setContext(context);

        if (object == null) {
            fieldValues.put(fieldInfo.getName(), list);
        } else {
            setValue(object, list);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final void parseArray(DefaultJSONParser parser, Type objectType, Collection array) {
        Type itemType = this.itemType;
        ObjectDeserializer itemTypeDeser = this.deserializer;
        
        if (itemType instanceof TypeVariable //
            && objectType instanceof ParameterizedType) {
            TypeVariable typeVar = (TypeVariable) itemType;
            ParameterizedType paramType = (ParameterizedType) objectType;

            Class<?> objectClass = null;
            if (paramType.getRawType() instanceof Class) {
                objectClass = (Class<?>) paramType.getRawType();
            }

            int paramIndex = -1;
            if (objectClass != null) {
                for (int i = 0, size = objectClass.getTypeParameters().length; i < size; ++i) {
                    TypeVariable item = objectClass.getTypeParameters()[i];
                    if (item.getName().equals(typeVar.getName())) {
                        paramIndex = i;
                        break;
                    }
                }
            }

            if (paramIndex != -1) {
                itemType = paramType.getActualTypeArguments()[paramIndex];
                if (!itemType.equals(this.itemType)) {
                    itemTypeDeser = parser.getConfig().getDeserializer(itemType);
                }
            }
        }

        final JSONLexer lexer = parser.getLexer();

        if (lexer.token() != JSONToken.LBRACKET) {
            String errorMessage = "exepct '[', but " + JSONToken.name(lexer.token());
            if (objectType != null) {
                errorMessage += ", type : " + objectType;
            }
            throw new JSONException(errorMessage);
        }

        if (itemTypeDeser == null) {
            itemTypeDeser = deserializer = parser.getConfig().getDeserializer(itemType);
            itemFastMatchToken = deserializer.getFastMatchToken();
        }

        lexer.nextToken(itemFastMatchToken);

        for (int i = 0;; ++i) {
            if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                while (lexer.token() == JSONToken.COMMA) {
                    lexer.nextToken();
                    continue;
                }
            }

            if (lexer.token() == JSONToken.RBRACKET) {
                break;
            }

            Object val = itemTypeDeser.deserialze(parser, itemType, i);
            array.add(val);

            parser.checkListResolve(array);

            if (lexer.token() == JSONToken.COMMA) {
                lexer.nextToken(itemFastMatchToken);
                continue;
            }
        }

        lexer.nextToken(JSONToken.COMMA);
    }

}
