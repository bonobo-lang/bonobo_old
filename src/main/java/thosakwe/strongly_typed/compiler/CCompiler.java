package thosakwe.strongly_typed.compiler;

import thosakwe.strongly_typed.compiler.codegen.c.CAstNode;
import thosakwe.strongly_typed.compiler.codegen.c.CFunctionDeclaration;
import thosakwe.strongly_typed.compiler.codegen.c.CProgram;
import thosakwe.strongly_typed.grammar.StronglyTypedBaseVisitor;
import thosakwe.strongly_typed.grammar.StronglyTypedParser;

public class CCompiler extends StronglyTypedBaseVisitor<CAstNode> {
    String compile(StronglyTypedParser.CompilationUnitContext ast) {
        final CodeBuilder builder = new CodeBuilder();
        final CProgram program = visitCompilationUnit(ast);
        program.apply(builder);
        return builder.toString();
    }

    @Override
    public CProgram visitCompilationUnit(StronglyTypedParser.CompilationUnitContext ctx) {
        final CProgram result = new CProgram();

        for (StronglyTypedParser.TopLevelDefContext def : ctx.topLevelDef()) {
            if (def instanceof StronglyTypedParser.TopLevelFuncDefContext) {
                result.getFunctions().add(visitTopLevelFuncDef((StronglyTypedParser.TopLevelFuncDefContext) def));
            }
        }

        return result;
    }

    @Override
    public CFunctionDeclaration visitTopLevelFuncDef(StronglyTypedParser.TopLevelFuncDefContext ctx) {
        final CFunctionDeclaration result = new CFunctionDeclaration();
        result.setName(ctx.funcSignature().name.getText());
        return result;
    }
}
