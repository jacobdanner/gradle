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
package org.gradle.plugins.ide.jdev

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.internal.reflect.Instantiator
import org.gradle.plugins.ide.api.XmlFileContentMerger
import org.gradle.plugins.ide.jdev.internal.JDevNameDeduper
import org.gradle.plugins.ide.jdev.model.*
import org.gradle.plugins.ide.internal.IdePlugin

import javax.inject.Inject

/**
 * Adds a GenerateJDevModule task. When applied to a root project, also adds a GenerateJDevProject task.
 * For projects that have the Java plugin applied, the tasks receive additional Java-specific configuration.
 */
class JDevPlugin extends IdePlugin {
    private final Instantiator instantiator
    JDevModel model

    @Inject
    JDevPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override protected String getLifecycleTaskName() {
        return 'jdev'
    }

    @Override protected void onApply(Project project) {
        lifecycleTask.description = 'Generates JDev project files (JWS, JPR)'
        cleanTask.description = 'Cleans JDev project files (JWS, JPR)'

        model = project.extensions.create("jdev", JDevModel)

        configureJDevWorkspace(project)
        configureJDevProject(project)
        configureJDevModule(project)
        configureForJavaPlugin(project)

        hookDeduplicationToTheRoot(project)
    }

    void hookDeduplicationToTheRoot(Project project) {
        if (isRoot(project)) {
            project.gradle.projectsEvaluated {
                makeSureModuleNamesAreUnique()
            }
        }
    }

    public void makeSureModuleNamesAreUnique() {
        new JDevNameDeduper().configureRoot(project.rootProject)
    }

    private configureJDevWorkspace(Project project) {
        if (isRoot(project)) {
            def task = project.task('jdevWorkspace', description: 'Generates an JDev workspace file (JWS)', type: GenerateJDevWorkspace) {
                workspace = new JDevWorkspace(jws: new XmlFileContentMerger(xmlTransformer))
                model.workspace = workspace
                outputFile = new File(project.projectDir, project.name + ".jws")
            }
            addWorker(task, false)
        }
    }

    private configureJDevModule(Project project) {
        def task = project.task('jdevModule', description: 'Generates JDev module files (JPR)', type: GenerateJDevModule) {
            def jpr = new JDevModuleJpr(xmlTransformer, project.projectDir)
            module = instantiator.newInstance(JDevModule, project, jpr)

            model.module = module

            module.conventionMapping.sourceDirs = { [] as LinkedHashSet }
            module.conventionMapping.name = { project.name }
            module.conventionMapping.contentRoot = { project.projectDir }
            module.conventionMapping.testSourceDirs = { [] as LinkedHashSet }
            module.conventionMapping.excludeDirs = { [project.buildDir, project.file('.gradle')] as LinkedHashSet }

            module.conventionMapping.pathFactory = {
                PathFactory factory = new PathFactory()
                factory.addPathVariable('MODULE_DIR', outputFile.parentFile)
                module.pathVariables.each { key, value ->
                    factory.addPathVariable(key, value)
                }
                factory
            }
        }

        addWorker(task)
    }

    private configureJDevProject(Project project) {
        if (isRoot(project)) {
            def task = project.task('jdevProject', description: 'Generates JDev project file (JPR)', type: GenerateJDevProject) {
                def jpr = new XmlFileContentMerger(xmlTransformer)
                jdevProject = instantiator.newInstance(JDevProject, jpr)

                model.project = jdevProject

                jdevProject.outputFile = new File(project.projectDir, project.name + ".jpr")
                jdevProject.conventionMapping.jdkName = { JavaVersion.current().toString() }
                jdevProject.conventionMapping.languageLevel = { new JDevLanguageLevel(JavaVersion.VERSION_1_6) }
                jdevProject.wildcards = ['!?*.java', '!?*.groovy'] as Set
                jdevProject.conventionMapping.modules = {
                    project.rootProject.allprojects.findAll { it.plugins.hasPlugin(JDevPlugin) }.collect { it.jdev.module }
                }

                jdevProject.conventionMapping.pathFactory = {
                    new PathFactory().addPathVariable('PROJECT_DIR', outputFile.parentFile)
                }
            }
            addWorker(task)
        }
    }

    private configureForJavaPlugin(Project project) {
        project.plugins.withType(JavaPlugin) {
            configureJDevProjectForJava(project)
            configureJDevModuleForJava(project)
        }
    }

    private configureJDevProjectForJava(Project project) {
        if (isRoot(project)) {
            project.jdev.project.conventionMapping.languageLevel = {
                new JDevLanguageLevel(project.sourceCompatibility)
            }
        }
    }

    private configureJDevModuleForJava(Project project) {
        project.jdevModule {
            module.conventionMapping.sourceDirs = { project.sourceSets.main.allSource.srcDirs as LinkedHashSet }
            module.conventionMapping.testSourceDirs = { project.sourceSets.test.allSource.srcDirs as LinkedHashSet }
            def configurations = project.configurations
            module.scopes = [
                    PROVIDED: [plus: [], minus: []],
                    COMPILE: [plus: [configurations.compile], minus: []],
                    RUNTIME: [plus: [configurations.runtime], minus: [configurations.compile]],
                    TEST: [plus: [configurations.testRuntime], minus: [configurations.runtime]]
            ]
            module.conventionMapping.singleEntryLibraries = {
                [
                        RUNTIME: project.sourceSets.main.output.dirs,
                        TEST: project.sourceSets.test.output.dirs
                ]
            }
            dependsOn {
                project.sourceSets.main.output.dirs + project.sourceSets.test.output.dirs
            }
        }
    }


    private boolean isRoot(Project project) {
        return project.parent == null
    }
}

