package thosakwe.bonobo.analysis;

import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.objects.BonoboFunctionParameter;
import thosakwe.bonobo.language.types.BonoboIntegerType;
import thosakwe.bonobo.language.types.BonoboListType;

import java.io.IOException;
import java.util.Map;

public class BonoboDumper {
    public static void main(String[] args) throws IOException, BonoboException {
        if (args.length < 1) {
            System.err.println("fatal error: no input file");
            System.exit(1);
            return;
        }

        BonoboParser parser = Bonobo.parseFile(args[0]);
        BonoboParser.CompilationUnitContext ast = parser.compilationUnit();
        BonoboLibrary library = new StaticAnalyzer().analyzeCompilationUnit(ast);
        dump(library);
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
