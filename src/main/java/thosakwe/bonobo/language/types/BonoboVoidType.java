package thosakwe.bonobo.language.types;

import thosakwe.bonobo.language.BonoboType;

/**
 * Created on 2/14/2017.
 */
public class BonoboVoidType extends BonoboAbstractClassImpl {
    public static final BonoboVoidType INSTANCE = new BonoboVoidType();

    private BonoboVoidType() {
        super("void");
    }
}
