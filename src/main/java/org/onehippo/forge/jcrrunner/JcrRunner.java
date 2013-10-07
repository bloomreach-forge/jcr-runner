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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main wrapper to start the runner.
 */
public final class JcrRunner {

    private static Runner runner = null;

    private static final String DEFAULT_CONFIG_FILE = "runner.properties";

    private static final Logger log = LoggerFactory.getLogger(JcrRunner.class);

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

        // start the runner
        runner = new Runner();
        runner.registerPlugins(config.getPluginConfigs());
        JcrHelper.ensureConnected();
        runner.start();
        JcrHelper.disconnect();
    }

    static RunnerConfig parseConfig(String[] args) throws IOException {
        // read & parse config
        InputStream configStream;
        RunnerConfig config;
        if (args != null && args.length > 0) {
            Properties props = new Properties();
            for (String arg : args) {
                File file = new File(arg);
                configStream = new BufferedInputStream(new FileInputStream(file));
                props.load(configStream);
                closeQuietly(configStream);
            }
            config = new RunnerConfig(props);
        } else {
            File file = new File(DEFAULT_CONFIG_FILE);
            configStream = new BufferedInputStream(new FileInputStream(file));
            config = new RunnerConfig(new BufferedInputStream(new FileInputStream(file)));
            closeQuietly(configStream);
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

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            log.debug("Error while closing: " + closeable.getClass().getSimpleName(), ioe);
        }
    }
}
