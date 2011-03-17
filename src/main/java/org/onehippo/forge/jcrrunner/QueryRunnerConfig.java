package org.onehippo.forge.jcrrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @version $Id$
 */
public class QueryRunnerConfig {
    @SuppressWarnings({"UnusedDeclaration"})
    private static Logger log = LoggerFactory.getLogger(QueryRunnerConfig.class);

    private static final String REPOSITORY_URL = "repository.url";
    private static final String REPOSITORY_USER = "repository.user";
    private static final String REPOSITORY_PASS = "repository.pass";
    private static final String REPOSITORY_QUERY = "repository.query";

    private static final String REPOSITORY_QUERY_LANGUAGE = "repository.query.language";

    private static final String PLUGINS_JAVA = "plugins.java";
    private static final String PLUGINS_BEANSHELL = "plugins.beanshell";

    private SortedMap<String, RunnerPluginConfig> pluginConfigMap = new TreeMap<String, RunnerPluginConfig>();

    private String repositoryUrl;
    private String repositoryUser;
    private String repositoryPass;
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
        this.repositoryQueryLanguage = repositoryQueryLanguage;
    }

    public List<RunnerPluginConfig> getPluginConfigs() {
        return new ArrayList<RunnerPluginConfig>(pluginConfigMap.values());
    }

    public QueryRunnerConfig(InputStream inputStream) throws IOException {
        Properties props = new Properties();
        props.load(inputStream);
        readRepositoryConfig(props);
        readPluginInformation(props);
    }

    private void readRepositoryConfig(Properties props) {
        String url = props.getProperty(REPOSITORY_URL);
        if (url == null || url.equals("")) {
            throw new IllegalArgumentException("repository.url is missing.");
        }
        setRepositoryUrl(url);
        String user = props.getProperty(REPOSITORY_USER);
        if (user == null || user.equals("")) {
            throw new IllegalArgumentException("repository.user is missing.");
        }
        setRepositoryUser(user);
        String pass = props.getProperty(REPOSITORY_PASS);
        if (pass == null || pass.equals("")) {
            throw new IllegalArgumentException("repository.pass is missing.");
        }
        setRepositoryPass(pass);
        String query = props.getProperty(REPOSITORY_QUERY);
        if (query == null || query.equals("")) {
            throw new IllegalArgumentException("repository.query is missing.");
        }
        setRepositoryQuery(query);
        String queryLanguage = props.getProperty(REPOSITORY_QUERY_LANGUAGE);
        if (queryLanguage == null || query.equals("")) {
            throw new IllegalArgumentException("repository.query.language is missing.");
        }
        setRepositoryQueryLanguage(queryLanguage);

    }

    private void readPluginInformation(Properties props) {
        Set<Map.Entry<Object, Object>> entries = props.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
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
