/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.plugins.ide.jdev.internal

import org.gradle.api.Project
import org.gradle.plugins.ide.jdev.JDevPlugin
import org.gradle.plugins.ide.internal.configurer.DeduplicationTarget
import org.gradle.plugins.ide.internal.configurer.ProjectDeduper

class JDevNameDeduper {

    void configureRoot(Project rootProject) {
        def jdevProjects = rootProject.allprojects.findAll { it.plugins.hasPlugin(JDevPlugin) }
        new ProjectDeduper().dedupe(jdevProjects, { project ->
            new DeduplicationTarget(project: project,
                    moduleName: project.jdevModule.module.name,
                    updateModuleName: { project.jdevModule.module.name = it } )
        })
    }
}
