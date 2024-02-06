package uk.ac.ox.oxfish.maximization;

import eva2.optimization.statistics.InterfaceTextListener;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class FileAndScreenWriter implements InterfaceTextListener, Closeable {

    private final FileWriter fileWriter;

    public FileAndScreenWriter(final Path outputFile) throws IOException {
        this.fileWriter = new FileWriter(outputFile.toFile());
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
    }

    @Override
    public void println(final String str) {
        print(str + "\n");
    }

    @Override
    public void print(final String str) {
        System.out.println(str);
        try {
            fileWriter.write(str);
            fileWriter.flush();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
