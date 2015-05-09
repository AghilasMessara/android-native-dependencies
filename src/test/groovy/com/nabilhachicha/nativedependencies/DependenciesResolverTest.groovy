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

import com.nabilhachicha.nativedependencies.utils.Artifacts
import com.nabilhachicha.nativedependencies.utils.ConfPath
import com.nabilhachicha.nativedependencies.utils.TempGradleProject
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat

class DependenciesResolverTest {
    @Rule
    public TempGradleProject gradleProject = new TempGradleProject(
            new ConfPath(path: "src${File.separator}test${File.separator}gradle_project_template",
                    jniLibsLocation: "${File.separator}src${File.separator}main${File.separator}jniLibs"),
            new ConfPath(path: "src${File.separator}test${File.separator}gradle_project_template_custom_jni_dir",
                    jniLibsLocation: "${File.separator}src${File.separator}main${File.separator}native_libs"))

    ProjectConnection mConnection

    @Before
    public void setUp() {
        try {
            GradleConnector connector = GradleConnector.newConnector();

            //append DSL to this build
            gradleProject.gradleFile.append "native_dependencies { " + gradleProject.artifacts.join('\n') + " }"

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
        } catch (Exception exception) {
            exception.printStackTrace()
        }
    }

    @After
    public void tearDown() {
        mConnection?.close();
    }

    @Artifacts("artifact 'com.snappydb:snappydb-native:0.2.0'")
    @Test
    public void testDSLResolveWithStringNotationAllArch() {
        assertThat(gradleProject.mJniLibs).exists()

        assertThat(gradleProject.mMipsDepFile).exists()
        assertThat(gradleProject.mX86Dir).exists()
        assertThat(gradleProject.mArmDir).exists()
        assertThat(gradleProject.mArmv7aDir).exists()

        assertThat(gradleProject.mMipsDepFile).exists()
        assertThat(gradleProject.mX86DepFile).exists()
        assertThat(gradleProject.mArmDepFile).exists()
        assertThat(gradleProject.mArmv7aDepFile).exists()
    }

    @Artifacts("artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0'")
    @Test
    public void testDSLResolveWithMappingNotationAllArch() {
        assertThat(gradleProject.mJniLibs).exists()

        assertThat(gradleProject.mMipsDir).exists()
        assertThat(gradleProject.mX86Dir).exists()
        assertThat(gradleProject.mArmDir).exists()
        assertThat(gradleProject.mArmv7aDir).exists()

        assertThat(gradleProject.mMipsDepFile).exists()
        assertThat(gradleProject.mX86DepFile).exists()
        assertThat(gradleProject.mArmDepFile).exists()
        assertThat(gradleProject.mArmv7aDepFile).exists()
    }

    @Artifacts(["artifact 'com.snappydb:snappydb-native:0.2.0:mips'",
            "artifact 'com.snappydb:snappydb-native:0.2.0:x86'"])
    @Test
    public void testDSLResolveWithStringNotationFilterByArch() {
        assertThat(gradleProject.mJniLibs).exists()

        assertThat(gradleProject.mMipsDepFile).exists()
        assertThat(gradleProject.mX86Dir).exists()

        assertThat(gradleProject.mMipsDepFile).exists()
        assertThat(gradleProject.mX86DepFile).exists()
    }

    @Artifacts(["artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'armeabi'",
            "artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'armeabi-v7a'"])
    @Test
    public void testDSLResolveWithMappingNotationFilterByArch() {
        assertThat(gradleProject.mJniLibs).exists()

        assertThat(gradleProject.mArmDir).exists()
        assertThat(gradleProject.mArmv7aDir).exists()

        assertThat(gradleProject.mArmDepFile).exists()
        assertThat(gradleProject.mArmv7aDepFile).exists()
    }

    @Artifacts("artifact group: 'com.snappydb', name: 'snappydb-native', version: '0.2.+', classifier: 'armeabi'")
    @Test
    public void testRangeNotationResolveWithMapping() {
        assertThat(gradleProject.mJniLibs).exists()

        assertThat(gradleProject.mArmDir).exists()

        assertThat(gradleProject.mArmDepFile).exists()
    }

    @Artifacts("artifact 'com.snappydb:snappydb-native:0.2.+:x86'")
    @Test
    public void testRangeNotationResolveWithStringNotation() {
        assertThat(gradleProject.mJniLibs).exists()

        assertThat(gradleProject.mX86Dir).exists()

        assertThat(gradleProject.mX86DepFile).exists()
    }

    // Testing addLibPrefixToArtifact closure

    @Artifacts("artifact (group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'mips') { addLibPrefixToArtifact=false }")
    @Test
    public void testDisableLibPrefixStringNotation() {
        assertThat(gradleProject.mJniLibs).exists()
        assertThat(gradleProject.mMipsDir).exists()
        assertThat(gradleProject.mMipsDepFileNoLibPrefix).exists()
    }

    @Artifacts("artifact (group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'mips') { addLibPrefixToArtifact=true }")
    @Test
    public void testEnableLibPrefixStringNotation() {
        assertThat(gradleProject.mJniLibs).exists()
        assertThat(gradleProject.mMipsDir).exists()
        assertThat(gradleProject.mMipsDepFile).exists()
    }

    @Artifacts(["artifact ('com.snappydb:snappydb-native:0.2.0:mips') { addLibPrefixToArtifact = false } ",
            "artifact 'com.snappydb:snappydb-native:0.2.0:x86'",
            "artifact ('com.snappydb:snappydb-native:0.2.0:armeabi') { addLibPrefixToArtifact = false } ",
            "artifact 'com.snappydb:snappydb-native:0.2.0:armeabi-v7a'"])
    @Test
    public void testLibPrefixMixStringNotation() {
        assertThat(gradleProject.mJniLibs).exists()
        assertThat(gradleProject.mMipsDir).exists()
        assertThat(gradleProject.mX86Dir).exists()
        assertThat(gradleProject.mArmDir).exists()
        assertThat(gradleProject.mArmv7aDir).exists()

        assertThat(gradleProject.mMipsDepFileNoLibPrefix).exists()
        assertThat(gradleProject.mX86DepFile).exists()
        assertThat(gradleProject.mArmDepFileNoLibPrefix).exists()
        assertThat(gradleProject.mArmv7aDepFile).exists()
    }

    @Artifacts(["artifact (group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'mips') {}",
            "artifact (group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'x86') { addLibPrefixToArtifact=false }",
            "artifact (group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'armeabi')",
            "artifact (group: 'com.snappydb', name: 'snappydb-native', version: '0.2.0', classifier: 'armeabi-v7a') { addLibPrefixToArtifact=false }"])
    @Test
    public void testLibPrefixMixMappingNotation() {
        assertThat(gradleProject.mJniLibs).exists()
        assertThat(gradleProject.mMipsDir).exists()
        assertThat(gradleProject.mX86Dir).exists()
        assertThat(gradleProject.mArmDir).exists()
        assertThat(gradleProject.mArmv7aDir).exists()

        assertThat(gradleProject.mMipsDepFile).exists()
        assertThat(gradleProject.mX86DepFileNoLibPrefix).exists()
        assertThat(gradleProject.mArmDepFile).exists()
        assertThat(gradleProject.mArmv7aDepFileNoLibPrefix).exists()
    }
}