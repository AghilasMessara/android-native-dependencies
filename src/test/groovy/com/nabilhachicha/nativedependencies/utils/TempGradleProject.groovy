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

package com.nabilhachicha.nativedependencies.utils

import org.apache.commons.io.FileUtils
import org.junit.rules.TemporaryFolder
import org.junit.runner.Description
import org.junit.runners.model.Statement

public class TempGradleProject extends TemporaryFolder {
    final ConfPath[] gradleTemplateDir
    final String GRADLE_BUILD_FILE = "build.gradle"
    File gradleFile
    String[] artifacts
    ConfPath currentDir;

    // Param
    File mJniLibs

    File mMipsDir
    File mX86Dir
    File mArmDir
    File mArmv7aDir

    File mMipsDepFile
    File mX86DepFile
    File mArmDepFile
    File mArmv7aDepFile

    File mMipsDepFileNoLibPrefix
    File mX86DepFileNoLibPrefix
    File mArmDepFileNoLibPrefix
    File mArmv7aDepFileNoLibPrefix

    public TempGradleProject(ConfPath... templateDir) {
        gradleTemplateDir = templateDir
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        File templateDir = new File(currentDir.path)

        def root = getRoot()
        FileUtils.copyDirectory(templateDir, root)

        gradleFile = new File(root, GRADLE_BUILD_FILE)

        mJniLibs = new File(root.absolutePath + currentDir.jniLibsLocation)

        mMipsDir = new File(mJniLibs, 'mips')
        mX86Dir = new File(mJniLibs, 'x86')
        mArmDir = new File(mJniLibs, 'armeabi')
        mArmv7aDir = new File(mJniLibs, 'armeabi-v7a')

        mMipsDepFile = new File(mMipsDir, 'libsnappydb-native.so')
        mX86DepFile = new File(mX86Dir, 'libsnappydb-native.so')
        mArmDepFile = new File(mArmDir, 'libsnappydb-native.so')
        mArmv7aDepFile = new File(mArmv7aDir, 'libsnappydb-native.so')

        mMipsDepFileNoLibPrefix = new File(mMipsDir, 'snappydb-native.so')
        mX86DepFileNoLibPrefix = new File(mX86Dir, 'snappydb-native.so')
        mArmDepFileNoLibPrefix = new File(mArmDir, 'snappydb-native.so')
        mArmv7aDepFileNoLibPrefix = new File(mArmv7aDir, 'snappydb-native.so')

    }

    @Override
    public Statement apply(Statement base, Description description) {
        Artifacts annotation = description.getAnnotation(Artifacts.class);
        if (annotation == null) {
            throw new IllegalStateException(String.format("Test '%s' missing @Artifacts annotation.",
                    description.getDisplayName()));
        }
        artifacts = annotation.value();
        return new RepeatedStatement(base, gradleTemplateDir);
    }


    class RepeatedStatement extends Statement {
        final ConfPath[] dirPaths;
        final Statement test;
        final AccessibleErrorCollector errorCollector;

        public RepeatedStatement(Statement test, ConfPath[] dirPaths) {
            this.dirPaths = dirPaths;
            this.test = test;
            this.errorCollector = new AccessibleErrorCollector();
        }


        @Override
        public void evaluate() throws Throwable {
            for (ConfPath path : dirPaths) {
                currentDir = path;
                before();

                try {
                    test.evaluate();
                } catch (Throwable t) {
                    errorCollector.addError(new AssertionError("For dir: " + currentDir.path, t));
                } finally {
                    after();
                }
            }

            errorCollector.verify();
        }
    }
}