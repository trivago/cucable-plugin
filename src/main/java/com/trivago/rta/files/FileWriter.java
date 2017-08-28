package com.trivago.rta.files;

import com.trivago.rta.exceptions.filesystem.FileCreationException;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.PrintStream;

@Singleton
public class FileWriter {
    public void writeContentToFile(String content, String filePath) throws FileCreationException {
        try (PrintStream ps = new PrintStream(filePath)) {
            ps.println(content);
        } catch (IOException e) {
            throw new FileCreationException(filePath);
        }
    }
}
