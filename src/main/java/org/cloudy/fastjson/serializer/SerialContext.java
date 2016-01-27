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

public class SerialContext {

    private final SerialContext parent;

    private final Object        object;

    private final Object        fieldName;

    private int                 features;

    private int                 fieldFeatures;

    public SerialContext(SerialContext parent, Object object, Object fieldName, int features, int fieldFeatures){
        this.parent = parent;
        this.object = object;
        this.fieldName = fieldName;
        this.features = features;
        this.fieldFeatures = fieldFeatures;
    }

    public SerialContext getParent() {
        return parent;
    }

    public Object getObject() {
        return object;
    }

    public Object getFieldName() {
        return fieldName;
    }

    public String getPath() {
        if (parent == null) {
            return "$";
        } else {
            if (fieldName instanceof Integer) {
                return parent.getPath() + "[" + fieldName + "]";
            } else {
                return parent.getPath() + "." + fieldName;
            }

        }
    }

    public String toString() {
        return getPath();
    }

    public int getFeatures() {
        return features;
    }

    public boolean isEnabled(SerializerFeature feature) {
        return SerializerFeature.isEnabled(features, fieldFeatures, feature);
    }
}
