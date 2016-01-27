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
package org.cloudy.fastjson;

class JSONStreamContext {

    final static int               StartObject   = 1001;
    final static int               PropertyKey   = 1002;
    final static int               PropertyValue = 1003;
    final static int               StartArray    = 1004;
    final static int               ArrayValue    = 1005;

    private final JSONStreamContext parent;

    private int                     state;

    public JSONStreamContext(JSONStreamContext parent, int state){
        this.parent = parent;
        this.state = state;
    }

    public JSONStreamContext getParent() {
        return parent;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

}
