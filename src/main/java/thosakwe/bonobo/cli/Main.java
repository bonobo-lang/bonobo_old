package thosakwe.bonobo.cli;

import org.apache.commons.cli.*;
import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.grammar.BonoboParser;

import java.io.File;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {
        try {
            Options cliOptions = makeCliOptions();
            CommandLine commandLine = new DefaultParser().parse(cliOptions, args);

            if (commandLine.hasOption("help")) {
                printUsage();
                return;
            }

            if (commandLine.hasOption("version")) {
                System.out.println(Bonobo.VERSION);
                return;
            }

            if (commandLine.hasOption("analysis-server")) {
                return;
            }

            if (commandLine.getArgList().isEmpty()) {
                System.err.println("fatal error: no input file");
                return;
            }

            String filename = new File(commandLine.getArgs()[0]).getAbsolutePath();
            BonoboParser parser = Bonobo.parseFile(filename);
            BonoboParser.CompilationUnitContext ast = parser.compilationUnit();

            String outputFilename;

            if (commandLine.hasOption("out"))
                outputFilename = commandLine.getOptionValue("out");
            else {
                int lastDot = filename.lastIndexOf('.');

                if (lastDot == -1)
                    outputFilename = "out.class";
                else {
                    String baseName = filename.substring(0, lastDot);
                    outputFilename = String.format("%s.class", baseName);
                }
            }

            PrintStream out = commandLine.hasOption("write-stdout") ? System.out : new PrintStream(outputFilename);
            out.println("TODO: Compilation");
        } catch (ParseException exc) {
            printUsage();
            System.exit(1);
        } catch (Exception exc) {
            System.err.println(exc.getMessage());
            exc.printStackTrace();
            System.exit(1);
        }
    }

    private static Options makeCliOptions() {
        return new Options()
                .addOption(Option.builder().longOpt("analysis-server").desc("Start the analysis server.").build())
                .addOption(Option.builder().longOpt("analyze").desc("Statically analyzes the source, and prints JSON as output.").build())
                .addOption("debug", "verbose", false, "Enable verbose debug output.")
                .addOption("h", "help", false, "print this help information.")
                .addOption("p", "port", true, "Specify a port for the analysis server. Default: 2359")
                .addOption("stdout", "write-stdout", false, "Prints the resulting JVM bytecode to stdout.")
                .addOption("v", "version", false, "Prints the program version.");
    }

    private static void printUsage() {
        new HelpFormatter().printHelp("bonobo [options...] <filename>", makeCliOptions());
    }
}
