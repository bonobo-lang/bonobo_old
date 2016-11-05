package thosakwe.strongly_typed.cli;

import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import thosakwe.strongly_typed.Json;
import thosakwe.strongly_typed.StronglyTyped;
import thosakwe.strongly_typed.analysis.StaticAnalyzer;
import thosakwe.strongly_typed.compiler.CCompiler;
import thosakwe.strongly_typed.grammar.StronglyTypedParser;
import thosakwe.strongly_typed.grammar.SyntaxErrorListener;
import thosakwe.strongly_typed.lang.errors.CompilerError;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            final Options cliOptions = makeCliOptions();
            final CommandLine commandLine = new DefaultParser().parse(cliOptions, args);

            if (commandLine.hasOption("help")) {
                printUsage();
                return;
            }

            if (commandLine.hasOption("version")) {
                System.out.println(StronglyTyped.VERSION);
                return;
            }

            if (commandLine.hasOption("analysis-server")) {
                return;
            }

            if (commandLine.getArgList().isEmpty()) {
                System.err.println("fatal error: no input file");
                return;
            }

            final String filename = new File(commandLine.getArgs()[0]).getAbsolutePath();
            final Pair<StronglyTypedParser, SyntaxErrorListener> pair = StronglyTyped.parseFile(filename);
            final StronglyTypedParser.CompilationUnitContext ast = pair.a.compilationUnit();
            final StaticAnalyzer analyzer = new StaticAnalyzer(pair.b, filename);
            analyzer.visitCompilationUnit(ast);

            if (commandLine.hasOption("analyze")) {
                final Map<String, Object> result = new HashMap<>();
                result.put("errors", analyzer.getErrors());
                result.put("version", StronglyTyped.VERSION);
                System.out.println(Json.stringify(result));
            } else if (!analyzer.getErrors().isEmpty()) {
                for (CompilerError error : analyzer.getErrors()) {
                    System.err.printf("%s: %s (%s:%d:%d)%n", error.getType(), error.getMessage(), filename, error.getLine(), error.getColumn());
                }

                System.exit(1);
            }

            for (CompilerError warning : analyzer.getWarnings()) {
                System.out.printf("%s: %s (%s:%d:%d)%n", warning.getType(), warning.getMessage(), filename, warning.getLine(), warning.getColumn());
            }

            String outputFilename;

            if (commandLine.hasOption("out"))
                outputFilename = commandLine.getOptionValue("out");
            else {
                final int lastDot = filename.lastIndexOf('.');

                if (lastDot == -1)
                    outputFilename = "out.c";
                else {
                    String baseName = filename.substring(0, lastDot);
                    outputFilename = String.format("%s.c", baseName);
                }
            }

            final PrintStream out = commandLine.hasOption("write-stdout") ? System.out : new PrintStream(outputFilename);
            final CCompiler compiler = new CCompiler(filename, commandLine.hasOption("verbose"));

            try {
                final String output = compiler.compile(ast);
                out.print(output.trim());
                out.close();
                System.out.println(output);
            } catch (CompilerError exc) {
                compiler.getErrors().add(exc);
            }

            if (!compiler.getErrors().isEmpty()) {
                for (CompilerError error : compiler.getErrors()) {
                    System.err.printf("%s: %s (%s:%d:%d)%n", error.getType(), error.getMessage(), filename, error.getLine(), error.getColumn());
                }

                System.exit(1);
            }
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
                .addOption("stdout", "write-stdout", false, "Prints the resulting C code stdout.")
                .addOption("v", "version", false, "Prints the program version.");
    }

    private static void printUsage() {
        new HelpFormatter().printHelp("strongly-typed [options...] <filename>", makeCliOptions());
    }
}
