package lba.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Class which extends SimpleFileVisitor<Path> intended to be used with Files.walkFileTree
 */
public class JavaSourceFileVisitor<T extends AbstractAnalyzer>  extends SimpleFileVisitor<Path> {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(JavaSourceFileVisitor.class);

    /**
     * Specialized java file analyzer
     */
    private T analyser;

    /**
     * Constructor
     *
     * @param analyser Specialized java file analyzer
     */
    public JavaSourceFileVisitor(T analyser) {
        this.analyser = analyser;
    }

    /**
     * Invoked for a file in a directory.
     *
     * <p> Unless overridden, this method returns {@link FileVisitResult#CONTINUE
     * CONTINUE}.
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
        logFileInfo(file, attr);
        String filename = file.getFileName().toString();
        if (filename.endsWith(".java") && !"package-info.java".equals(filename)) {
            analyser.analyseFile(file);
        }

        return FileVisitResult.CONTINUE;
    }

    /**
     * Invoked for a directory after entries in the directory, and all of their
     * descendants, have been visited.
     *
     * <p> Unless overridden, this method returns {@link FileVisitResult#CONTINUE
     * CONTINUE} if the directory iteration completes without an I/O exception;
     * otherwise this method re-throws the I/O exception that caused the iteration
     * of the directory to terminate prematurely.
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException e) {
        LOG.info("Directory: {}", dir);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Invoked for a file that could not be visited.
     *
     * <p> Unless overridden, this method re-throws the I/O exception that prevented
     * the file from being visited.
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        LOG.error(exc.getMessage(), exc);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Log file information
     */
    private void logFileInfo(Path file, BasicFileAttributes attr) {
        if (attr.isSymbolicLink()) {
            LOG.info("Symbolic link: {} ({} bytes)", file, attr.size());
        } else if (attr.isRegularFile()) {
            LOG.info("Regular file: {} ({} bytes)", file, attr.size());
        } else {
            LOG.info("Other: {} ({} bytes)", file, attr.size());
        }
    }

    /**
     * @return Specialized Java source file analyzer
     */
    public T getAnalyser() { return analyser; }
}

