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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.cloudy.fastjson.JSONException;
import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.util.FieldInfo;

public abstract class FieldDeserializer {

    protected final FieldInfo fieldInfo;

    protected final Class<?>  clazz;

    public FieldDeserializer(Class<?> clazz, FieldInfo fieldInfo){
        this.clazz = clazz;
        this.fieldInfo = fieldInfo;
    }
    
    public FieldInfo getFieldInfo() {
        return fieldInfo;
    }

    public Method getMethod() {
        return fieldInfo.getMethod();
    }

    public Field getField() {
        return fieldInfo.getField();
    }

    public Class<?> getFieldClass() {
        return fieldInfo.getFieldClass();
    }

    public Type getFieldType() {
        return fieldInfo.getFieldType();
    }

    public abstract void parseField(DefaultJSONParser parser, Object object, Type objectType,
                                    Map<String, Object> fieldValues);

    public int getFastMatchToken() {
        return 0;
    }

    public void setValue(Object object, boolean value) {
        setValue(object, Boolean.valueOf(value));
    }

    public void setValue(Object object, int value) {
        setValue(object, Integer.valueOf(value));
    }

    public void setValue(Object object, long value) {
        setValue(object, Long.valueOf(value));
    }

    public void setValue(Object object, String value) {
        setValue(object, (Object) value);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setValue(Object object, Object value) {
        Method method = fieldInfo.getMethod();
        if (method != null) {
            try {
                if (fieldInfo.isGetOnly()) {
                    if (fieldInfo.getFieldClass() == AtomicInteger.class) {
                        AtomicInteger atomic = (AtomicInteger) method.invoke(object);
                        if (atomic != null) {
                            atomic.set(((AtomicInteger) value).get());
                        }
                    } else if (fieldInfo.getFieldClass() == AtomicLong.class) {
                        AtomicLong atomic = (AtomicLong) method.invoke(object);
                        if (atomic != null) {
                            atomic.set(((AtomicLong) value).get());
                        }
                    } else if (fieldInfo.getFieldClass() == AtomicBoolean.class) {
                        AtomicBoolean atomic = (AtomicBoolean) method.invoke(object);
                        if (atomic != null) {
                            atomic.set(((AtomicBoolean) value).get());
                        }
                    } else if (Map.class.isAssignableFrom(method.getReturnType())) {
                        Map map = (Map) method.invoke(object);
                        if (map != null) {
                            map.putAll((Map) value);
                        }
                    } else {
                        Collection collection = (Collection) method.invoke(object);
                        if (collection != null) {
                            collection.addAll((Collection) value);
                        }
                    }
                } else {
                    if (value == null && fieldInfo.getFieldClass().isPrimitive()) {
                        return;
                    }
                    method.invoke(object, value);
                }
            } catch (Exception e) {
                throw new JSONException("set property error, " + fieldInfo.getName(), e);
            }
            return;
        }

        final Field field = fieldInfo.getField();
        if (field != null) {
            try {
                field.set(object, value);
            } catch (Exception e) {
                throw new JSONException("set property error, " + fieldInfo.getName(), e);
            }
        }
    }
}
