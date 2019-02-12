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
import com.trivago.exceptions.filesystem.FileDeletionException;
import com.trivago.exceptions.filesystem.PathCreationException;
import com.trivago.properties.PropertyManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

@Singleton
public class FileSystemManager {

    private final PropertyManager propertyManager;

    @Inject
    public FileSystemManager(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    /**
     * Create generated feature and runner dirs if they don't exist and clear their contents.
     */
    public void prepareGeneratedFeatureAndRunnerDirectories() throws CucablePluginException {
        createDirIfNotExists(propertyManager.getGeneratedFeatureDirectory());
        removeFilesFromPath(propertyManager.getGeneratedFeatureDirectory(), "feature");

        createDirIfNotExists(propertyManager.getGeneratedRunnerDirectory());
        removeFilesFromPath(propertyManager.getGeneratedRunnerDirectory(), "java");
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
}
