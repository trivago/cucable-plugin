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

import com.trivago.exceptions.filesystem.FileCreationException;
import com.trivago.exceptions.filesystem.MissingFileException;
import org.codehaus.plexus.util.FileUtils;

import javax.inject.Singleton;
import java.io.IOException;

/**
 * This class manages reading from and writing to files.
 */
@Singleton
public class FileIO {

    /**
     * Writes string content to a file.
     *
     * @param content  the string content to be written.
     * @param filePath the complete path to the target file.
     * @throws FileCreationException a {@link FileCreationException} in case the file cannot be created.
     */
    public void writeContentToFile(String content, String filePath) throws FileCreationException {
        try {
            FileUtils.fileWrite(filePath, "UTF-8", content);
        } catch (IOException e) {
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
