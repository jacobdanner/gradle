/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.plugins.ide.jdev.model

import org.gradle.util.ConfigureUtil

/**
 * DSL-friendly model of the JDEV project information.
 * First point of entry when it comes to customizing the JDEV generation.
 * <p>
 * See the examples in docs for {@link JDevModule} or {@link JDevProject}.
 * <p>
 */
class JDevModel {

    /**
     * Configures JDEV module information.
     * <p>
     * For examples see docs for {@link JDevModule}.
     */
    JDevModule module

    /**
     * Configures JDEV project information.
     * <p>
     * For examples see docs for {@link JDevProject}.
     */
    JDevProject project

    /**
     * Configures JDEV workspace information.
     * <p>
     * For examples see docs for {@link JDevWorkspace}.
     */
    JDevWorkspace workspace = new JDevWorkspace()

    /**
     * Configures JDEV module information.
     * <p>
     * For examples see docs for {@link JDevModule}.
     *
     * @param closure
     */
    void module(Closure closure) {
        ConfigureUtil.configure(closure, getModule())
    }

    /**
     * Configures JDEV project information.
     * <p>
     * For examples see docs for {@link JDevProject}.
     *
     * @param closure
     */
    void project(Closure closure) {
        ConfigureUtil.configure(closure, getProject())
    }

    /**
     * Configures JDEV workspace information.
     * <p>
     * For examples see docs for {@link JDevWorkspace}.
     *
     * @param closure
     */
    void workspace(Closure closure) {
        ConfigureUtil.configure(closure, getWorkspace())
    }

    /**
     * Adds path variables to be used for replacing absolute paths in resulting files (*.jpr, etc.).
     * <p>
     * For example see docs for {@link JDevModule}.
     *
     * @param pathVariables A map with String->File pairs.
     */
    void pathVariables(Map<String, File> pathVariables) {
        assert pathVariables != null
        module.pathVariables.putAll pathVariables
    }
}
