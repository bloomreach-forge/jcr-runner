/*
 *  Copyright 2009-2013 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.jcrrunner;

import java.util.HashMap;
import java.util.Map;

public class RunnerPluginConfig {

    private final String id;

    private final RunnerPluginType type;

    private Map<String, String> properties = new HashMap<String, String>();

    public RunnerPluginConfig(final String id, final RunnerPluginType type) {
        super();
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public RunnerPluginType getType() {
        return type;
    }

    public void addEntry(String key, String value) {
        properties.put(key, value);
    }

    public String getValue(String key) {
        return properties.get(key);
    }

}
