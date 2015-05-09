/*
 * Copyright (C) 2014 Nabil HACHICHA.
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

package com.nabilhachicha.nativedependencies.task

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.artifacts.Dependency
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import java.io.File
import com.nabilhachicha.nativedependencies.extension.NativeDep

class NativeDependenciesResolverTask extends DefaultTask {
	def @Input dependencies
	def @OutputDirectory jniLibs = new File("$project.projectDir"+File.separator+"src" +
                                            File.separator+"main"+
                                            File.separator+"jniLibs")
	
    final String X86_FILTER = "x86"
    final String X86_64_FILTER = "x86_64"
    final String MIPS_FILTER = "mips"
    final String ARM_FILTER = "armeabi"
    final String ARMV7A_FILTER = "armeabi-v7a"
    final String DEPENDENCY_SUFFIX = "@so"
    final String ARTIFACT_FILE_EXT = ".so"

    final Logger log = Logging.getLogger NativeDependenciesResolverTask

    @TaskAction
    def exec (IncrementalTaskInputs inputs) {
    	project.delete {jniLibs}
        log.lifecycle "Executing NativeDependenciesResolverTask"
        dependencies.each { artifact ->
            log.info "Processing artifact: '$artifact.dependency'"
            copyToJniLibs artifact
        }
    }

    def copyToJniLibs (NativeDep artifact) {
        String filter

        if (artifact.dependency.endsWith(X86_FILTER)) {
            filter = X86_FILTER

        } else if (artifact.endsWith(X86_64_FILTER)) {
            filter = X86_64_FILTER
            
        } else if  (artifact.endsWith(MIPS_FILTER)) {
            filter = MIPS_FILTER

        } else if  (artifact.dependency.endsWith(ARM_FILTER)) {
            filter = ARM_FILTER

        } else if  (artifact.dependency.endsWith(ARMV7A_FILTER)) {
            filter = ARMV7A_FILTER

        } else {
            throw new IllegalArgumentException("Unsupported architecture for artifact '${artifact.dependency}'.")
        }


        def map = downloadDep (artifact.dependency)

        if (!map.isEmpty()) {
            copyToTarget (map.depFile, filter, map.depName, artifact.shouldPrefixWithLib)

        } else {
            throw new StopExecutionException("Failed to retrieve artifcat '$artifact'")
        }
    }


    /**
     * Download (or use gradle cache) the artifact from the user's defined repositories
     *
     * @param artifact
     * The dependency notation, in one of the accepted notations:
     *
     * native_dependencies {
     *   //the string notation, e.g. group:name:version
     *   artifact com.snappydb:snappydb-native:0.2.+
     *
     *   //map notation:
     *   artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0'
     *
     *   //optional, you can specify the 'classifier' in order to restrict the desired architecture(s)
     *   artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'armeabi'
     *   //or
     *   artifact com.snappydb:snappydb-native:0.2.+:armeabi
     * }
     *
     * @return
     *  the dependency {@link java.io.File} or null
     */
    def downloadDep (String artifact) {
        log.info "Trying to resolve artifact '$artifact' using defined repositories"

        def map = [:]
        Dependency dependency = project.dependencies.create(artifact + DEPENDENCY_SUFFIX)
        Configuration configuration = project.configurations.detachedConfiguration(dependency)
        configuration.setTransitive(false)

        configuration.files.each { file ->
            if (file.isFile() && file.name.endsWith(ARTIFACT_FILE_EXT)) {
                map ['depFile'] = file
                map ['depName'] = dependency.getName()
            } else {
                log.info "Could not find the file corresponding to the artifact '$artifact'"
            }
        }
        return map
    }

    /**
     * Copy the artifact file from gradle cache to the project appropriate jniLibs directory
     *
     * @param depFile
     * {@link java.io.File} to copy
     *
     * @param architecture
     * supported jniLibs architecture ("x86", "mips", "armeabi" or "armeabi-v7a")
     *
     * @param shouldPrefixWithLib
     * enable or disable the standard 'lib' prefix to an artifact name
     */
    def copyToTarget (File depFile, String architecture, String depName, boolean shouldPrefixWithLib) {
        project.copy {
            from depFile
            into "$project.projectDir"+File.separator+
                    "src"+File.separator+"main"+File.separator+
                    "jniLibs"+File.separator+"$architecture"
            rename { fileName ->
                if (shouldPrefixWithLib) {
                    "lib" + depName + ".so"
                } else {
                    depName + ".so"
                }
            }
        }
    }
}
