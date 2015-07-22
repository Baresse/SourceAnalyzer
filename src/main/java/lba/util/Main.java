package lba.util;

import lba.util.dependency.DependencyAnalyzer;
import lba.util.lines.LineCounterAnalyzer;
import lba.util.service.RestletServiceAnalyzer;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;
import java.util.SortedSet;

public class Main {

    // Helpformatter used to present the CLI usage help message
    protected HelpFormatter formatter = new HelpFormatter();

    // Command line parser
    protected CommandLineParser parser = new DefaultParser();

    // Initialisation block
    {
        formatter.setWidth(200);
    }


    public void run(String args[]) throws IOException {

        Options options = new Options();

        // Build specific options for the top level options
        OptionGroup group = new OptionGroup();

        group.addOption(
                Option.builder("d")
                        .longOpt("dependencies")
                        .desc("Analyze packages dependencies")
                        .build()
        );

        group.addOption(
                Option.builder("r")
                        .longOpt("restletServices")
                        .desc("Analyze Restlet services")
                        .build()
        );

        group.addOption(
                Option.builder("l")
                        .longOpt("linesCounter")
                        .desc("Count the number of lines of code")
                        .build()
        );
        options.addOptionGroup(group);

        options.addOption(
                Option.builder("s")
                        .longOpt("src")
                        .desc("Root source directory to analyze")
                        .hasArg()
                        .argName("rootDir")
                        .required()
                        .build()
        );

        options.addOption(
                Option.builder("p")
                        .longOpt("pkg")
                        .desc("Root package used to filter analyzed Java class")
                        .hasArg()
                        .argName("package")
                        .required()
                        .build()
        );

        options.addOption(
                Option.builder("o")
                        .longOpt("output")
                        .desc("Output file path where the result will be stored")
                        .hasArg()
                        .argName("filePath")
                        .required()
                        .build()
        );

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            Path rootPath = Paths.get(line.getOptionValue("src"));
            String rootPackage = line.getOptionValue("pkg");

            if (line.hasOption("dependencies")) {
                // Create the specialized visitor
                JavaSourceFileVisitor<DependencyAnalyzer> javaSourceFileVisitor =
                        new JavaSourceFileVisitor<>(new DependencyAnalyzer());

                // Perform the walk file tree...
                Files.walkFileTree(rootPath, javaSourceFileVisitor);

                // Save the result into the output file
                DependencyAnalyzer analyzer = javaSourceFileVisitor.getAnalyser();
                Files.write(Paths.get(line.getOptionValue("output")), analyzer.toDot(rootPackage).getBytes());
            }

            if (line.hasOption("restletServices")) {
                // Create the specialized visitor
                JavaSourceFileVisitor<RestletServiceAnalyzer> javaSourceFileVisitor =
                        new JavaSourceFileVisitor<>(new RestletServiceAnalyzer());

                // Perform the walk file tree...
                Files.walkFileTree(rootPath, javaSourceFileVisitor);

                // Save the result into the output file
                RestletServiceAnalyzer analyzer = javaSourceFileVisitor.getAnalyser();

                SortedMap<String, SortedSet<String>> repo = analyzer.getServicesRepository();
                String report = "";
                int entityNumber = repo.keySet().size();
                int serviceNumber = 0;

                for (String entity : repo.keySet()) {
                    SortedSet<String> services = repo.get(entity);
                    serviceNumber += services.size();
                    report += entity + "=";
                    for (String verb : services) {
                        report += verb + " ";
                    }
                    report += "\n";
                }

                report += "\n Total number of entities :" + entityNumber + "\n";
                report += "\n Total number of services :" + serviceNumber + "\n";

                Files.write(Paths.get(line.getOptionValue("output")), report.getBytes());
            }

            if (line.hasOption("linesCounter")) {
                // Create the specialized visitor
                JavaSourceFileVisitor<LineCounterAnalyzer> javaSourceFileVisitor =
                        new JavaSourceFileVisitor<>(new LineCounterAnalyzer());

                // Perform the walk file tree...
                Files.walkFileTree(rootPath, javaSourceFileVisitor);

                // Print the total lines of code
                String report = "Total lines of code :" + javaSourceFileVisitor.getAnalyser().getLinesNumber();
                System.out.println(report);

                Files.write(Paths.get(line.getOptionValue("output")), report.getBytes());
            }

        } catch (ParseException e) {
            // automatically generate the help statement
            formatter.printHelp("Sources Analyzer", options);
        }
    }


    /**
     * Main method
     */
    public static void main(String args[]) throws IOException {
        new Main().run(args);
    }
}
