package org.onehippo.forge.jcrrunner;

import org.hippoecm.repository.api.HippoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public class QueryRunner {
    @SuppressWarnings({"UnusedDeclaration"})
    private static Logger log = LoggerFactory.getLogger(QueryRunner.class);

    private String query;

    private String queryLanguage = "xpath";

    private volatile boolean keepRunning = true;

    private List<RunnerPlugin> plugins = new ArrayList<RunnerPlugin>();

    public void setQuery(String query) {
        this.query = query;
    }

    public void setQueryLanguage(String queryLanguage) {
        this.queryLanguage = queryLanguage;
    }

    /**
     * Check if the node is virtual.
     *
     * @param node
     * @return true if the node is virtual otherwise false
     */
    private boolean isVirtual(Node node) {
        if (node == null) {
            return false;
        }
        if (!(node instanceof HippoNode)) {
            return false;
        }
        HippoNode hippoNode = (HippoNode) node;
        try {
            Node canonical = hippoNode.getCanonicalNode();
            if (canonical == null) {
                return true;
            }
            return !hippoNode.getCanonicalNode().isSame(hippoNode);
        } catch (RepositoryException e) {
            log.error("Error while trying to determine if the node is virtual: " + node.getClass().getName(), e);
            return false;
        }
    }

    public QueryRunner() {
    }

    public void start() {
        log.info("Runner starting.");
        initPlugins();
        keepRunning = true;
        try {
            Session session = JcrHelper.getRootNode().getSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();
            Query jcrQuery = queryManager.createQuery(query, queryLanguage);
            QueryResult results = jcrQuery.execute();

            NodeIterator iter = results.getNodes();

            long i = 0;
            //long j = iter.getSize();
            while (iter.hasNext() && keepRunning) {
                Node child = iter.nextNode();
                if (child != null && !isVirtual(child)) {
                    if (i == 0) {
                      visitStart(child);
                    } else if(!iter.hasNext()){
                      visitEnd(child);
                    } else{
                      visit(child);
                    }
                    JcrHelper.refresh(true);
                }
                i++;
            }
        } catch (PathNotFoundException e) {
            log.error("Path not found");
            destroyPlugins();
        } catch (RepositoryException e) {
            destroyPlugins();
            e.printStackTrace();
        }
        log.info("Runner finished.");
    }

    public void stop() {
        log.debug("Runner stopping.");
        keepRunning = false;
        destroyPlugins();
        log.info("Runner stopped.");
    }

    // ------------------------------- PLUGIN INTERFACES -----------------//
    private void visit(Node node) {
        synchronized (plugins) {
            for (RunnerPlugin plugin : plugins) {
                plugin.visit(node);
            }
        }
    }

    private void visitStart(Node node) {
        synchronized (plugins) {
            for (RunnerPlugin plugin : plugins) {
                plugin.visitStart(node);
            }
        }
    }

    private void visitEnd(Node node) {
        synchronized (plugins) {
            for (RunnerPlugin plugin : plugins) {
                plugin.visitEnd(node);
            }
        }
    }

    public void registerPlugin(RunnerPlugin plugin) {
        synchronized (plugins) {
            log.debug("Registering plugin: " + plugin.getClass().getName());
            plugins.add(plugin);
        }
    }

    public void initPlugins() {
        synchronized (plugins) {
            for (RunnerPlugin plugin : plugins) {
                log.debug("Initializing plugin: " + plugin.getClass().getName());
                plugin.init();
            }
        }
    }

    public void destroyPlugins() {
        synchronized (plugins) {
            for (RunnerPlugin plugin : plugins) {
                log.debug("Destroying plugin: " + plugin.getClass().getName());
                plugin.destroy();
            }
        }
    }

    public void registerPlugins(List<RunnerPluginConfig> pluginConfigs) {
        RunnerPlugin runnerPlugin = null;
        for (RunnerPluginConfig pluginConfig : pluginConfigs) {
            switch (pluginConfig.getType()) {
                case JAVA:
                    runnerPlugin = RunnerPluginFactory.createJavaPlugin(pluginConfig);
                    break;
                case BEANSHELL:
                    runnerPlugin = RunnerPluginFactory.createBeanShellPlugin(pluginConfig);
                    break;
                default:
                    log.error("Unknown plugin of type {}", pluginConfig.getType());
            }
            if (runnerPlugin != null) {
                registerPlugin(runnerPlugin);
            }
        }
    }


}
