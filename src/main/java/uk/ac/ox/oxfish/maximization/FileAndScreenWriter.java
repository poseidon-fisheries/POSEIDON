package uk.ac.ox.oxfish.maximization;

import eva2.optimization.statistics.InterfaceTextListener;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

class FileAndScreenWriter implements InterfaceTextListener, Closeable {

    private final FileWriter fileWriter;

    FileAndScreenWriter(Path outputFile) throws IOException {
        this.fileWriter = new FileWriter(outputFile.toFile());
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
    }

    @Override
    public void print(String str) {
        System.out.println(str);
        try {
            fileWriter.write(str);
            fileWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void println(String str) {
        print(str + "\n");
    }

}
