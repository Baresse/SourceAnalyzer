package lba.util.service;

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

public class RestletServiceAnalyzer extends AbstractAnalyzer {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(RestletServiceAnalyzer.class);

    /**
     * Services found by the analyzer
     */
    private SortedMap<String, SortedSet<String>> servicesRepository = new TreeMap<String, SortedSet<String>>();


    @Override
    public void analyseFile(Path file) {
        try {
            // First we will read the file
            List<String> lines = Files.readAllLines(file, Charset.defaultCharset());

            SortedSet<String> services = new TreeSet<String>();

            boolean foundEntity = false;
            String packageName = null;

            Pattern patternPackage = Pattern.compile("package [\\s]*((?:[a-z][a-z_0-9]*\\.)*(?:$[a-z_]|[\\w_])*)");
            Pattern patternImport = Pattern.compile("import [\\s]*org\\.restlet\\.resource\\.(Get|Post|Put|Delete)");
            Pattern patternEntity = Pattern.compile("public(?:\\s)*(?:final)*(?:abstract)*(?:\\s)*(?:class|enum|interface) ((?:\\w)*)");
            Matcher matcher;

            // Then we have to find the class package
            for (String line : lines) {
                matcher = patternPackage.matcher(line);
                if (matcher.find()) {
                    packageName = matcher.group(1);
                    LOG.debug("Package found :" + packageName);
                } else {
                    // Try to find import statements
                    matcher = patternImport.matcher(line);
                    if (matcher.find()) {
                        String verb = matcher.group(1);
                        LOG.debug("Service found :" + verb);
                        services.add(verb);
                    } else {
                        // Try to find class, interface or enum keyword
                        matcher = patternEntity.matcher(line);
                        if (matcher.find()) {
                            foundEntity = true;
                            String entity = matcher.group(1);
                            if (services.size() > 0) {
                                servicesRepository.put(packageName + "." + entity, services);
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

    public SortedMap<String, SortedSet<String>> getServicesRepository() {
        return servicesRepository;
    }

}
