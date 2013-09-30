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

package org.gradle.plugins.ide.jdev.model.internal

import org.gradle.api.Project
import org.gradle.plugins.ide.jdev.model.JDevModule
import org.gradle.plugins.ide.jdev.model.SingleEntryModuleLibrary
import org.gradle.plugins.ide.internal.IdeDependenciesExtractor

class JDevDependenciesProvider {

    private final IdeDependenciesExtractor dependenciesExtractor = new IdeDependenciesExtractor()
    Closure getPath;

    Set<org.gradle.plugins.ide.jdev.model.Dependency> provide(JDevModule jdevModule) {
        getPath = { File file -> file? jdevModule.pathFactory.path(file) : null }

        Set result = new LinkedHashSet()
        jdevModule.singleEntryLibraries.each { scope, files ->
            files.each {
                if (it && it.isDirectory()) {
                    result << new SingleEntryModuleLibrary(getPath(it), scope)
                }
            }
        }

        jdevModule.scopes.each { scopeName, scopeMap ->
            result.addAll(getModuleLibraries(jdevModule, scopeName, scopeMap))
            result.addAll(getModules(jdevModule.project, scopeName, scopeMap))
            result
        }

        return result
    }

    protected Set getModules(Project project, String scopeName, Map scopeMap) {
        if (!scopeMap) {
            return []
        }
        return dependenciesExtractor.extractProjectDependencies(scopeMap.plus, scopeMap.minus).collect {
                new JDevModuleDependencyBuilder().create(it.project, scopeName)
        }
    }

    protected Set getModuleLibraries(JDevModule jdevModule, String scopeName, Map scopeMap) {
        if (!scopeMap) {
            return []
        }

        LinkedHashSet moduleLibraries = []

        if (!jdevModule.offline) {
            def repoFileDependencies = dependenciesExtractor.extractRepoFileDependencies(
                    jdevModule.project.configurations, scopeMap.plus, scopeMap.minus,
                    jdevModule.downloadSources, jdevModule.downloadJavadoc)

            repoFileDependencies.each {
                def library = new SingleEntryModuleLibrary(
                        getPath(it.file), getPath(it.javadocFile), getPath(it.sourceFile), scopeName)
                library.moduleVersion = it.id
                moduleLibraries << library
            }
        }

        dependenciesExtractor.extractLocalFileDependencies(scopeMap.plus, scopeMap.minus).each {
            moduleLibraries << new SingleEntryModuleLibrary(getPath(it.file), scopeName)
        }
        moduleLibraries
    }
}
