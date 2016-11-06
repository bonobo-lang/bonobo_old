package thosakwe.bonobo.compiler;

import org.antlr.v4.runtime.misc.Pair;
import thosakwe.bonobo.analysis.Symbol;

import java.util.ArrayList;
import java.util.List;

public class VirtualHeap {
    private List<Pair<String, Integer>> slots = new ArrayList<>();

    public void free(Symbol symbol) {
        slots.add(new Pair<>(symbol.getName(), symbol.getValue().toCExpression().getSize()));
    }

    public Pair<String, Integer> malloc(int size) {
        // Search for an available slot
        // of the given size.

        Pair<String, Integer> openSlot = null;

        for (Pair<String, Integer> pair : slots) {
            if (pair.b == size) {
                openSlot = pair;
                break;
            }
        }

        if (openSlot != null) {
            slots.remove(openSlot);
            return openSlot;
        } else return null;
    }
}
