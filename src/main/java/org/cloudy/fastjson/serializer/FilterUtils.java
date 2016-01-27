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

import java.lang.reflect.Type;
import java.util.List;

import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.deserializer.ExtraProcessor;
import org.cloudy.fastjson.parser.deserializer.ExtraTypeProvider;

public class FilterUtils {
    public static Type getExtratype(DefaultJSONParser parser, Object object, String key) {
        List<ExtraTypeProvider> extraTypeProviders = parser.getExtraTypeProvidersDirect();
        if (extraTypeProviders == null) {
            return null;
        }
        
        Type type = null;
        for (ExtraTypeProvider extraProvider : extraTypeProviders) {
            type = extraProvider.getExtraType(object, key);
        }
        return type;
    }
    
    public static void processExtra(DefaultJSONParser parser, Object object, String key, Object value) {
        List<ExtraProcessor> extraProcessors = parser.getExtraProcessorsDirect();
        if (extraProcessors == null) {
            return;
        }
        for (ExtraProcessor process : extraProcessors) {
            process.processExtra(object, key, value);
        }
    }

    public static char writeBefore(JSONSerializer serializer, Object object, char seperator) {
        List<BeforeFilter> beforeFilters = serializer.getBeforeFiltersDirect();
        if (beforeFilters != null) {
            for (BeforeFilter beforeFilter : beforeFilters) {
                seperator = beforeFilter.writeBefore(serializer, object, seperator);
            }
        }
        return seperator;
    }
    
    public static char writeAfter(JSONSerializer serializer, Object object, char seperator) {
        List<AfterFilter> afterFilters = serializer.getAfterFiltersDirect();
        if (afterFilters != null) {
            for (AfterFilter afterFilter : afterFilters) {
                seperator = afterFilter.writeAfter(serializer, object, seperator);
            }
        }
        return seperator;
    }

    public static Object processValue(JSONSerializer serializer, Object object, String key, Object propertyValue) {
        List<ValueFilter> valueFilters = serializer.getValueFiltersDirect();
        if (valueFilters != null) {
            for (ValueFilter valueFilter : valueFilters) {
                propertyValue = valueFilter.process(object, key, propertyValue);
            }
        }

        return propertyValue;
    }

