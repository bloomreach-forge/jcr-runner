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

import javax.jcr.Node;
import javax.jcr.Session;

/**
 * All RunnerPlugins must implement this interface directly or
 * extend the {@link org.onehippo.forge.jcrrunner.plugins.AbstractRunnerPlugin}.
 */
public interface RunnerPlugin {

    /**
     * Get the id of the plugin
     */
    String getId();

    /**
     * Get the id of the plugin
     */
    void setId(String id);

    /**
     * Get the name of the plugin
     */
    void setConfig(RunnerPluginConfig config);

    /**
     * Get a value from the embedded {@link RunnerPluginConfig}
     * @param key the name of the key
     * @return the String value
     */
    String getConfigValue(String key);

    /**
     * Get a value from the embedded {@link RunnerPluginConfig}
     * @param key the name of the key
     * @param defaultValue the default value
     * @return the String value or the default value if the key is not set
     */
    String getConfigValue(String key, String defaultValue);

    /**
     * Get an boolean value from the embedded {@link RunnerPluginConfig}
     * @param key the name of the key
     * @param defaultValue the default value
     * @return the boolean value or the default value if the key is not set
     */
    boolean getBooleanConfigValue(String key, boolean defaultValue);

    /**
     * Get an int value from the embedded {@link RunnerPluginConfig}
     * @param key the name of the key
     * @param defaultValue the default value
     * @return the int value or the default value if the key is not set
     */
    int getIntConfigValue(String key, int defaultValue);

    /**
     * Get a long value from the embedded {@link RunnerPluginConfig}
     * @param key the name of the key
     * @param defaultValue the default value
     * @return the String value or the default value if the key is not set
     */
    long getLongConfigValue(String key, long defaultValue);

    /**
     * Initialization hook
     * @param session The jcr session
     */
    void init(Session session);

    /**
     * Shutdown hook
     * @param session The jcr session
     */
    void destroy(Session session);

    /**
     * Called when visiting the node
     * @param node The current JCR node to visit
     */
    void visit(Node node);
}