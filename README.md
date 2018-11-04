# Compiler implementation for the MiniJava language (a small subset of Java)

- Course Project of Compilers Course.

- Implemented in Java using tools like JavaCC and JTB.

- Also used the visitor design pattern for Static Checking (Semantic Analysis)
as well as for Generating Intermediate Code used by the LLVM compiler project.

## Compile

- being in miniJava/ folder run **make all**

## Execute

- being in miniJava/ folder run **java Main file1.java [file2.java] ... [fileN.java]**
```
- where fileX.java is the name of the file that our program must compile to LLVM IR
```

## Example to run

- **java Main ../examples/Factorial.java**
- **clang-4.0 Output/Factorial.ll -o out**
- **./out**
