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
package org.onehippo.forge.jcrrunner.plugins;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Example {@link RunnerPlugin} implementation that just logs all calls.
 */
public class LoggingPlugin extends AbstractRunnerPlugin {

    private static final long MILLISECONDS_IN_SECOND = 1000L;
    private long counter;
    private long start;

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Node node) {
        try {
            getLogger().info("Visiting node {}", node.getPath());
            counter++;
        } catch (RepositoryException e) {
            getLogger().error("Error getting node path", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitStart(Node node) {
        start = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    public void visitEnd(Node node) {
        long duration = (System.currentTimeMillis() - start) / MILLISECONDS_IN_SECOND;
        getLogger().info("Visited " + counter + " nodes in " + duration + " seconds.");
    }

}
