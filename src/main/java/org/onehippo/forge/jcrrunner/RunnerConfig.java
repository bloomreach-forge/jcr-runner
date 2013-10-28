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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

public final class RunnerConfig {

    private static final String REPOSITORY_URL = "repository.url";
    private static final String REPOSITORY_USER = "repository.user";
    private static final String REPOSITORY_PASS = "repository.pass";

    private static final String PLUGINS_JAVA = "plugins.java";
    private static final String PLUGINS_BEANSHELL = "plugins.beanshell";

    private SortedMap<String, RunnerPluginConfig> pluginConfigMap = new TreeMap<String, RunnerPluginConfig>();

    private String repositoryUrl;
    private String repositoryUser;
    private String repositoryPass;

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getRepositoryUser() {
        return repositoryUser;
    }

    public void setRepositoryUser(String repositoryUser) {
        this.repositoryUser = repositoryUser;
    }

    public String getRepositoryPass() {
        return repositoryPass;
    }

    public void setRepositoryPass(String repositoryPass) {
        this.repositoryPass = repositoryPass;
    }

    public List<RunnerPluginConfig> getPluginConfigs() {
        return new ArrayList<RunnerPluginConfig>(pluginConfigMap.values());
    }

    public RunnerConfig(InputStream inputStream) throws IOException {
        Properties props = new Properties();
        props.load(inputStream);
        initializeRunnerConfig(props);
    }

    public RunnerConfig(Properties props) {
        initializeRunnerConfig(props);
    }

    private void initializeRunnerConfig(Properties props) {
        readRunnerConfig(props);
        validateRunnerConfig();
        readPluginInformation(props);
    }

    private void readRunnerConfig(Properties props) {
        setRepositoryUrl(props.getProperty(REPOSITORY_URL));
        setRepositoryUser(props.getProperty(REPOSITORY_USER));
        setRepositoryPass(props.getProperty(REPOSITORY_PASS));
    }

    private void validateRunnerConfig() {
        if (isEmpty(getRepositoryUrl())) {
            throw new IllegalArgumentException(REPOSITORY_URL + " is missing.");
        }
        if (isEmpty(getRepositoryUser())) {
            throw new IllegalArgumentException(REPOSITORY_USER + " is missing.");
        }
        if (isEmpty(getRepositoryPass())) {
            throw new IllegalArgumentException(REPOSITORY_PASS + " is missing.");
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private void readPluginInformation(Properties props) {
        Set<Entry<Object, Object>> entries = props.entrySet();
        for (Entry<Object, Object> entry : entries) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith(PLUGINS_JAVA)) {
                int pos = key.indexOf('.', PLUGINS_JAVA.length() + 1);
                if (pos > 0) {
                    String id = key.substring(PLUGINS_JAVA.length() + 1, pos);
                    String pluginKey = key.substring(pos + 1);
                    if (!pluginConfigMap.containsKey(id)) {
                        pluginConfigMap.put(id, new RunnerPluginConfig(id, RunnerPluginType.JAVA));
                    }
                    pluginConfigMap.get(id).addEntry(pluginKey, value);
                }
            }
            if (key.startsWith(PLUGINS_BEANSHELL)) {
                int pos = key.indexOf('.', PLUGINS_BEANSHELL.length() + 1);
                if (pos > 0) {
                    String id = key.substring(PLUGINS_BEANSHELL.length() + 1, pos);
                    String pluginKey = key.substring(pos + 1);
                    if (!pluginConfigMap.containsKey(id)) {
                        pluginConfigMap.put(id, new RunnerPluginConfig(id, RunnerPluginType.BEANSHELL));
                    }
                    pluginConfigMap.get(id).addEntry(pluginKey, value);
                }
            }
        }
    }
}