# Strongly Typed
Goal: Elegant language that compiles to efficient native code. Provide GC
via the compiler, rather than a runtime.

# Usage

The StronglyTyped compiler produces C programs.
You can pipe its output into GCC for one-step compilation:
```bash
bin/strongly-typed main.str -stdout | gcc -xc -o main.o -
```

Alternatively, if you wish to see the generated C code:
```bash
bin/strongly-typed main.str
gcc main.c -o main.o
```