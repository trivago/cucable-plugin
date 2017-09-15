package com.trivago.rta.files;

import com.trivago.rta.exceptions.filesystem.FileCreationException;
import org.junit.Test;

public class FileWriterTest {

    @Test(expected = FileCreationException.class)
    public void emptyFileTest() throws Exception {
        FileWriter fileWriter = new FileWriter();
        fileWriter.writeContentToFile(null, "");
    }
}
