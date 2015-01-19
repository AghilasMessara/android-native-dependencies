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

class NativeDependenciesExtension {
    final String CONFIGURATION_SEPARATOR = ":"
    def dependencies = []
    def classifiers = ['armeabi', 'armeabi-v7a', 'x86', 'x86_64', 'mips']

    /**
     * add {@code dep} to the list of dependencies to retrieve
     *
     * @param dep
     * handle String notation ex: artifact com.snappydb:snappydb-native:0.2.+
     */
    def artifact (String dep) {
        def dependency = dep.tokenize(CONFIGURATION_SEPARATOR)
        if (dependency.size() < 3 || dependency.size()>4) {
            throw new StopExecutionException('please specify group:name:version')

        } else if (dependency.size() == 3) {//add classifier
            classifiers.each {dependencies <<  dep + CONFIGURATION_SEPARATOR + it}

        } else {
            dependencies << dep
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
    def artifact (Map m) {
        String temp = m['group'] + CONFIGURATION_SEPARATOR + m['name'] + CONFIGURATION_SEPARATOR + m['version']

        if(!m.containsKey('classifier')) {
            classifiers.each {dependencies <<  temp + CONFIGURATION_SEPARATOR + it}
        } else {
            dependencies << temp + CONFIGURATION_SEPARATOR + m['classifier']
        }
    }
}
