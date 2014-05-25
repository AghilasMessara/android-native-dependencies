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
import org.gradle.api.tasks.TaskInstantiationException
import static org.junit.Assert.assertTrue

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import org.junit.Rule
import org.junit.Before
import org.junit.After
import com.nabilhachicha.nativedependencies.utils.*
import static org.fest.assertions.api.Assertions.assertThat
import org.gradle.tooling.ProgressListener
import org.gradle.tooling.ProgressEvent


class DependenciesResolverTest {
    @Rule
    public TempGradleProject gradleProject = new TempGradleProject();

    ProjectConnection mConnection

    File mJniLibs

    File mMipsDir
    File mX86Dir
    File mArmDir
    File mArmv7aDir

    File mMipsDepFile
    File mX86DepFile
    File mArmDepFile
    File mArmv7aDepFile

    @Before
    public void setUp() {
        try {
            GradleConnector connector = GradleConnector.newConnector();

            //append DSL to this build
            gradleProject.gradleFile.append "native_dependencies { " + gradleProject.artifacts.join('\n') +  " }"

            connector.forProjectDirectory(gradleProject.root);
            mConnection = connector.connect();

            // Configure the build
            BuildLauncher launcher = mConnection.newBuild();
            launcher.forTasks("resolveNativeDependencies");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            launcher.setStandardOutput(outputStream);
            launcher.setStandardError(outputStream);

            // Run the build
            launcher.run();

            mJniLibs = new File(gradleProject.root.absolutePath +
                    File.separator + 'src' +
                    File.separator + 'main' +
                    File.separator + 'jniLibs')

            mMipsDir = new File(mJniLibs, 'mips')
            mX86Dir = new File(mJniLibs, 'x86')
            mArmDir = new File(mJniLibs, 'armeabi')
            mArmv7aDir = new File(mJniLibs, 'armeabi-v7a')

            mMipsDepFile = new File(mMipsDir, 'libsnappydb-native.so')
            mX86DepFile = new File(mX86Dir, 'libsnappydb-native.so')
            mArmDepFile = new File(mArmDir, 'libsnappydb-native.so')
            mArmv7aDepFile = new File(mArmv7aDir, 'libsnappydb-native.so')

        } catch (Exception exception) {}
    }

    @After
    public void tearDown() {
        mConnection?.close();
    }

    @Artifacts ("artifact 'com.snappydb:snappydb-native:0.2.0'")
    @Test
    public void testDSLResolveWithStringNotationAllArch() {
        assertThat(mJniLibs).exists()

        assertThat(mMipsDepFile).exists()
        assertThat(mX86Dir).exists()
        assertThat(mArmDir).exists()
        assertThat(mArmv7aDir).exists()

        assertThat(mMipsDepFile).exists()
        assertThat(mX86DepFile).exists()
        assertThat(mArmDepFile).exists()
        assertThat(mArmv7aDepFile).exists()
    }

    @Artifacts ("artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0'")
    @Test
    public void testDSLResolveWithMappingNotationAllArch() {
        assertThat(mJniLibs).exists()

        assertThat(mMipsDir).exists()
        assertThat(mX86Dir).exists()
        assertThat(mArmDir).exists()
        assertThat(mArmv7aDir).exists()

        assertThat(mMipsDepFile).exists()
        assertThat(mX86DepFile).exists()
        assertThat(mArmDepFile).exists()
        assertThat(mArmv7aDepFile).exists()
    }

    @Artifacts (["artifact 'com.snappydb:snappydb-native:0.2.0:mips'",
            "artifact 'com.snappydb:snappydb-native:0.2.0:x86'"])
    @Test
    public void testDSLResolveWithStringNotationFilterByArch() {
        assertThat(mJniLibs).exists()

        assertThat(mMipsDepFile).exists()
        assertThat(mX86Dir).exists()

        assertThat(mMipsDepFile).exists()
        assertThat(mX86DepFile).exists()
    }

    @Artifacts (["artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'armeabi'",
            "artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'armeabi-v7a'"])
    @Test
    public void testDSLResolveWithMappingNotationFilterByArch() {
        assertThat(mJniLibs).exists()

        assertThat(mArmDir).exists()
        assertThat(mArmv7aDir).exists()

        assertThat(mArmDepFile).exists()
        assertThat(mArmv7aDepFile).exists()
    }

    @Artifacts ("artifact 'com.snappydb:snappydb-native:0.2.0:powerpc'")
    @Test
    public void testUnsupportedArchitectureWithStringNotation() {
        // throw IllegalArgumentException: Unsupported architecture for
        // artifact 'com.snappydb:snappydb-native:0.2.0:powerpc'
        assertThat(mJniLibs).isNull()
    }

    @Artifacts ("artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'powerpc'")
    @Test
    public void testUnsupportedArchitectureWithMappingNotation() {
        assertThat(mJniLibs).isNull()
    }
}