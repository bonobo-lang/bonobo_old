package thosakwe.bonobo.analysis;

class BaseStaticAnalyzer {
    private Scope scope = new Scope();

    void pushScope() {
        scope = scope.fork();
    }

    void popScope() {
        scope = scope.join();
    }
}
