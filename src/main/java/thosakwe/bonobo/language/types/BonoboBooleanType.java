package thosakwe.bonobo.language.types;

public class BonoboBooleanType extends BonoboAbstractClassImpl {
    public static final BonoboBooleanType INSTANCE = new BonoboBooleanType();

    private BonoboBooleanType() {
        super("bool");
    }
}
