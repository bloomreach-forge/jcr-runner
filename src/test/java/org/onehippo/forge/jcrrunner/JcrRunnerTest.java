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

import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * @author Jettro Coenradie
 */
public class JcrRunnerTest {
    @Test
    public void checkInitializationUsingDefault() throws IOException {
        @SuppressWarnings({"NullableProblems"})
        RunnerConfig runnerConfig = JcrRunner.parseConfig(null);
        assertEquals("admin", runnerConfig.getRepositoryUser());
        assertEquals("admin", runnerConfig.getRepositoryPass());
        assertEquals("rmi://127.0.0.1:1099/hipporepository", runnerConfig.getRepositoryUrl());
    }

    @Test
    public void checkInitializationUsingOther() throws IOException {
        String[] args = {"src/test/resources/other.properties"};
        RunnerConfig runnerConfig = JcrRunner.parseConfig(args);
        assertEquals("testuser", runnerConfig.getRepositoryUser());
        assertEquals("testpass", runnerConfig.getRepositoryPass());
        assertEquals("rmi://127.0.0.1:1099/hipporepository", runnerConfig.getRepositoryUrl());
    }

    @Test
    public void checkInitializationUsingMultiple() throws IOException {
        String[] args = {"runner.properties", "src/test/resources/other.properties"};
        RunnerConfig runnerConfig = JcrRunner.parseConfig(args);
        assertEquals("testuser", runnerConfig.getRepositoryUser());
        assertEquals("testpass", runnerConfig.getRepositoryPass());
        assertEquals("rmi://127.0.0.1:1099/hipporepository", runnerConfig.getRepositoryUrl());
    }
}
