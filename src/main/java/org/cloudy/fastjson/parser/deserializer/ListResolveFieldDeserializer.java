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

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.cloudy.fastjson.JSONArray;
import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.util.TypeUtils;

@SuppressWarnings("rawtypes")
public final class ListResolveFieldDeserializer extends FieldDeserializer {

    private final int               index;
    private final List              list;
    private final DefaultJSONParser parser;

    public ListResolveFieldDeserializer(DefaultJSONParser parser, List list, int index){
        super(null, null);
        this.parser = parser;
        this.index = index;
        this.list = list;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object object, Object value) {
        list.set(index, value);

        if (list instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) list;
            Object array = jsonArray.getRelatedArray();

            if (array != null) {
                int arrayLength = Array.getLength(array);

                if (arrayLength > index) {
                    Object item;
                    if (jsonArray.getComponentType() != null) {
                        item = TypeUtils.cast(value, jsonArray.getComponentType(), parser.getConfig());
                    } else {
                        item = value;
                    }
                    Array.set(array, index, item);
                }
            }
        }
    }

    public void parseField(DefaultJSONParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {

    }

}
