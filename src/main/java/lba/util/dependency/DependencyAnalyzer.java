package lba.util.dependency;

import lba.util.AbstractAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyAnalyzer extends AbstractAnalyzer {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DependencyAnalyzer.class);

    /**
     * Dependency manager used to keep track of packages dependencies
     */
    private DependencyManager manager = new DependencyManager();


    /**
     * Extract from each class / interface or enum the package name and all the import statements
     *
     * @param file
     */
    @Override
    public void analyseFile(Path file) {
        try {
            // First we will read the file
            List<String> lines = Files.readAllLines(file, Charset.defaultCharset());

            SortedSet<String> dependencies = new TreeSet<String>();
            String packageName = null;

            boolean foundEntity = false;

            Pattern patternPackage = Pattern.compile("package [\\s]*((?:[a-z][a-z_0-9]*\\.)*(?:$[a-z_]|[\\w_])*)");
            Pattern patternImport = Pattern.compile("import [\\s]*((?:[a-z][a-z_0-9]*\\.)*)((?:$[A-Z_]|[\\w_])*)");
            Pattern patternEntity = Pattern.compile("class|enum|interface");
            Matcher matcher;

            // Then we have to find the class package
            for (String line : lines) {
                if (packageName == null) {
                    matcher = patternPackage.matcher(line);
                    if (matcher.find()) {
                        packageName = matcher.group(1);
                        LOG.debug("Package found :" + packageName);
                    }
                } else {
                    // Try to find import statements
                    matcher = patternImport.matcher(line);
                    if (matcher.find()) {
                        String importStmt = matcher.group(1) + matcher.group(2);
                        LOG.debug("Import found :" + importStmt);
                        dependencies.add(importStmt);
                    } else {
                        // Try to find class, interface or enum keyword
                        matcher = patternEntity.matcher(line);
                        if (matcher.find()) {
                            foundEntity = true;
                            if (dependencies.size() > 0) {
                                manager.addDependencies(packageName, dependencies);
                            }
                            break;
                        }
                    }
                }
            }

            if (!foundEntity) {
                // Something went wrong...
                throw new IOException("No class, interface or enum found in file :" + file);
            }

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Generate the Dot graph source of the project
     *
     * @param rootPackage
     * @return
     */
    public String toDot(String rootPackage) {

        Map<String, SortedSet<String>> depsNormalized = manager.getNormalizedDependencies(rootPackage);

        String dot = "digraph DepGraph { \n";

        for (String pkg : depsNormalized.keySet()) {
            for (String dep : depsNormalized.get(pkg)) {
                dot += "\t" + pkg + "->" + dep + "\n";
            }
            dot += "\n";
        }

        dot += "}";

        return dot;
    }



}
