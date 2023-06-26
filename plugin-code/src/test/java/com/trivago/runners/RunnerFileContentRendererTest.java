package com.trivago.runners;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.files.FileSystemManager;
import com.trivago.logging.CucableLogger;
import com.trivago.properties.PropertyManager;
import com.trivago.vo.FeatureRunner;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RunnerFileContentRendererTest {
    private FileSystemManager fileSystemManager;
    private RunnerFileContentRenderer runnerFileContentRenderer;
    private PropertyManager propertyManager;

    @Before
    public void setup() {
        fileSystemManager = mock(FileSystemManager.class);
        propertyManager = mock(PropertyManager.class);
        CucableLogger logger = mock(CucableLogger.class);
        runnerFileContentRenderer = new RunnerFileContentRenderer(fileSystemManager, propertyManager, logger);
    }

    @Test
    public void getRenderedFeatureFileContentFromTextFileTest() throws Exception {
        String template = "package parallel;\n" +
                          "\n" +
                          "import cucumber.api.CucumberOptions;\n" +
                          "\n" +
                          "@CucumberOptions(\n" +
                          "    monochrome = false,\n" +
                          "    features = {\"classpath:parallel/features/[CUCABLE:FEATURE].feature\"},\n" +
                          "    plugin = {\"json:target/cucumber-report/[CUCABLE:RUNNER].json\"}\n" +
                          ")\n" +
                          "public class [CUCABLE:RUNNER] {\n" +
                          "}\n";

        when(fileSystemManager.readContentFromFile(anyString())).thenReturn(template);

        String expectedOutput = "package parallel;\n" +
                                "\n" +
                                "import cucumber.api.CucumberOptions;\n" +
                                "\n" +
                                "@CucumberOptions(\n" +
                                "    monochrome = false,\n" +
                                "    features = {\"classpath:parallel/features/featureFileName.feature\"},\n" +
                                "    plugin = {\"json:target/cucumber-report/RunnerClass.json\"}\n" +
                                ")\n" +
                                "public class RunnerClass {\n" +
                                "}\n" +
                                "\n" +
                                "\n" +
                                "// Generated by Cucable unknown from pathToTemplate\n";

        ArrayList<String> featureFileNames = new ArrayList<>();
        featureFileNames.add("featureFileName");

        FeatureRunner featureRunner = new FeatureRunner(
                "pathToTemplate", "RunnerClass", featureFileNames
        );

        String renderedRunnerFileContent = runnerFileContentRenderer.getRenderedRunnerFileContent(featureRunner);

        // In a windows system, replace line separator "\r\n" with "\n".
        renderedRunnerFileContent = renderedRunnerFileContent.replaceAll("\\r\\n", "\n");

        assertThat(renderedRunnerFileContent, is(expectedOutput));
    }

    @Test
    public void getRenderedFeatureFileContentReplaceBackslashInCommentTest() throws Exception {
        String template = "package parallel;\n" +
                          "\n" +
                          "import cucumber.api.CucumberOptions;\n" +
                          "\n" +
                          "@CucumberOptions(\n" +
                          "    monochrome = false,\n" +
                          "    features = {\"classpath:parallel/features/[CUCABLE:FEATURE].feature\"},\n" +
                          "    plugin = {\"json:target/cucumber-report/[CUCABLE:RUNNER].json\"}\n" +
                          ")\n" +
                          "public class [CUCABLE:RUNNER] {\n" +
                          "}\n";

        when(fileSystemManager.readContentFromFile(anyString())).thenReturn(template);

        String expectedOutput = "package parallel;\n" +
                                "\n" +
                                "import cucumber.api.CucumberOptions;\n" +
                                "\n" +
                                "@CucumberOptions(\n" +
                                "    monochrome = false,\n" +
                                "    features = {\"classpath:parallel/features/featureFileName.feature\"},\n" +
                                "    plugin = {\"json:target/cucumber-report/RunnerClass.json\"}\n" +
                                ")\n" +
                                "public class RunnerClass {\n" +
                                "}\n" +
                                "\n" +
                                "\n" +
                                "// Generated by Cucable unknown from c:/unknown/path\n";

        ArrayList<String> featureFileNames = new ArrayList<>();
        featureFileNames.add("featureFileName");

        FeatureRunner featureRunner = new FeatureRunner(
                "c:\\unknown\\path", "RunnerClass", featureFileNames
        );

        String renderedRunnerFileContent = runnerFileContentRenderer.getRenderedRunnerFileContent(featureRunner);

        // In a windows system, replace line separator "\r\n" with "\n".
        renderedRunnerFileContent = renderedRunnerFileContent.replaceAll("\\r\\n", "\n");

        assertThat(renderedRunnerFileContent, is(expectedOutput));
    }


    @Test
    public void getRenderedFeatureFileContentFromJavaFileTest() throws Exception {

        String template = "package parallel;\n" +
                          "\n" +
                          "package some.package;\n" +
                          "import cucumber.api.CucumberOptions;\n" +
                          "\n" +
                          "@CucumberOptions(\n" +
                          "    monochrome = false,\n" +
                          "    features = {\"classpath:parallel/features/[CUCABLE:FEATURE].feature\"},\n" +
                          "    plugin = {\"json:target/cucumber-report/[CUCABLE:RUNNER].json\"}\n" +
                          ")\n" +
                          "public class MyClass {\n" +
                          "}\n";

        when(fileSystemManager.readContentFromFile(anyString())).thenReturn(template);

        String expectedOutput = "\n" +
                                "\n" +
                                "\n" +
                                "import cucumber.api.CucumberOptions;\n" +
                                "\n" +
                                "@CucumberOptions(\n" +
                                "    monochrome = false,\n" +
                                "    features = {\"classpath:parallel/features/featureFileName.feature\"},\n" +
                                "    plugin = {\"json:target/cucumber-report/RunnerClass.json\"}\n" +
                                ")\n" +
                                "public class RunnerClass {\n" +
                                "}\n" +
                                "\n" +
                                "\n" +
                                "// Generated by Cucable unknown from MyClass.java\n";


        ArrayList<String> featureFileNames = new ArrayList<>();
        featureFileNames.add("featureFileName");

        FeatureRunner featureRunner = new FeatureRunner(
                "MyClass.java", "RunnerClass", featureFileNames
        );

        String renderedRunnerFileContent = runnerFileContentRenderer.getRenderedRunnerFileContent(featureRunner);

        // In a windows system, replace line separator "\r\n" with "\n".
        renderedRunnerFileContent = renderedRunnerFileContent.replaceAll("\\r\\n", "\n");

        assertThat(renderedRunnerFileContent, is(expectedOutput));
    }

    @Test
    public void multipleFeatureRunnerTest() throws Exception {

        String template = "package parallel;\n" +
                          "\n" +
                          "package some.package;\n" +
                          "import cucumber.api.CucumberOptions;\n" +
                          "\n" +
                          "@CucumberOptions(\n" +
                          "    monochrome = false,\n" +
                          "    features = {\"classpath:parallel/features/[CUCABLE:FEATURE].feature\"},\n" +
                          "    plugin = {\"json:target/cucumber-report/[CUCABLE:RUNNER].json\"}\n" +
                          ")\n" +
                          "public class MyClass {\n" +
                          "}\n";

        when(fileSystemManager.readContentFromFile(anyString())).thenReturn(template);

        String expectedOutput = "\n" +
                                "\n" +
                                "\n" +
                                "import cucumber.api.CucumberOptions;\n" +
                                "\n" +
                                "@CucumberOptions(\n" +
                                "    monochrome = false,\n" +
                                "    features = {\"classpath:parallel/features/featureFileName.feature\",\n" +
                                "\"classpath:parallel/features/featureFileName2.feature\"},\n" +
                                "    plugin = {\"json:target/cucumber-report/RunnerClass.json\"}\n" +
                                ")\n" +
                                "public class RunnerClass {\n" +
                                "}\n" +
                                "\n" +
                                "\n" +
                                "// Generated by Cucable unknown from MyClass.java\n";


        ArrayList<String> featureFileNames = new ArrayList<>();
        featureFileNames.add("featureFileName");
        featureFileNames.add("featureFileName2");

        FeatureRunner featureRunner = new FeatureRunner(
                "MyClass.java", "RunnerClass", featureFileNames
        );

        String renderedRunnerFileContent = runnerFileContentRenderer.getRenderedRunnerFileContent(featureRunner);

        // In a windows system, replace line separator "\r\n" with "\n".
        renderedRunnerFileContent = renderedRunnerFileContent.replaceAll("\\r\\n", "\n");

        assertThat(renderedRunnerFileContent, is(expectedOutput));
    }

    @Test(expected = CucablePluginException.class)
    public void deprecatedPlaceholderTest() throws Exception {
        String template = "package parallel;\n" +
                          "\n" +
                          "import cucumber.api.CucumberOptions;\n" +
                          "\n" +
                          "@CucumberOptions(\n" +
                          "    monochrome = false,\n" +
                          "    features = {\"classpath:parallel/features/[CUCABLE:FEATURE].feature\"},\n" +
                          "    plugin = {\"json:target/cucumber-report/[CUCABLE:RUNNER].json\"}\n" +
                          ")\n" +
                          "public class [FEATURE_FILE_NAME] {\n" +
                          "}\n";

        when(fileSystemManager.readContentFromFile(anyString())).thenReturn(template);

        ArrayList<String> featureFileNames = new ArrayList<>();
        featureFileNames.add("featureFileName");

        FeatureRunner featureRunner = new FeatureRunner(
                "MyClass.java", "RunnerClass", featureFileNames
        );
        runnerFileContentRenderer.getRenderedRunnerFileContent(featureRunner);
    }

    @Test
    public void customParametersTest() throws CucablePluginException {
        String template =
                "Template [CUCABLE:FEATURE] [CUCABLE:CUSTOM:test1]!\n[CUCABLE:CUSTOM:test2], [CUCABLE:CUSTOM:test1]...";
        when(fileSystemManager.readContentFromFile(anyString())).thenReturn(template);

        Map<String, String> customParameters = new HashMap<>();
        customParameters.put("test1", "testvalue1");
        customParameters.put("test2", "another value");

        ArrayList<String> featureFileNames = new ArrayList<>();
        featureFileNames.add("featureFileName");

        FeatureRunner featureRunner = new FeatureRunner(
                "pathToTemplate", "RunnerClass", featureFileNames
        );

        when(propertyManager.getCustomPlaceholders()).thenReturn(customParameters);
        String renderedRunnerFileContent = runnerFileContentRenderer.getRenderedRunnerFileContent(featureRunner);

        // In a windows system, replace line separator "\r\n" with "\n".
        renderedRunnerFileContent = renderedRunnerFileContent.replaceAll("\\r\\n", "\n");

        assertThat(renderedRunnerFileContent, is(
                "Template [CUCABLE:FEATURE] testvalue1!\n" +
                "another value, testvalue1...\n" +
                "\n" +
                "// Generated by Cucable unknown from pathToTemplate\n"));
    }

    @Test(expected = CucablePluginException.class)
    public void missingRequiredPlaceholderTest() throws Exception {
        String template = "No Placeholder included";

        when(fileSystemManager.readContentFromFile(anyString())).thenReturn(template);

        ArrayList<String> featureFileNames = new ArrayList<>();
        featureFileNames.add("featureFileName");

        FeatureRunner featureRunner = new FeatureRunner(
                "MyClass.java", "RunnerClass", featureFileNames
        );

        runnerFileContentRenderer.getRenderedRunnerFileContent(featureRunner);
    }
}
