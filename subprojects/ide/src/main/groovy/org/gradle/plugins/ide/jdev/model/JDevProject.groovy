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

import org.gradle.api.Incubating
import org.gradle.plugins.ide.api.XmlFileContentMerger
import org.gradle.util.ConfigureUtil

/**
 * Enables fine-tuning project details (*.jpr file) of the JDEV plugin.
 * <p>
 * Example of use with a blend of all possible properties.
 * Typically you don't have configure JDEV module directly because Gradle configures it for you.
 *
 * <pre autoTested=''>
 * import org.gradle.plugins.ide.jdev.model.*
 *
 * apply plugin: 'java'
 * apply plugin: 'jdev'
 *
 * jdev {
 *   project {
 *     //if you want to set specific jdk and language level
 *     jdkName = '1.6'
 *     languageLevel = '1.5'
 *
 *     //you can update the source wildcards
 *     wildcards += '!?*.ruby'
 *
 *     //you can change the modules of the the *.jpr
 *     //modules = project(':someProject').jdev.module
 *
 *     //you can change the output file
 *     outputFile = new File(outputFile.parentFile, 'someBetterName.jpr')
 *
 *     //you can add project-level libraries
 *     projectLibraries &lt;&lt; new ProjectLibrary(name: "my-library", classes: [new Path("path/to/library")])
 *   }
 * }
 * </pre>
 *
 * For tackling edge cases users can perform advanced configuration on resulting XML file.
 * It is also possible to affect the way JDEV plugin merges the existing configuration
 * via beforeMerged and whenMerged closures.
 * <p>
 * beforeMerged and whenMerged closures receive {@link Project} object
 * <p>
 * Examples of advanced configuration:
 *
 * <pre autoTested=''>
 * apply plugin: 'java'
 * apply plugin: 'jdev'
 *
 * jdev {
 *   project {
 *     jpr {
 *       //you can tinker with the output *.jpr file before it's written out
 *       withXml {
 *         def node = it.asNode()
 *         node.appendNode('iLove', 'tinkering with the output *.jpr file!')
 *       }
 *
 *       //closure executed after *.jpr content is loaded from existing file
 *       //but before gradle build information is merged
 *       beforeMerged { project ->
 *         //you can tinker with {@link Project}
 *       }
 *
 *       //closure executed after *.jpr content is loaded from existing file
 *       //and after gradle build information is merged
 *       whenMerged { project ->
*         //you can tinker with {@link Project}
 *       }
 *     }
 *   }
 * }
 * </pre>
 */
class JDevProject {

    /**
     * A {@link org.gradle.api.dsl.ConventionProperty} that holds modules for the jpr file.
     * <p>
     * See the examples in the docs for {@link JDevProject}
     */
    List<JDevModule> modules

    /**
     * The java version used for defining the project sdk.
     * <p>
     * See the examples in the docs for {@link JDevProject}
     */
    String jdkName

    /**
     * The java language level of the project.
     * Pass a valid Java version number (e.g. '1.5') or JDEV language level (e.g. 'JDK_1_5').
     * <p>
     * See the examples in the docs for {@link JDevProject}.
     */
    JDevLanguageLevel languageLevel

    void setLanguageLevel(Object languageLevel) {
        this.languageLevel = new JDevLanguageLevel(languageLevel)
    }

    /**
     * The wildcard resource patterns.
     * <p>
     * See the examples in the docs for {@link JDevProject}.
     */
    Set<String> wildcards

    /**
     * Output *.jpr
     * <p>
     * See the examples in the docs for {@link JDevProject}.
     */
    File outputFile

    /**
     * The project-level libraries to be added to the JDEV project.
     */
    @Incubating
    Set<ProjectLibrary> projectLibraries = [] as LinkedHashSet

    /**
     * The name of the JDEV project. It is a convenience property that returns the name of the output file (without the file extension).
     * In JDEV, the project name is driven by the name of the 'jpr' file.
     */
    String getName() {
       getOutputFile().name.replaceFirst(/\.jpr$/, '')
    }

    /**
     * Enables advanced configuration like tinkering with the output XML
     * or affecting the way existing *.jpr content is merged with Gradle build information.
     * <p>
     * See the examples in the docs for {@link JDevProject}
     */
    public void jpr(Closure closure) {
        ConfigureUtil.configure(closure, getIpr())
    }

    /**
     * See {@link #jpr(Closure) }
     */
    final XmlFileContentMerger jpr

    PathFactory pathFactory

    JDevProject(XmlFileContentMerger jpr) {
        this.jpr = jpr
    }

    void mergeXmlProject(Project xmlProject) {
        jpr.beforeMerged.execute(xmlProject)
        def modulePaths = getModules().collect {
            getPathFactory().relativePath('PROJECT_DIR', it.outputFile)
        }
        xmlProject.configure(modulePaths, getJdkName(), getLanguageLevel(), getWildcards(), getProjectLibraries())
        jpr.whenMerged.execute(xmlProject)
    }
}
