package thosakwe.strongly_typed.compiler;

public class VirtualHeap {
    private int freeSpace = 0;
    private int heapSize = 0;

    public boolean free() {
        return false;
    }

    public boolean malloc(int size) {
        if (freeSpace >= 0) {
            freeSpace -= size;
            return true;
        } else return false;
    }
}
