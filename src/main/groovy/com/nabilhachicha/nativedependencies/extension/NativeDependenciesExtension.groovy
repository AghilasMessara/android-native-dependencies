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

package com.nabilhachicha.nativedependencies.extension
import org.gradle.api.tasks.StopExecutionException
import org.gradle.util.ConfigureUtil

class NativeDependenciesExtension {
    final String CONFIGURATION_SEPARATOR = ":"
    final def classifiers = ['armeabi', 'armeabi-v7a', 'x86', 'mips']

    def dependencies = []
    /**
     * set by a closure to let the user choose if he/she wants to disable
     * prefixing the artifact with 'lib'
     */
    boolean addLibPrefixToArtifact = true

    /**
     * add {@code dep} to the list of dependencies to retrieve
     *
     * @param dep
     * handle String notation ex: artifact com.snappydb:snappydb-native:0.2.+
     */
    def artifact (String dep, Closure... enablePrefixClosure) {
        if (enablePrefixClosure?.size()>0) {
            ConfigureUtil.configure(enablePrefixClosure[0], this);

        } else {// reset to default
            addLibPrefixToArtifact = true;
        }

        def dependency = dep.tokenize(CONFIGURATION_SEPARATOR)
        if (dependency.size() < 3 || dependency.size()>4) {
            throw new StopExecutionException('please specify group:name:version')

        } else if (dependency.size() == 3) {//add classifier
            classifiers.each {
                dependencies << new NativeDep (dependency: dep + CONFIGURATION_SEPARATOR + it, shouldPrefixWithLib: addLibPrefixToArtifact)
            }

        } else {
            dependencies << new NativeDep (dependency: dep, shouldPrefixWithLib: addLibPrefixToArtifact)
        }
    }

    /**
     * add {@code dep} to the list of dependencies to retrieve
     *
     * @param dep
     * artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0'
     *
     * Note: if the user doesn't specify the optional 'classifier', this method will add
     * all the supported architectures to this dependencies ('armeabi', 'armeabi-v7a', 'x86' and 'mips')
     */
    def artifact (Map m, Closure... enablePrefixClosure) {
        if (enablePrefixClosure?.size()>0) {
            ConfigureUtil.configure(enablePrefixClosure[0], this);

        } else {// reset to default
            addLibPrefixToArtifact = true;
        }

        String temp = m['group'] + CONFIGURATION_SEPARATOR + m['name'] + CONFIGURATION_SEPARATOR + m['version']

        if(!m.containsKey('classifier')) {
            classifiers.each {
                dependencies << new NativeDep (dependency: temp + CONFIGURATION_SEPARATOR + it, shouldPrefixWithLib: addLibPrefixToArtifact)
            }
        } else {
            dependencies << new NativeDep (dependency: temp + CONFIGURATION_SEPARATOR + m['classifier'], shouldPrefixWithLib: addLibPrefixToArtifact)
        }
    }
}