    public static String processKey(JSONSerializer serializer, Object object, String key, Object propertyValue) {
        List<NameFilter> nameFilters = serializer.getNameFiltersDirect();
        if (nameFilters != null) {
            for (NameFilter nameFilter : nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }

        return key;
    }

    public static String processKey(JSONSerializer serializer, Object object, String key, byte intValue) {
        List<NameFilter> nameFilters = serializer.getNameFiltersDirect();
        if (nameFilters != null) {
            Object propertyValue = Byte.valueOf(intValue);

            for (NameFilter nameFilter : nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }

        return key;
    }

    public static String processKey(JSONSerializer serializer, Object object, String key, short intValue) {
        List<NameFilter> nameFilters = serializer.getNameFiltersDirect();
        if (nameFilters != null) {
            Object propertyValue = Short.valueOf(intValue);

            for (NameFilter nameFilter : nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }

        return key;
    }

    public static String processKey(JSONSerializer serializer, Object object, String key, int intValue) {
        List<NameFilter> nameFilters = serializer.getNameFiltersDirect();
        if (nameFilters != null) {
            Object propertyValue = Integer.valueOf(intValue);

            for (NameFilter nameFilter : nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }

        return key;
    }

    public static String processKey(JSONSerializer serializer, Object object, String key, long intValue) {
        List<NameFilter> nameFilters = serializer.getNameFiltersDirect();
        if (nameFilters != null) {
            Object propertyValue = Long.valueOf(intValue);

            for (NameFilter nameFilter : nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }

        return key;
    }

    public static String processKey(JSONSerializer serializer, Object object, String key, float intValue) {
        List<NameFilter> nameFilters = serializer.getNameFiltersDirect();
        if (nameFilters != null) {
            Object propertyValue = Float.valueOf(intValue);

            for (NameFilter nameFilter : nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }

        return key;
    }

    public static String processKey(JSONSerializer serializer, Object object, String key, double intValue) {
        List<NameFilter> nameFilters = serializer.getNameFiltersDirect();
        if (nameFilters != null) {
            Object propertyValue = Double.valueOf(intValue);

            for (NameFilter nameFilter : nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }

        return key;
    }

    public static String processKey(JSONSerializer serializer, Object object, String key, boolean intValue) {
        List<NameFilter> nameFilters = serializer.getNameFiltersDirect();
        if (nameFilters != null) {
            Object propertyValue = Boolean.valueOf(intValue);

            for (NameFilter nameFilter : nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }

        return key;
    }

    public static String processKey(JSONSerializer serializer, Object object, String key, char intValue) {
        List<NameFilter> nameFilters = serializer.getNameFiltersDirect();
        if (nameFilters != null) {
            Object propertyValue = Character.valueOf(intValue);

            for (NameFilter nameFilter : nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }

        return key;
    }

    public static boolean applyName(JSONSerializer serializer, Object object, String key) {
        List<PropertyPreFilter> filters = serializer.getPropertyPreFiltersDirect();

        if (filters == null) {
            return true;
        }

        for (PropertyPreFilter filter : filters) {
            if (!filter.apply(serializer, object, key)) {
                return false;
            }
        }

        return true;
    }

    public static boolean apply(JSONSerializer serializer, Object object, String key, Object propertyValue) {
        List<PropertyFilter> propertyFilters = serializer.getPropertyFiltersDirect();

        if (propertyFilters == null) {
            return true;
        }

        for (PropertyFilter propertyFilter : propertyFilters) {
            if (!propertyFilter.apply(object, key, propertyValue)) {
                return false;
            }
        }

        return true;
    }

    public static boolean apply(JSONSerializer serializer, Object object, String key, byte value) {
        List<PropertyFilter> propertyFilters = serializer.getPropertyFiltersDirect();

        if (propertyFilters != null) {
            boolean apply = true;

            Object propertyValue = Byte.valueOf(value);
            for (PropertyFilter propertyFilter : propertyFilters) {
                if (!propertyFilter.apply(object, key, propertyValue)) {
                    return false;
                }
            }

            return apply;
        }

        return true;
    }

    public static boolean apply(JSONSerializer serializer, Object object, String key, short value) {
        List<PropertyFilter> propertyFilters = serializer.getPropertyFiltersDirect();

        if (propertyFilters != null) {
            boolean apply = true;

            Object propertyValue = Short.valueOf(value);
            for (PropertyFilter propertyFilter : propertyFilters) {
                if (!propertyFilter.apply(object, key, propertyValue)) {
                    return false;
                }
            }

            return apply;
        }

        return true;
    }

    public static boolean apply(JSONSerializer serializer, Object object, String key, int value) {
        List<PropertyFilter> propertyFilters = serializer.getPropertyFiltersDirect();

        if (propertyFilters != null) {
            boolean apply = true;

            Object propertyValue = Integer.valueOf(value);
            for (PropertyFilter propertyFilter : propertyFilters) {
                if (!propertyFilter.apply(object, key, propertyValue)) {
                    return false;
                }
            }

            return apply;
        }

        return true;
    }

    public static boolean apply(JSONSerializer serializer, Object object, String key, char value) {
        List<PropertyFilter> propertyFilters = serializer.getPropertyFiltersDirect();

        if (propertyFilters != null) {
            boolean apply = true;

            Object propertyValue = Character.valueOf(value);
            for (PropertyFilter propertyFilter : propertyFilters) {
                if (!propertyFilter.apply(object, key, propertyValue)) {
                    return false;
                }
            }

            return apply;
        }

        return true;
    }

    public static boolean apply(JSONSerializer serializer, Object object, String key, long value) {
        List<PropertyFilter> propertyFilters = serializer.getPropertyFiltersDirect();

        if (propertyFilters != null) {
            boolean apply = true;

            Object propertyValue = Long.valueOf(value);
            for (PropertyFilter propertyFilter : propertyFilters) {
                if (!propertyFilter.apply(object, key, propertyValue)) {
                    return false;
                }
            }

            return apply;
        }

        return true;
    }

    public static boolean apply(JSONSerializer serializer, Object object, String key, float value) {
        List<PropertyFilter> propertyFilters = serializer.getPropertyFiltersDirect();

        if (propertyFilters != null) {
            boolean apply = true;

            Object propertyValue = Float.valueOf(value);
            for (PropertyFilter propertyFilter : propertyFilters) {
                if (!propertyFilter.apply(object, key, propertyValue)) {
                    return false;
                }
            }

            return apply;
        }

        return true;
    }

    public static boolean apply(JSONSerializer serializer, Object object, String key, double value) {
        List<PropertyFilter> propertyFilters = serializer.getPropertyFiltersDirect();

        if (propertyFilters != null) {
            boolean apply = true;

            Object propertyValue = Double.valueOf(value);
            for (PropertyFilter propertyFilter : propertyFilters) {
                if (!propertyFilter.apply(object, key, propertyValue)) {
                    return false;
                }
            }

            return apply;
        }

        return true;
    }
    
    public static boolean applyLabel(JSONSerializer serializer, String label) {
        List<LabelFilter> viewFilters = serializer.getLabelFiltersDirect();

        if (viewFilters != null) {
            boolean apply = true;

            for (LabelFilter propertyFilter : viewFilters) {
                if (!propertyFilter.apply(label)) {
                    return false;
                }
            }

            return apply;
        }

        return true;
    }
}
