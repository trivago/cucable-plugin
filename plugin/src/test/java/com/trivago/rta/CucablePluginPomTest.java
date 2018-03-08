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

package com.trivago.rta;

import com.trivago.rta.exceptions.properties.WrongOrMissingPropertyException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

import static org.codehaus.plexus.PlexusTestCase.getBasedir;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class CucablePluginPomTest {
    private static final String TEST_POM_PATH = "src/test/resources/pom/";
    private static final String VALID_POM = TEST_POM_PATH + "valid_pom.xml";
    private static final String VALID_POM_2_RUNS = TEST_POM_PATH + "valid_pom_2_runs.xml";
    private static final String MISSING_SOURCE_RUNNER_TEMPLATE_POM =
            TEST_POM_PATH + "missing_source_runner_template_file_pom.xml";
    private static final String MISSING_SOURCE_FEATURE_DIRECTORY_POM =
            TEST_POM_PATH + "missing_source_features_pom.xml";
    private static final String MISSING_GENERATED_FEATURE_DIRECTORY_POM =
            TEST_POM_PATH + "missing_generated_feature_directory_pom.xml";
    private static final String MISSING_GENERATED_RUNNER_DIRECTORY_POM =
            TEST_POM_PATH + "missing_generated_runner_directory_pom.xml";

    private static final String SOURCE_RUNNER_TEMPLATE_FILE = "sourceRunnerTemplateFile";
    private static final String SOURCE_FEATURES = "sourceFeatures";
    private static final String GENERATED_FEATURE_DIRECTORY = "generatedFeatureDirectory";
    private static final String GENERATED_RUNNER_DIRECTORY = "generatedRunnerDirectory";
    private static final String NUMBER_OF_TEST_RUNS = "numberOfTestRuns";

    private static final String MAVEN_GOAL = "parallel";

    @Rule
    public MojoRule mojoRule = new MojoRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testMojoInstantiation() throws Exception {
        CucablePlugin mojo = createMojoFromPomFile(VALID_POM);
        assertThat(mojo, is(notNullValue()));
    }

    @Test
    public void validConfigurationTest() throws Exception {
        CucablePlugin mojo = createMojoFromPomFile(VALID_POM);

        String sourceRunnerTemplateFile =
                (String) mojoRule.getVariableValueFromObject(mojo, SOURCE_RUNNER_TEMPLATE_FILE);
        assertThat(sourceRunnerTemplateFile, is("src/test/resources/parallel/cucable_parallel_runner.template"));

        String sourceFeatures =
                (String) mojoRule.getVariableValueFromObject(mojo, SOURCE_FEATURES);
        assertThat(sourceFeatures, is("src/test/resources/features"));

        String generatedFeatureDirectory =
                (String) mojoRule.getVariableValueFromObject(mojo, GENERATED_FEATURE_DIRECTORY);
        assertThat(generatedFeatureDirectory, is("target/parallel/features"));

        String generatedRunnerDirectory =
                (String) mojoRule.getVariableValueFromObject(mojo, GENERATED_RUNNER_DIRECTORY);
        assertThat(generatedRunnerDirectory, is("target/parallel/runners"));

        int numberOfTestRuns =
                (int) mojoRule.getVariableValueFromObject(mojo, NUMBER_OF_TEST_RUNS);
        assertThat(numberOfTestRuns, is(1));
    }

    @Test
    public void numberOfTestRunsOverrideTest() throws Exception {
        CucablePlugin mojo = createMojoFromPomFile(VALID_POM_2_RUNS);
        int numberOfTestRuns =
                (int) mojoRule.getVariableValueFromObject(mojo, NUMBER_OF_TEST_RUNS);
        assertThat(numberOfTestRuns, is(2));
    }

    @Test
    public void missingGeneratedFeatureDirectoryTest() throws Exception {
        expectedException.expect(WrongOrMissingPropertyException.class);
        CucablePlugin mojo = createMojoFromPomFile(MISSING_GENERATED_FEATURE_DIRECTORY_POM);
        mojo.execute();
    }

    @Test
    public void missingGeneratedRunnerDirectoryTest() throws Exception {
        expectedException.expect(WrongOrMissingPropertyException.class);
        CucablePlugin mojo = createMojoFromPomFile(MISSING_GENERATED_RUNNER_DIRECTORY_POM);
        mojo.execute();
    }

    @Test
    public void missingSourceFeaturesTest() throws Exception {
        expectedException.expect(WrongOrMissingPropertyException.class);
        CucablePlugin mojo = createMojoFromPomFile(MISSING_SOURCE_FEATURE_DIRECTORY_POM);
        mojo.execute();
    }

    @Test
    public void missingSourceRunnerTemplateFileTest() throws Exception {
        expectedException.expect(WrongOrMissingPropertyException.class);
        CucablePlugin mojo = createMojoFromPomFile(MISSING_SOURCE_RUNNER_TEMPLATE_POM);
        mojo.execute();
    }

    private CucablePlugin createMojoFromPomFile(String pomLocation) throws Exception {
        File testPom = new File(getBasedir(), pomLocation);
        assertThat(testPom, is(notNullValue()));
        assertThat(testPom.exists(), is(true));
        return (CucablePlugin) mojoRule.lookupMojo(MAVEN_GOAL, testPom);
    }
}
