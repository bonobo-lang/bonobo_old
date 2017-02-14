package thosakwe.bonobo.tool;

import org.apache.commons.cli.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.analysis.BonoboLanguageServer;
import thosakwe.bonobo.analysis.ErrorChecker;
import thosakwe.bonobo.analysis.StaticAnalyzer;
import thosakwe.bonobo.compiler.BonoboCompiler;
import thosakwe.bonobo.compiler.BonoboToJvmCompiler;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CompilerFrontend {
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

            if (commandLine.hasOption("analyze")) {
                languageServerMain(commandLine);
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
            BonoboCompiler compiler = new BonoboToJvmCompiler(commandLine.hasOption("verbose"));

            // Analyze and check for errors
            try {
                StaticAnalyzer analyzer = new StaticAnalyzer(compiler.isDebug());
                BonoboLibrary library = analyzer.analyzeCompilationUnit(ast);
                ErrorChecker errorChecker = new ErrorChecker(analyzer);
                List<BonoboException> errors = errorChecker.visitLibrary(library);

                if (!errors.isEmpty()) {
                    System.err.printf("%d compiler error(s):%n%n", errors.size());

                    for (BonoboException error : errors) {
                        System.err.printf(String.format(
                                "line %d, column %d: %s%n",
                                error.getSource().start.getLine(),
                                error.getSource().start.getCharPositionInLine(),
                                error.getMessage()
                        ));
                    }
                } else {
                    compiler.compile(library, out);
                }

            } catch (BonoboException exc) {
                System.err.println("Static analysis error: " + exc.getMessage());
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


    private static void languageServerMain(CommandLine commandLine) throws IOException {
        if (commandLine.hasOption("write-stdout")) {
            try {
                BonoboLanguageServer server = new BonoboLanguageServer(commandLine.hasOption("verbose"));
                Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
                server.connect(launcher.getRemoteProxy());
                launcher.startListening().get();
            } catch (ExecutionException exc) {
                System.err.println(exc.getCause().getMessage());
                exc.getCause().printStackTrace();
                return;
            } catch (InterruptedException exc) {
                System.err.println(exc.getMessage());
                exc.printStackTrace();
                return;
            }
        }

        ServerSocket serverSocket = new ServerSocket();
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), Integer.parseInt(commandLine.getOptionValue("port", "2359")));
        serverSocket.bind(address);
        System.out.printf("Bonobo language server now listening at %s:%d%n", address.getAddress().getCanonicalHostName(), address.getPort());

        while (true) {
            Socket client = serverSocket.accept();
            System.out.printf("New client connection: %s:%d%n", client.getInetAddress().getCanonicalHostName(), client.getPort());
            BonoboLanguageServer server = new BonoboLanguageServer(commandLine.hasOption("verbose"));
            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, client.getInputStream(), client.getOutputStream());
            server.connect(launcher.getRemoteProxy());
            launcher.startListening();
        }
    }

    private static Options makeCliOptions() {
        return new Options()
                .addOption(Option.builder().longOpt("analysis-server").desc("Start the analysis server.").build())
                .addOption(Option.builder().longOpt("analyze").desc("Statically analyzes the source, and prints JSON as output.").build())
                .addOption("a", "analyze", false, "Run the language server.")
                .addOption("debug", "verbose", false, "Enable verbose debug output.")
                .addOption("h", "help", false, "print this help information.")
                .addOption("o", "out", true, "The output file to be generated.")
                .addOption("p", "port", true, "Specify a port for the analysis server. Default: 2359")
                .addOption("stdout", "write-stdout", false, "Prints the resulting JVM bytecode to stdout.")
                .addOption("v", "version", false, "Prints the program version.");
    }

    private static void printUsage() {
        new HelpFormatter().printHelp("bonobo [options...] <filename>", makeCliOptions());
    }
}
