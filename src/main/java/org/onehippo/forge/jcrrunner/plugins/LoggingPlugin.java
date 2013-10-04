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
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example {@link RunnerPlugin} implementation that just logs all calls.
 */
public class LoggingPlugin extends AbstractRunnerPlugin {

    private static Logger log = LoggerFactory.getLogger(LoggingPlugin.class);

    private static final long MILLISECONDS_IN_SECOND = 1000L;
    private long counter;
    private long start;

    @Override
    public void visit(Node node) {
        try {
            log.info("Visiting node {}", node.getPath());
            counter++;
        } catch (RepositoryException e) {
            log.error("Error getting node path", e);
        }
    }

    @Override
    public void init(Session session) {
        start = System.currentTimeMillis();
    }

    @Override
    public void destroy(Session session) {
        long duration = (System.currentTimeMillis() - start) / MILLISECONDS_IN_SECOND;
        log.info("Visited " + counter + " nodes in " + duration + " seconds.");
    }

}
