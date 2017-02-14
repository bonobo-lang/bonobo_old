package thosakwe.bonobo.analysis;

class BaseStaticAnalyzer {
    private Scope scope = new Scope();

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
