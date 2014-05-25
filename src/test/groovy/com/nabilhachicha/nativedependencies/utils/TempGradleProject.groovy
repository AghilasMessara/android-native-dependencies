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

import java.io.File
import org.apache.commons.io.FileUtils
import org.junit.rules.TemporaryFolder
import org.junit.runners.model.Statement;
import org.junit.runner.Description;

public class TempGradleProject extends TemporaryFolder {
    final String GRADLE_TEMPLATE_DIRECTORY = "src"+File.separator+"test"+File.separator+"gradle_project_template"
    final String GRADLE_BUILD_FILE = "build.gradle"
    File gradleFile
    String[] artifacts

    @Override
    protected void before() throws Throwable {
        super.before();

        File templateDir = new File(GRADLE_TEMPLATE_DIRECTORY)

        def root = getRoot()
        FileUtils.copyDirectory(templateDir, root)

        gradleFile = new File(root, GRADLE_BUILD_FILE)
    }

    @Override
    public Statement apply(Statement base, Description description) {
        Artifacts annotation = description.getAnnotation(Artifacts.class);
        if (annotation == null) {
            throw new IllegalStateException(String.format("Test '%s' missing @Artifacts annotation.",
                    description.getDisplayName()));
        }
        artifacts = annotation.value();

        return super.apply(base, description);
    }
}