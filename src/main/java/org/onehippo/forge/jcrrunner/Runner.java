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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {

    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    private static final String REPOSITORY_QUERY_LANGUAGE_DEFAULT = "xpath";

    private List<RunnerPlugin> plugins = new ArrayList<RunnerPlugin>();

    // plugin state
    private static final long MILLISECONDS_IN_SECOND = 1000L;
    private long counter;
    private long start;
    private List<String> pathElements;
    private int level = 0;
    private int wildcardLevel = -1;
    private RunnerPlugin activePlugin;
    private volatile boolean keepRunning = true;

    //------------------------------- RUNNER LIFECYCLE -----------------------//
    public Runner() {
    }

    public void start() {
        log.info("Runners starting.");
        for (RunnerPlugin plugin : plugins) {
            initPlugin(plugin);
            runVisitor(plugin);
            destroyPlugin(plugin);
            JcrHelper.refresh(false);
        }
        log.info("Runners finished.");
    }

    public void stop() {
        log.debug("Interrupt intercepted. Stopping runner.");
        if (activePlugin != null) {
            destroyPlugin(activePlugin);
        }
        log.info("Runner stopped.");
    }

    //------------------------------- VISITOR ------------------------?
    private void recursiveVisit(RunnerPlugin plugin, String path) throws RepositoryException {
        Node node;
        try {
            node = JcrHelper.getNode(path);
        } catch (PathNotFoundException e) {
            log.info("Path not found: " + path);
            return;
        }
        counter++;
        plugin.visit(node);

        if (node.hasNodes()) {
            NodeIterator iter = node.getNodes();
            while (keepRunning && iter.hasNext()) {
                final Node child = iter.nextNode();
                if (child != null && !JcrHelper.isVirtual(child)) {
                    level++;
                    try {
                        String name = child.getName();
                        if (matchNodePath(name)) {
                            recursiveVisit(plugin, child.getPath());
                        }
                    } catch (InvalidItemStateException e) {
                        log.warn("InvalidItemStateException while getting child node, the node will be skipped: "
                                + e.getMessage());
                    }
                    level--;
                }
            }
        }
    }

    private void runPathVisitor(RunnerPlugin plugin) throws RepositoryException {
        String path = plugin.getConfigValue("path");
        if (path == null || path.length() == 0) {
            log.info("{}: No path set. Skipping path visitor.", plugin.getId());
            return;
        }

        String absPath = makePathAbsolute(path);
        String startPath = findStartPath(absPath);
        pathElements = Arrays.asList(absPath.substring(1).split("/"));
        level = startPath.split("/").length - 2;
        wildcardLevel = pathElements.indexOf("**");

        if (JcrHelper.safeItemExists(startPath)) {
            log.info("{}: Using path '{}'", plugin.getId(), path);
            recursiveVisit(plugin, startPath);
        } else {
            log.warn("{}: Path not found '{}'. Skipping path visitor.", plugin.getId(), startPath);
        }
    }

    private void runQueryVisitor(RunnerPlugin plugin) throws RepositoryException {
        String query = plugin.getConfigValue("query");
        String language = plugin.getConfigValue("query.language", REPOSITORY_QUERY_LANGUAGE_DEFAULT);
        if (query == null) {
            log.info("{}: No query set. Skipping query visitor.", plugin.getId());
            return;
        }

        log.info("{}: Using query '{}', type '{}'", new String[] {plugin.getId(), query, language});

        Session session = JcrHelper.getSession();
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query jcrQuery = queryManager.createQuery(query, language);
        QueryResult results = jcrQuery.execute();
        NodeIterator resultsIter = results.getNodes();

        while (keepRunning && resultsIter.hasNext()) {
            Node child = resultsIter.nextNode();
            if (child != null && !JcrHelper.isVirtual(child)) {
                // make sure the node is valid and exists
                String childPath = JcrHelper.safeGetPath(child);
                if (JcrHelper.safeItemExists(childPath)) {
                    counter++;
                    plugin.visit(child);
                }
            }
        }
    }

    //------------------------------- PLUGIN LIFECYCLE -----------------------//
    public void registerPlugins(List<RunnerPluginConfig> pluginConfigs) {
        RunnerPlugin runnerPlugin = null;
        for (RunnerPluginConfig pluginConfig : pluginConfigs) {
            switch (pluginConfig.getType()) {
            case JAVA:
                runnerPlugin = RunnerPluginFactory.createJavaPlugin(pluginConfig);
                log.info("Registering java plugin {}.", pluginConfig.getId());
                break;
            case BEANSHELL:
                runnerPlugin = RunnerPluginFactory.createBeanShellPlugin(pluginConfig);
                log.info("Registering beanshell plugin {}.", pluginConfig.getId());
                break;
            default:
                log.error("Unknown plugin of type {}", pluginConfig.getType());
            }
            if (runnerPlugin != null) {
                registerPlugin(runnerPlugin);
            }
        }
    }

    public void registerPlugin(RunnerPlugin plugin) {
        log.debug(plugin.getId() + ": Registering plugin: " + plugin.getClass().getName());
        plugins.add(plugin);
    }

    public void initPlugin(RunnerPlugin plugin) {
        keepRunning = true;
        activePlugin = plugin;
        start = System.currentTimeMillis();
        counter = 0;
        log.info("{}: Initializing plugin class: {}", plugin.getId(), plugin.getClass().getName());
        plugin.init(JcrHelper.getSession());
    }

    public void runVisitor(RunnerPlugin plugin) {
        try {
            runPathVisitor(plugin);
        } catch (RepositoryException e) {
            log.error(plugin.getId() + ": Error while trying to run path visitor for " + plugin.getId(), e);
        } catch (RunnerStopException e) {
            log.info(plugin.getId() + ": Path visitor stopped: {}", e.getMessage());
        }
        try {
            runQueryVisitor(plugin);
        } catch (RepositoryException e) {
            log.error(plugin.getId() + ": Error while trying to run query visitor for " + plugin.getId(), e);
        } catch (RunnerStopException e) {
            log.info(plugin.getId() + ": Query visitor stopped: {}", e.getMessage());
        }
    }

    public void destroyPlugin(RunnerPlugin plugin) {
        keepRunning = false;
        activePlugin = null;
        plugin.destroy(JcrHelper.getSession());
        long duration = (System.currentTimeMillis() - start) / MILLISECONDS_IN_SECOND;
        log.info(plugin.getId() + ": Visited " + counter + " nodes in " + duration + " seconds.");
        log.info("{}: Destroying plugin class: {}", plugin.getId(), plugin.getClass().getName());
    }

    //-------------------------------- PATH PARSING -----------------------------//
    private String makePathAbsolute(String path) {
        if (path == null || path.length() == 0) {
            return "/";
        } else if (!path.startsWith("/")) {
            return "/" + path;
        } else {
            return path;
        }
    }

    private String findStartPath(String absPath) {
        String beginPath;
        int starPos = absPath.indexOf('*');
        if (starPos > 0) {
            beginPath = absPath.substring(0, starPos);
        } else {
            beginPath = absPath;
        }
        int pos = beginPath.lastIndexOf('/');
        if (pos > -1) {
            beginPath = beginPath.substring(0, pos);
        }
        return beginPath;
    }

    /**
     * Match:
     * - /asdf/**
     * - /asdf/asd*sdf
     * - /asdf/qwer
     */
    private boolean matchNodePath(String path) {
        if (wildcardLevel != -1 && level >= wildcardLevel) {
            return true;
        }
        if (level >= pathElements.size()) {
            return false;
        }
        String element = pathElements.get(level);
        if (element.equals("*")) {
            return true;
        }
        if (element.equals(path)) {
            return true;
        }
        int pos = element.indexOf('*');
        if (pos > 0) {
            String prefix = element.substring(0, pos);
            String suffix = "";
            if ((pos + 1) < element.length()) {
                suffix = element.substring(pos + 1);
            }
            if (path.startsWith(prefix) && path.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

}
