/*
 * Copyright 2017 trivago N.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trivago.vo;

import java.util.List;

/**
 * Represents a test runner for a single or multiple features.
 */
public final class FeatureRunner {

    // The path to the runner file template.
    private final String runnerTemplatePath;

    // The name of the generated runner class.
    private final String runnerClassName;

    // The name of the feature file this runner belongs to.
    private final List<String> featureFileNames;

    /**
     * Constructor for a single or multiple feature runner.
     *
     * @param runnerTemplatePath The path to the runner template.
     * @param runnerClassName    The name of the generated runner class.
     * @param featureFileNames   The name string of the feature file(s) for this runner.
     */
    public FeatureRunner(
            final String runnerTemplatePath,
            final String runnerClassName, final List<String> featureFileNames) {

        this.runnerTemplatePath = runnerTemplatePath;
        this.runnerClassName = runnerClassName;
        this.featureFileNames = featureFileNames;
    }

    public String getRunnerTemplatePath() {
        return runnerTemplatePath;
    }

    public List<String> getFeatureFileNames() {
        return featureFileNames;
    }

    public String getRunnerClassName() {
        return runnerClassName;
    }
}
