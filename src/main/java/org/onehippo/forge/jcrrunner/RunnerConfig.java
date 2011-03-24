/*
 *  Copyright 2009 - 2011 Hippo.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public final class RunnerConfig {

    private static final String REPOSITORY_URL = "repository.url";
    private static final String REPOSITORY_USER = "repository.user";
    private static final String REPOSITORY_PASS = "repository.pass";
    private static final String REPOSITORY_PATH = "repository.path";
    private static final String REPOSITORY_QUERY = "repository.query";
    private static final String REPOSITORY_QUERY_LANGUAGE = "repository.query.language";
    private static final String REPOSITORY_QUERY_LANGUAGE_DEFAULT = "xpath";

    private static final String PLUGINS_JAVA = "plugins.java";
    private static final String PLUGINS_BEANSHELL = "plugins.beanshell";

    private SortedMap<String, RunnerPluginConfig> pluginConfigMap = new TreeMap<String, RunnerPluginConfig>();

    private String repositoryUrl;
    private String repositoryUser;
    private String repositoryPass;
    private String repositoryPath;
    private String repositoryQuery;
    private String repositoryQueryLanguage;

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

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getRepositoryQuery() {
        return repositoryQuery;
    }

    public void setRepositoryQuery(String repositoryQuery) {
        this.repositoryQuery = repositoryQuery;
    }

    public String getRepositoryQueryLanguage() {
        return repositoryQueryLanguage;
    }

    public void setRepositoryQueryLanguage(String repositoryQueryLanguage) {
        if (isEmpty(repositoryQueryLanguage)) {
            this.repositoryQueryLanguage = REPOSITORY_QUERY_LANGUAGE_DEFAULT;
        } else {
            this.repositoryQueryLanguage = repositoryQueryLanguage;
        }
    }

    public List<RunnerPluginConfig> getPluginConfigs() {
        return new ArrayList<RunnerPluginConfig>(pluginConfigMap.values());
    }

    public RunnerConfig(InputStream inputStream) throws IOException {
        Properties props = new Properties();
        props.load(inputStream);
        readRunnerConfig(props);
        validateRunnerConfig();
        readPluginInformation(props);
    }

    private void readRunnerConfig(Properties props) {
        setRepositoryUrl(props.getProperty(REPOSITORY_URL));
        setRepositoryUser(props.getProperty(REPOSITORY_USER));
        setRepositoryPass(props.getProperty(REPOSITORY_PASS));

        setRepositoryPath(props.getProperty(REPOSITORY_PATH));
        setRepositoryQuery(props.getProperty(REPOSITORY_QUERY));
        setRepositoryQueryLanguage(props.getProperty(REPOSITORY_QUERY_LANGUAGE));
    }

    private void validateRunnerConfig() {
        if (isEmpty(getRepositoryUrl())) {
            throw new IllegalArgumentException("repository.url is missing.");
        }
        if (isEmpty(getRepositoryUser())) {
            throw new IllegalArgumentException("repository.user is missing.");
        }
        if (isEmpty(getRepositoryPass())) {
            throw new IllegalArgumentException("repository.pass is missing.");
        }
        if (isEmpty(getRepositoryPath()) && isEmpty(getRepositoryQuery())) {
            throw new IllegalArgumentException("Both repository.path and repository query are missing.");
        }
    }

    private boolean isEmpty(String s) {
        return s == null || "".equals(s);
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