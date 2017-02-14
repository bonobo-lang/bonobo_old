package thosakwe.bonobo.analysis;

class BaseStaticAnalyzer {
    private Scope scope;

    BaseStaticAnalyzer(boolean debug) {
        this.scope = new Scope(debug);
    }

    public Scope getScope() {
        return scope;
    }

    void pushScope() {
        scope = scope.fork();
    }

    void popScope() {
        scope = scope.join();
    }
}
