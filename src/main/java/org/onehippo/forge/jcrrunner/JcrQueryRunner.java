package org.onehippo.forge.jcrrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @version $Id$
 */
public class JcrQueryRunner {
    @SuppressWarnings({"UnusedDeclaration"})
    private static Logger log = LoggerFactory.getLogger(JcrQueryRunner.class);

    private static QueryRunner runner = null;

    private static final String DEFAULT_CONFIG_FILE = "runner.properties";

    /**
     * Private constructor.
     */
    private JcrQueryRunner() {
        super();
    }

    public static void main(final String[] args) throws IOException {
        // read & parse config
        String fileName = DEFAULT_CONFIG_FILE;
        if (args != null && args.length > 0) {
            fileName = args[0];
        }
        File file = new File(fileName);
        QueryRunnerConfig config = new QueryRunnerConfig(new BufferedInputStream(new FileInputStream(file)));

        // register hook for proper shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        JcrHelper.setServerUrl(config.getRepositoryUrl());
        JcrHelper.setUsername(config.getRepositoryUser());
        JcrHelper.setPassword(config.getRepositoryPass());
        JcrHelper.connect();

        // start the query runner
        runner = new QueryRunner();
        runner.registerPlugins(config.getPluginConfigs());
        runner.setQueryLanguage(config.getRepositoryQueryLanguage());
        runner.setQuery(config.getRepositoryQuery());
        runner.start();
        JcrHelper.disconnect();
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
