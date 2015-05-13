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

package com.nabilhachicha.nativedependencies
import com.nabilhachicha.nativedependencies.task.NativeDependenciesResolverTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import org.gradle.api.internal.plugins.PluginApplicationException
import static org.junit.Assert.assertTrue

class BasicPluginTest {

    @Test(expected = PluginApplicationException.class)
    public void testShouldApplyAndroidPluginBefore() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'android-native-dependencies'
    }

    @Test
    public void testTaskCreation() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.android.application'
        project.apply plugin: 'android-native-dependencies'
        def task = project.task('resolveNativeDependencies', type: NativeDependenciesResolverTask)

        assertTrue(task instanceof NativeDependenciesResolverTask)
    }

}