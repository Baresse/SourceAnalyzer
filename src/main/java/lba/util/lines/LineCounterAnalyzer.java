package lba.util.lines;

import lba.util.AbstractAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LineCounterAnalyzer extends AbstractAnalyzer {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LineCounterAnalyzer.class);

    private long linesNumber = 0L;

    @Override
    public void analyseFile(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            linesNumber += reader.lines().count();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public long getLinesNumber() {
        return linesNumber;
    }
}
