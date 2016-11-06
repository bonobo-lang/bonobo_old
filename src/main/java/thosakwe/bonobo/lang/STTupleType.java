package thosakwe.bonobo.lang;

public class STTupleType extends STType {
    private final STType childType;

    public STTupleType(STType childType) {
        this.childType = childType;
    }

    @Override
    public boolean isPointerType() {
        return true;
    }

    @Override
    public String toCType() {
        return String.format("%s*", childType.toCType());
    }
}
