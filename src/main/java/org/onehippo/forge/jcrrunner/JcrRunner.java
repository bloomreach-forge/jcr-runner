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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Main wrapper to start the runner.
 */
public final class JcrRunner {

    private static final Logger log = LoggerFactory.getLogger(JcrRunner.class);

    private static Runner runner = null;

    private static final String DEFAULT_CONFIG_FILE = "runner.properties";

    /**
     * Private constructor.
     */
    private JcrRunner() {
        super();
    }

    public static void main(final String[] args) throws IOException {
        RunnerConfig config = parseConfig(args);

        // register hook for proper shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        JcrHelper.setServerUrl(config.getRepositoryUrl());
        JcrHelper.setUsername(config.getRepositoryUser());
        JcrHelper.setPassword(config.getRepositoryPass());
        JcrHelper.ensureConnected();

        try {
            // start the runner
            runner = new Runner();
            runner.registerPlugins(config.getPluginConfigs());
            runner.setPath(config.getRepositoryPath());
            runner.setQueryLanguage(config.getRepositoryQueryLanguage());
            runner.setQuery(config.getRepositoryQuery());
            runner.start();
        } catch (PathNotFoundException e) {
            log.error("Configured start path not found: " + e.getMessage());
        } catch (RepositoryException e) {
            log.error("Error while trying to set start path: " + config.getRepositoryPath(), e);
        }
        JcrHelper.disconnect();
    }

    static RunnerConfig parseConfig(String[] args) throws IOException {
        // read & parse config
        RunnerConfig config;
        if (args != null && args.length > 0) {
            Properties props = new Properties();
            for (String arg : args) {
                File file = new File(arg);
                props.load(new BufferedInputStream(new FileInputStream(file)));
            }
            config = new RunnerConfig(props);
        } else {
            File file = new File(DEFAULT_CONFIG_FILE);
            config = new RunnerConfig(new BufferedInputStream(new FileInputStream(file)));
        }
        return config;
    }

    /**
     * Trivial shutdown hook class.
     */
    static class ShutdownHook extends Thread {
        /**
         * Exit properly on shutdown.
         */
        public void run() {
            if (runner != null) {
                runner.stop();
            }
            JcrHelper.disconnect();
        }
    }
}
