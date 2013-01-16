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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;
import bsh.TargetError;

public final class RunnerPluginFactory {

    private static final Logger log = LoggerFactory.getLogger(RunnerPluginFactory.class);

    private static final String KEY_SCRIPT = "bsh";
    private static final String KEY_CLASS = "class";

    private RunnerPluginFactory() {
    }

    /**
     * Instantiate a plugin from the beanshell
     * @param config the configuration of the plugin
     * @return a new Instance of the RunnerPlugin
     */
    public static RunnerPlugin createBeanShellPlugin(final RunnerPluginConfig config) {
        try {
            if (config.getValue(KEY_SCRIPT) == null || "".equals(config.getValue(KEY_SCRIPT))) {
                log.error(KEY_SCRIPT + " parameter not found. Not loading plugin: " + config.getId());
                return null;
            }
            RunnerPlugin plugin =(RunnerPlugin) new bsh.Interpreter().source(config.getValue(KEY_SCRIPT));
            plugin.setId(config.getId());
            plugin.setConfig(config);
            return plugin;
        } catch (TargetError e) {
            log.error("The script or code of plugin " + config.getId() + "  threw an exception: " + e.getTarget());
        } catch (EvalError e) {
            log.error("There was an error in evaluating the script of plugin " + config.getId() + ":", e);
        } catch (FileNotFoundException e) {
            log.error("Script not found: " + config.getValue(KEY_SCRIPT) + ". Not loading plugin: " + config.getId());
        } catch (IOException e) {
            log.error("Error while trying to read script: " + config.getValue(KEY_SCRIPT) + "'" + e.getMessage() + "'"
                    + " Not loading plugin: " + config.getId());
        }
        return null;
    }

    /**
     * Instantiate a plugin from the full className
     * @param config the configuration of the plugin
     * @return a new Instance of the RunnerPlugin
     */
    public static RunnerPlugin createJavaPlugin(final RunnerPluginConfig config) {
        try {
            if (config.getValue(KEY_CLASS) == null || "".equals(config.getValue(KEY_CLASS))) {
                log.error(KEY_CLASS + " parameter not found. Not loading plugin: " + config.getId());
                return null;
            }
            RunnerPlugin plugin = (RunnerPlugin) Class.forName(config.getValue(KEY_CLASS)).newInstance();
            plugin.setId(config.getId());
            plugin.setConfig(config);
            return plugin;
        } catch (ClassNotFoundException e) {
            log.error("Class not found: " + config.getValue(KEY_CLASS) + ". Not loading plugin.");
        } catch (InstantiationException e) {
            log.error("Unable to instantiate class: " + config.getValue(KEY_CLASS) + ". Not loading plugin.");
        } catch (IllegalAccessException e) {
            log.error("Class not a RunnerPlugin: " + config.getValue(KEY_CLASS) + ". Not loading plugin.");
        }
        return null;
    }
}
