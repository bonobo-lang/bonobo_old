package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;

class BaseStaticAnalyzer {
    private Scope lastPoppedScope = null;
    private Scope scope;

    BaseStaticAnalyzer(boolean debug, ParserRuleContext source) {
        this.scope = new Scope(debug, source);
    }

    public Scope getLastPoppedScope() {
        return lastPoppedScope;
    }

    public Scope getScope() {
        return scope;
    }

    void pushScope(ParserRuleContext source) {
        lastPoppedScope = scope;
        scope = scope.fork(source);
    }

    void popScope() {
        scope = scope.join();
    }
}
