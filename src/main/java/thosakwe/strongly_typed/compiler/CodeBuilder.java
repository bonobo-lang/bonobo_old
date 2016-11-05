package thosakwe.strongly_typed.compiler;

import java.io.StringWriter;

public class CodeBuilder extends StringWriter {
    private int indentationLevel = 0;

    public void applyTabs() {
        for (int i = 0; i < indentationLevel; i++)
            write("  ");
    }

    public void indent() {
        if (indentationLevel < 0)
            indentationLevel = 1;
        else indentationLevel++;
    }

    public void outdent() {
        indentationLevel--;
    }

    public void write(Object object) {
        write(String.valueOf(object));
    }

    public void writeln(Object object) {
        write(object);
        println();
    }

    public void print(Object object) {
        applyTabs();
        write(object);
    }

    public void println() {
        write("\n");
    }

    public void println(Object object) {
        applyTabs();
        write(object);
        println();
    }

    public void resetIndentation() {
        indentationLevel = 0;
    }
}