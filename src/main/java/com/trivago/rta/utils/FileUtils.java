/*
 * Copyright 2017 trivago GmbH
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

package com.trivago.rta.utils;

import com.trivago.rta.exceptions.CucablePluginException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * General file utilities.
 */
public final class FileUtils {
    /**
     * Private constructor since this is a class
     * providing static helper methods.
     */
    private FileUtils() {
    }

    /**
     * Returns a list of feature file paths located
     * in the given directory or its subdirectories.
     *
     * @param sourceFeatureDirectory The directory
     *                               where the source feature files are stored.
     * @return a list of feature file paths.
     * @throws CucablePluginException see {@link CucablePluginException}
     */
    public static List<Path> getFeatureFilePaths(
            final String sourceFeatureDirectory
    ) throws CucablePluginException {
        List<Path> featureFilesLocations;
        try {
            featureFilesLocations =
                    Files
                            .walk(Paths.get(sourceFeatureDirectory))
                            .filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".feature"))
                            .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CucablePluginException(
                    "Unable to traverse feature files in "
                            + sourceFeatureDirectory + ": " + e.getMessage());
        }
        return featureFilesLocations;
    }

    /**
     * Removes specific files from the given path.
     *
     * @param path    The path to clear.
     * @param postfix The file postfixes to consider.
     */
    public static void removeFilesFromPath(
            final String path, final String postfix
    ) {
        File basePath = new File(path);
        File[] files = basePath.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith("." + postfix)) {
                    file.delete();
                }
            }
        }
    }

    /**
     * Creates a directory if it does not exists
     * @param dirName Name of directory.
     * @return true if directory exists or was created.
     */
    public static boolean createDir(String dirName){
        File directory = new File(dirName);
        return directory.exists() || directory.mkdir();
    }
}
