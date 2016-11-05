package thosakwe.strongly_typed.analysis;

public abstract class ScopeEventListener {
    public void onCreate(Scope scope) {}

    public void onDestroy(Scope scope) {}
}
