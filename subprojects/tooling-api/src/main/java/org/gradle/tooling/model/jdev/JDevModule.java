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

package org.gradle.tooling.model.jdev;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.HasGradleProject;
import org.gradle.tooling.model.HierarchicalElement;

/**
 * Represents information about the JDev module.
 *
 * @since 1.0-milestone-5
 */
public interface JDevModule extends HierarchicalElement, HasGradleProject {

    /**
     * All content roots. Most JDev modules have a single content root.
     *
     * @return content roots
     * @since 1.0-milestone-5
     */
    DomainObjectSet<? extends JDevContentRoot> getContentRoots();

    /**
     * The gradle project that is associated with this module.
     * Typically, a single module corresponds to a single gradle project.
     * <p>
     * See {@link org.gradle.tooling.model.HasGradleProject}
     *
     * @return associated gradle project
     * @since 1.0-milestone-5
     */
    GradleProject getGradleProject();

    /**
     * Returns the project of this module.
     * Alias to {@link #getProject()}
     *
     * @return JDev project
     * @since 1.0-milestone-5
     */
    JDevProject getParent();

    /**
     * Returns the project of this module.
     * Alias to {@link #getParent()}
     *
     * @return JDev project
     * @since 1.0-milestone-5
     */
    JDevProject getProject();

    /**
     * Returns information about JDev compiler output (output dirs, inheritance of output dir, etc.)
     *
     * @since 1.0-milestone-5
     */
    JDevCompilerOutput getCompilerOutput();

    /**
     * dependencies of this module (i.e. module dependencies, library dependencies, etc.)
     *
     * @return dependencies
     * @since 1.0-milestone-5
     */
    DomainObjectSet<? extends JDevDependency> getDependencies();
}