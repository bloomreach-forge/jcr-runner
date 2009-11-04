/*
 *  Copyright 2009 Hippo.
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
package org.onehippo.forge.jcrrunner.plugins;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrrunner.RunnerPlugin;
import org.onehippo.forge.jcrrunner.RunnerPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRunnerPlugin implements RunnerPlugin {

    private static final Logger log = LoggerFactory.getLogger(AbstractRunnerPlugin.class);

    /**
     * Holder for the current plugin id
     */
    private String id;

    /**
     * Holder for the current plugin's config
     */
    private RunnerPluginConfig config;

    /**
     * {@inheritDoc}
     */
    public abstract void visit(Node node);

    /**
     * {@inheritDoc}
     */
    public void init() {
        getLogger().info("Starting plugin " + getId());
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
        getLogger().info("Stopping plugin " + getId());
    }

    /**
     * {@inheritDoc}
     */
    public void visitStart(Node node) {
        try {
            getLogger().info("VisitStart node {}", node.getPath());
        } catch (RepositoryException e) {
            getLogger().error("Error getting node path", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitEnd(Node node) {
        try {
            getLogger().info("VisitEnd node {}", node.getPath());
        } catch (RepositoryException e) {
            getLogger().error("Error getting node path", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    final public void setId(String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    final public String getId() {
        return id;
    }

    /**
     * Get the static logger instance.
     * @return
     */
    final protected Logger getLogger() {
        return log;
    }

    final public void setConfig(RunnerPluginConfig config) {
        this.config = config;
    }

    public String getConfigValue(String key) {
        return config.getValue(key);
    }
}
