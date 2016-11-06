package thosakwe.bonobo.analysis;

import thosakwe.bonobo.compiler.codegen.c.CBlock;

import java.util.EventListener;

public abstract class ScopeEventListener implements EventListener {
    public void onCreate(Scope scope) {}

    public void onDestroy(Scope scope) {}

    public void onRelease(Scope scope, CBlock block) {}
}
