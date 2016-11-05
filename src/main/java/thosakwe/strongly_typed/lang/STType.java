package thosakwe.strongly_typed.lang;

public abstract class STType {
    public static final STType INT32 = new STType() {
        @Override
        public String toCType() {
            return "int";
        }
    };

    public static final STType STRING = new STType() {
        @Override
        public String toCType() {
            return "char*";
        }
    };

    public abstract String toCType();
}
