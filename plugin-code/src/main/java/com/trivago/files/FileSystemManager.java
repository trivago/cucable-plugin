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

package com.trivago.files;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.exceptions.filesystem.FileCreationException;
import com.trivago.exceptions.filesystem.FileDeletionException;
import com.trivago.exceptions.filesystem.MissingFileException;
import com.trivago.exceptions.filesystem.PathCreationException;
import com.trivago.vo.CucableFeature;
import org.codehaus.plexus.util.FileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class FileSystemManager {

    public static final String FEATURE_FILE_EXTENSION = "feature";
    public static final String TEXT_FILE_EXTENSION = "txt";

    @Inject
    public FileSystemManager() {
    }

    /**
     * Returns a list of feature file paths located in the specified source feature directory.
     *
     * @return a list of feature file paths.
     * @throws CucablePluginException see {@link CucablePluginException}
     */
    public List<Path> getPathsFromCucableFeature(final CucableFeature cucableFeature) throws CucablePluginException {

        if (cucableFeature == null) {
            return Collections.emptyList();
        }

        String sourceFeatures = cucableFeature.getName().
                replace("file://", "");

        File sourceFeaturesFile = new File(sourceFeatures);

        if (sourceFeatures.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Check if the property value is a single file or a directory
        if (sourceFeaturesFile.isFile() && sourceFeatures.endsWith(FEATURE_FILE_EXTENSION)) {
            return Collections.singletonList(Paths.get(sourceFeatures));
        }

        if (sourceFeaturesFile.isDirectory()) {
            return getFilesWithExtension(sourceFeatures, FEATURE_FILE_EXTENSION);
        }

        throw new CucablePluginException(
                sourceFeatures + " is not a feature file or a directory."
        );
    }

    /**
     * Returns a list of feature files in the given directory.
     *
     * @param sourceFeatureDirectory The source directory to scan for feature files.
     * @param extension              The file extension to look for.
     * @return A list of feature files in the given directory.
     * @throws CucablePluginException see {@link CucablePluginException}.
     */
    public List<Path> getFilesWithExtension(final String sourceFeatureDirectory, final String extension) throws
            CucablePluginException {
        try (Stream<Path> paths = Files.walk(Paths.get(sourceFeatureDirectory))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("." + extension))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CucablePluginException(
                    "Unable to traverse " + extension + " files in " + sourceFeatureDirectory + ": " + e.getMessage());
        }
    }

    /**
     * Create generated feature and runner dirs if they don't exist and clear their contents.
     */
    public void prepareGeneratedFeatureAndRunnerDirectories(final String runnerDir, final String featureDir) throws CucablePluginException {
        createDirIfNotExists(featureDir);
        removeFilesFromPath(featureDir, "feature");

        if (runnerDir != null && !runnerDir.isEmpty()) {
            createDirIfNotExists(runnerDir);
            removeFilesFromPath(runnerDir, "java");
        }
    }

    /**
     * Creates a directory if it does not exists.
     *
     * @param dirName Name of directory.
     */
    private void createDirIfNotExists(final String dirName) throws PathCreationException {
        File directory = new File(dirName);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new PathCreationException(dirName);
        }
    }

    /**
     * Removes files with the specified extension from the given path.
     *
     * @param path          The path to clear.
     * @param fileExtension The file extension to consider.
     */
    private void removeFilesFromPath(final String path, final String fileExtension)
            throws FileDeletionException {

        File basePath = new File(path);
        File[] files = basePath.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith("." + fileExtension) && !file.delete()) {
                    throw new FileDeletionException(file.getName());
                }
            }
        }
    }

    /**
     * Writes string content to a file.
     *
     * @param content  the string content to be written.
     * @param filePath the complete path to the target file.
     * @throws FileCreationException a {@link FileCreationException} in case the file cannot be created.
     */
    public void writeContentToFile(String content, String filePath) throws FileCreationException {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            throw new FileCreationException(filePath);
        }
    }

    /**
     * Reads string content from a file.
     *
     * @param filePath the complete path to the source file.
     * @return the file contents as a string.
     * @throws MissingFileException a {@link MissingFileException} in case the file does not exist.
     */
    public String readContentFromFile(String filePath) throws MissingFileException {
        try {
            return FileUtils.fileRead(filePath, "UTF-8");
        } catch (IOException e) {
            throw new MissingFileException(filePath);
        }
    }
}
