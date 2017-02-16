package thosakwe.bonobo.analysis;

import org.apache.commons.cli.*;
import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.objects.BonoboFunctionParameter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BonoboDumper {
    public static void main(String[] args) throws IOException, BonoboException {
        Options cliOptions = dumpOptions();
        CommandLine commandLine = null;

        try {
            commandLine = new DefaultParser().parse(cliOptions, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp("dumper [options...] <filename>", cliOptions);
            System.err.println("fatal error: no input file");
            System.exit(1);
            return;
        }

        String[] rest = commandLine.getArgs();

        if (rest.length < 1) {
            System.err.println("fatal error: no input file");
            System.exit(1);
            return;
        }

        BonoboParser parser = Bonobo.parseFile(args[0]);
        BonoboParser.CompilationUnitContext ast = parser.compilationUnit();
        StaticAnalyzer analyzer = new StaticAnalyzer(commandLine.hasOption("verbose"), ast);
        BonoboLibrary library = analyzer.analyzeCompilationUnit(ast);
        dump(library);
        ErrorChecker checker = new ErrorChecker(analyzer);
        List<BonoboException> errors = checker.visitLibrary(library);

        if (errors.isEmpty())
            System.out.println("Library contains 0 errors. Nice coding!");
        else {
            System.err.printf("Library contains %d error(s):%n%n", errors.size());

            for (BonoboException error : errors) {
                System.err.printf(String.format(
                        "line %d, column %d: %s%n",
                        error.getSource().start.getLine(),
                        error.getSource().start.getCharPositionInLine(),
                        error.getMessage()
                ));
            }

            System.exit(1);
        }
    }

    private static Options dumpOptions() {
        return new Options()
                .addOption("d", "verbose", false, "Print verbose debug output.");
    }

    private static void dump(BonoboLibrary library) {
        System.out.printf("Library exports %d member(s).%n", library.getExports().size());
        System.out.println();
        Map<String, BonoboObject> exports = library.getExports();

        for (String name : exports.keySet()) {
            BonoboObject value = exports.get(name);
            System.out.printf("----- Symbol:%s -----%n", name);
            dumpObject(value);
        }
    }

    private static void dumpObject(BonoboObject value) {
        System.out.printf("Type: %s%n", value.getType().getName());

        if (value instanceof BonoboFunction) {
            BonoboFunction function = (BonoboFunction) value;
            System.out.printf("Return type: %s%n", function.getReturnType().getName());

            if (function.getParameters().isEmpty()) {
                System.out.println("<Function takes no parameters>");
            } else {
                System.out.printf("Parameters (%d):%n", function.getParameters().size());

                for (BonoboFunctionParameter parameter : function.getParameters()) {
                    System.out.printf("  - %s:%s%n", parameter.getName(), parameter.getType().getName());
                }
            }
        }

        System.out.printf("Source text: %s%n", value.getSource().getText());
        System.out.println();
    }

    public static void dumpType(BonoboType type) {
        System.out.printf("----- Type:%s -----%n", type.getName());
        System.out.printf("Parent type:%s%n", type.getParentType() != null ? type.getParentType().getName() : "none");
        System.out.println();
    }
}
