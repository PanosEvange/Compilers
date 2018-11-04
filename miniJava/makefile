all: compile

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac ./symbol_table/ClassInfo.java
	javac ./symbol_table/FieldInfo.java
	javac ./symbol_table/MethodInfo.java
	javac ./symbol_table/SymbolTable.java
	javac ./symbol_table/TypeInfo.java
	javac ./symbol_table/ClassOffset.java
	javac TypeGathererVisitor.java
	javac TypeCheckerVisitor.java
	javac LlvmVisitor.java
	javac Main.java

clean:
	rm -f *.class *~
