package thosakwe.bonobo.lang;

public abstract class STType {
    public static final STType INT32 = new STType() {
        @Override
        public boolean isPointerType() {
            return false;
        }

        @Override
        public String toCType() {
            return "int";
        }
    };

    public static final STType STRING = new STType() {
        @Override
        public boolean isPointerType() {
            return true;
        }

        @Override
        public String toCType() {
            return "char*";
        }
    };

    @Override
    public boolean equals(Object obj) {
        return obj instanceof STType && ((STType) obj).toCType().equals(toCType());
    }

    public abstract String toCType();

    public abstract boolean isPointerType();
}
