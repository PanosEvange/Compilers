import java.io.*;
import syntaxtree.*;
import visitor.GJDepthFirst;
import symbol_table.*;
import java.util.*;

public class LlvmVisitor extends GJDepthFirst<String, String[]>{

	private final BufferedWriter writer;
	private int varCounter;
	private int loopCounter;
	private int oobCounter;
	private int ifCounter;
	private int allocCounter;
	private int andClauseCounter;

	public LlvmVisitor(BufferedWriter llFile){
		this.writer = llFile;

		/* Write vtable for each class */
		Map <String,String> tempV_Table = new LinkedHashMap < String,String >(); /* (methodName,className) */
		List <FieldInfo> parameters ;
		int i;
		String paramList;
		String methods;
		for (String s: SymbolTable.classes.keySet()){

			methods = "";

			/* Fill in the v_table with methods */
			SymbolTable.classes.get(s).putMyMethodsInVtable(tempV_Table);

			/* Write this v_table in the ll file */
			for (String t: tempV_Table.keySet()){

				paramList = "i8*";
				parameters = new ArrayList <FieldInfo>( SymbolTable.classes.get(tempV_Table.get(t)).getMethod(t).getParameters() );

				for( i=0; i < parameters.size(); i++ ){
					paramList += "," + typeToLlType(parameters.get(i).getType());
				}

				methods += "i8* bitcast (" + typeToLlType(SymbolTable.classes.get(tempV_Table.get(t)).getMethod(t).getType()) + " (" + paramList +
							")* @" + tempV_Table.get(t) + "." + t + " to i8*), ";

			}
			/* Remove from methods string last ", " */
			methods = methods.replaceAll(", $", "");

			emit( "@." + s + "_vtable = global [" + tempV_Table.size() + " x i8*] [" + methods + "]");

			tempV_Table.clear();

		}

		emit("\n");

		/* Define helper methods */
		emit(
				"declare i8* @calloc(i32, i32)\n" +
				"declare i32 @printf(i8*, ...)\n" +
				"declare void @exit(i32)\n\n" +
				"@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
				"@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
				"define void @print_int(i32 %i) {\n" +
				"    %_str = bitcast [4 x i8]* @_cint to i8*\n" +
				"    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
				"    ret void\n" +
				"}\n\n" +
				"define void @throw_oob() {\n" +
				"    %_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
				"    call i32 (i8*, ...) @printf(i8* %_str)\n" +
				"    call void @exit(i32 1)\n" +
				"    ret void\n" +
				"}\n"
			);

	}

	private String typeToLlType( String toConvert ){

			if( toConvert.equals("int") ){
				return "i32";
			}
			else if( toConvert.equals("boolean") ){
				return "i1";
			}
			else if( toConvert.equals("int[]") ){
				return "i32*";
			}
			else{ /* It is a reference pointer */
				return "i8*";
			}

	}

	private String nextVar(){
		this.varCounter += 1;
		return "%_" + ( this.varCounter - 1 );
	}

	private String nextLoop(){
		this.loopCounter += 1;
		return "loop" + ( this.loopCounter - 1 );
	}

	private String nextOob(){
		this.oobCounter += 1;
		return "oob" + ( this.oobCounter - 1 );
	}

	private String nextIf(){
		this.ifCounter += 1;
		return "if" + ( this.ifCounter - 1 );
	}

	private String nextAlloc(){
		this.allocCounter += 1;
		return "arr_alloc" + ( this.allocCounter - 1 );
	}

	private String nextAndClause(){
		this.andClauseCounter += 1;
		return "andclause" + ( this.andClauseCounter - 1 );
	}

	private void emit(String s){
		try{
			writer.write(s);
			writer.newLine();
		}
		catch(IOException ex){
			System.err.println("Error when writing ll file : " + ex.getMessage()  + "\n");
		}
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> "public"
	* f4 -> "static"
	* f5 -> "void"
	* f6 -> "main"
	* f7 -> "("
	* f8 -> "String
	* f9 -> "["
	* f10 -> "]"
	* f11 -> Identifier()
	* f12 -> ")"
	* f13 -> "{"
	* f14 -> ( VarDeclaration() )*
	* f15 -> ( Statement() )*
	* f16 -> "}"
	* f17 -> "}"
	*/
	public String visit(MainClass n, String[] argu) throws Exception {

		String _ret = null;

		String[] names = new String[2];

		names[0] = n.f1.accept(this, argu); /* Class name  */

		names[1] = "main"; /* Method name  */

		emit("define i32 @main(){\n");

		/* Alocate local variables */
		if( n.f14.present() ){
			n.f14.accept(this, argu);
		}

		if( n.f15.present() ){
			n.f15.accept(this, names);
		}
		emit("\tret i32 0");
		emit("}\n");

		this.varCounter = 0;
		this.loopCounter = 0;
		this.oobCounter = 0;
		this.ifCounter = 0;
		this.allocCounter = 0;
		this.andClauseCounter = 0;
		
		return _ret;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> ( VarDeclaration() )*
	* f4 -> ( MethodDeclaration() )*
	* f5 -> "}"
	*/
	public String visit(ClassDeclaration n, String[] argu) throws Exception {
		String _ret=null;

		String[] names = new String[2];

		names[0] = n.f1.accept(this, argu); /* Class name  */

		if( n.f4.present() ){
			n.f4.accept(this, names);
		}

		return _ret;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "extends"
	* f3 -> Identifier()
	* f4 -> "{"
	* f5 -> ( VarDeclaration() )*
	* f6 -> ( MethodDeclaration() )*
	* f7 -> "}"
	*/
	public String visit(ClassExtendsDeclaration n, String[] argu) throws Exception {
		String _ret=null;

		String[] names = new String[2];

		names[0] = n.f1.accept(this, argu); /* Class name  */

		if( n.f6.present() ){
			n.f6.accept(this, names);
		}

		return _ret;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	* f2 -> ";"
	*/
	public String visit(VarDeclaration n, String[] argu) throws Exception {
		String _ret=null;

		String type = n.f0.accept(this, argu);
		String name = n.f1.accept(this, argu);

		emit("\t%" + name + " = alloca " + typeToLlType(type) + "\n");

		return _ret;
	}

	/**
	* f0 -> "public"
	* f1 -> Type()
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( FormalParameterList() )?
	* f5 -> ")"
	* f6 -> "{"
	* f7 -> ( VarDeclaration() )*
	* f8 -> ( Statement() )*
	* f9 -> "return"
	* f10 -> Expression()
	* f11 -> ";"
	* f12 -> "}"
	*/
	public String visit(MethodDeclaration n, String[] argu) throws Exception {
		String _ret=null;

		String type = n.f1.accept(this, argu);
		String retReg;

		argu[1] = n.f2.accept(this, argu); /* Method name */

		String paramList = "i8* %this";
		List <FieldInfo> parameters = new ArrayList <FieldInfo>( SymbolTable.classes.get(argu[0]).getMethod(argu[1]).getParameters() );

		for( int i=0; i < parameters.size(); i++ ){
			paramList += ", " + typeToLlType(parameters.get(i).getType()) + " %." + parameters.get(i).getName();
		}

		emit("define " + typeToLlType(type) + " @" + argu[0] + "." + argu[1] +
			 "(" + paramList + ") {\n");

		/* Store parameters in registers */
		if( n.f4.present() ){
			n.f4.accept(this, argu);
		}

		emit("");

		/* Alocate local variables */
		if( n.f7.present() ){
			n.f7.accept(this, argu);
		}

		if( n.f8.present() ){
			n.f8.accept(this, argu);
		}

		retReg = n.f10.accept(this, argu);;
		emit("\tret " + retReg);
		emit("}\n");
		this.varCounter = 0;
		this.loopCounter = 0;
		this.oobCounter = 0;
		this.ifCounter = 0;
		this.allocCounter = 0;
		this.andClauseCounter = 0;

		return _ret;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	*/
	public String visit(FormalParameter n, String[] argu) throws Exception {
		String _ret=null;

		String type = n.f0.accept(this, argu);
		String name = n.f1.accept(this, argu);

		emit("\t%" + name + " = alloca " + typeToLlType(type));
		emit("\tstore " + typeToLlType(type) + " %." + name + ", " + typeToLlType(type) +
			 "* %" + name);

		return _ret;
	}

	/**
	* f0 -> Identifier()
	* f1 -> "="
	* f2 -> Expression()
	* f3 -> ";"
	*/
	public String visit(AssignmentStatement n, String[] argu) throws Exception {

		String _ret=null;

		String classOwner,register,register2;
		int offset;

		String id = n.f0.accept(this, argu);
		String exprRegister = n.f2.accept(this, argu); /* It has the form of "type register" or "type literalValue"*/

		/* We first check if it is local variable/parameter */
		String idType = SymbolTable.classes.get(argu[0]).getMethod(argu[1]).getFieldType(id);

		if(idType != null){ /* It is a local variable/parameter */
			emit("\tstore " + exprRegister + ", " + typeToLlType(idType) + "* %" + id);
		}
		else{ /* It is a field of this class or of a superClass */

			/* Find the classOwner of this field. Class owner will be the class which did the last hide of this field */
			classOwner = SymbolTable.classes.get(argu[0]).fieldClassOwner(id);
			/* Find offset of this field */
			offset = SymbolTable.classesOffset.get(classOwner).getVarOffset(id);

			idType = SymbolTable.classes.get(argu[0]).getFieldType(id);

			register = nextVar();
			emit("\t" + register + " = getelementptr i8, i8* %this, i32 " + (8+offset) );
			register2 = nextVar();
			emit("\t" + register2 + " = bitcast i8* " + register + " to " + typeToLlType(idType) + "*");
			emit("\tstore " + exprRegister + ", " + typeToLlType(idType) + "* " + register2);

		}
		emit("");
		return _ret;

	}

	/**
	* f0 -> Identifier()
	* f1 -> "["
	* f2 -> Expression()
	* f3 -> "]"
	* f4 -> "="
	* f5 -> Expression()
	* f6 -> ";"
	*/
	public String visit(ArrayAssignmentStatement n, String[] argu) throws Exception {

		String _ret=null;

		String classOwner,register1,register2,arrayRegister,label1,label2;
		int offset;

		String id = n.f0.accept(this, argu);
		String exprRegister = n.f2.accept(this, argu); /* It has the form of "type register" or "type literalValue"*/

		/* Here we are sure that identifier will be of type array ( i32* ) as we have passed type checking. */
		/* We first check if it is local variable/parameter */
		if( SymbolTable.classes.get(argu[0]).getMethod(argu[1]).isFieldIn(id) ){ /* It is a local variable/parameter */
			arrayRegister = nextVar();
			emit("\t" + arrayRegister + " = load i32*, i32** %" + id);
		}
		else{ /* It is a field of this class or of a superClass */

			/* Find the classOwner of this field. Class owner will be the class which did the last hide of this field */
			classOwner = SymbolTable.classes.get(argu[0]).fieldClassOwner(id);
			/* Find offset of this field */
			offset = SymbolTable.classesOffset.get(classOwner).getVarOffset(id);

			register1 = nextVar();
			emit("\t" + register1 + " = getelementptr i8, i8* %this, i32 " + (8+offset) );
			register2 = nextVar();
			emit("\t" + register2 + " = bitcast i8* " + register1 + " to i32**" );
			arrayRegister = nextVar();
			emit("\t" + arrayRegister + " = load i32*, i32** " + register2);

		}

		register1 = nextVar();
		emit("\t" + register1 + " = load i32, i32* " + arrayRegister); /* Take length from first word of the array */
		register2 = nextVar();
		emit("\t" + register2 + " = icmp ult " + exprRegister + ", " + register1); /* Check if index < length and index > 0 */
		/* For this check we use index as unsigned int so if index < 0 , we will have overflow so length will be compared with a very big number so we will have index >= length */

		label1 = nextOob();
		label2 = nextOob();
		emit("\t" + "br i1 " + register2 + ", label %" + label1 + ", label %" + label2);
		emit("");

		emit(label1 + ":");
		register1 = nextVar();
		emit("\t" + register1 + " = add " + exprRegister + ", 1"); /* index++ as at the first word we have the length of the array */
		register2 = nextVar();
		emit("\t" + register2 + " = getelementptr i32, i32* " + arrayRegister + ", i32 " + register1); /* get the memory address of index position of the array so as to store the value we want */
		exprRegister = n.f5.accept(this, argu); /* It has the form of "type register" or "type literalValue"*/
		emit("\tstore " + exprRegister + ", i32* " + register2); /* store exprRegister value to the index position of the array */
		label1 = nextOob();
		emit("\tbr label %" + label1);
		emit("");

		emit(label2 + ":");
		emit("\t" + "call void @throw_oob()");
		emit("\t" + "br label %" + label1);
		emit("");

		emit(label1 + ":"); /* continue other Statements or return */

		return _ret;

	}

	/**
	* f0 -> "if"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	* f5 -> "else"
	* f6 -> Statement()
	*/
	public String visit(IfStatement n, String[] argu) throws Exception {

		String label1,label2;
		String _ret=null;

		String exprRegister = n.f2.accept(this, argu);

		label1 = nextIf();
		label2 = nextIf();

		emit("\tbr " + exprRegister + ", label %" + label1 + ", label %" + label2);
		emit("");

		emit(label1 + ":");
		n.f4.accept(this, argu);
		label1 = nextIf();
		emit("\tbr label %" + label1);
		emit("");

		emit(label2 + ":");
		n.f6.accept(this, argu);
		emit("\tbr label %" + label1);
		emit("");

		emit(label1 + ":");

		return _ret;

	}

	/**
	* f0 -> "while"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	*/
	public String visit(WhileStatement n, String[] argu) throws Exception {

		String loopStart,next,end;
		String _ret=null;
		String exprRegister;

		loopStart = nextLoop();
		emit("\tbr label %" + loopStart);
		emit("");

		emit(loopStart + ":");
		exprRegister = n.f2.accept(this, argu);
		next = nextLoop();
		end = nextLoop();
		emit("\tbr " + exprRegister + ", label %" + next + ", label %" + end);
		emit("");

		emit(next + ":");
		n.f4.accept(this, argu);
		emit("\tbr label %" + loopStart);
		emit("");

		emit(end + ":");

		return _ret;

	}

	/**
	* f0 -> "System.out.println"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> ";"
	*/
	public String visit(PrintStatement n, String[] argu) throws Exception {

		String _ret=null;

		String exprRegister = n.f2.accept(this, argu);

		emit("\tcall void (i32) @print_int(" + exprRegister +")");

		emit("");
		return _ret;

	}

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	public String visit(ArrayType n, String[] argu) throws Exception {
		return n.f0.toString() + n.f1.toString() + n.f2.toString();
	}

	/**
	* f0 -> "boolean"
	*/
	public String visit(BooleanType n, String[] argu) throws Exception {
		return n.f0.toString();
	}

	/**
	* f0 -> "int"
	*/
	public String visit(IntegerType n, String[] argu) throws Exception {
		return n.f0.toString();
	}


	/**
	* f0 -> Clause()
	* f1 -> "&&"
	* f2 -> Clause()
	*/
	public String visit(AndExpression n, String[] argu) throws Exception {

		String clauseReg1,clauseReg2;
		String register;
		String label0,label1,label2,label3;
		String parts[];

		clauseReg1 = n.f0.accept(this, argu);
		label0 = nextAndClause();
		label1 = nextAndClause();
		label2 = nextAndClause();
		label3 = nextAndClause();

		emit("\tbr label %" + label0);
		emit("");

		emit(label0 + ":");
		emit("\tbr " + clauseReg1 + ", label %" + label1 + ", label %" + label3);
		emit("");

		emit(label1 + ":");
		clauseReg2 = n.f2.accept(this, argu);/* It has the form of "type register" or "type literalValue" but we need only the register/literalValue */
		parts = clauseReg2.split(" ");
		clauseReg2 = parts[1];

		emit("\tbr label %" + label2);
		emit("");

		emit(label2 + ":");
		emit("\tbr label %" + label3);
		emit("");

		emit(label3 + ":");
		register = nextVar();
		emit("\t" + register + " = phi i1 [ 0, %" + label0 + " ], [ " + clauseReg2 +
			 ", %" + label2 + " ]");

		return "i1 " + register;

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "<"
	* f2 -> PrimaryExpression()
	*/
	public String visit(CompareExpression n, String[] argu) throws Exception {

		String exprReg1 = n.f0.accept(this, argu);
		String exprReg2 = n.f2.accept(this, argu);	/* It has the form of "type register" or "type literalValue" but we need only the register/literalValue */
		String register;
		String parts[];

		parts = exprReg2.split(" ");
		exprReg2 = parts[1];

		register = nextVar();
		emit("\t" + register + " = icmp slt " + exprReg1 + ", " + exprReg2);

		return "i1 " + register;

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	public String visit(PlusExpression n, String[] argu) throws Exception {

		String exprReg1 = n.f0.accept(this, argu);
		String exprReg2 = n.f2.accept(this, argu);	/* It has the form of "type register" or "type literalValue" but we need only the register/literalValue */
		String register;
		String parts[];

		parts = exprReg2.split(" ");
		exprReg2 = parts[1];

		register = nextVar();
		emit("\t" + register + " = add " + exprReg1 + ", " + exprReg2);

		return "i32 " + register;

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "-"
	* f2 -> PrimaryExpression()
	*/
	public String visit(MinusExpression n, String[] argu) throws Exception {

		String exprReg1 = n.f0.accept(this, argu);
		String exprReg2 = n.f2.accept(this, argu);	/* It has the form of "type register" or "type literalValue" but we need only the register/literalValue */
		String register;
		String parts[];

		parts = exprReg2.split(" ");
		exprReg2 = parts[1];

		register = nextVar();
		emit("\t" + register + " = sub " + exprReg1 + ", " + exprReg2);

		return "i32 " + register;

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "*"
	* f2 -> PrimaryExpression()
	*/
	public String visit(TimesExpression n, String[] argu) throws Exception {

		String exprReg1 = n.f0.accept(this, argu);
		String exprReg2 = n.f2.accept(this, argu);	/* It has the form of "type register" or "type literalValue" but we need only the register/literalValue */
		String register;
		String parts[];

		parts = exprReg2.split(" ");
		exprReg2 = parts[1];

		register = nextVar();
		emit("\t" + register + " = mul " + exprReg1 + ", " + exprReg2);

		return "i32 " + register;

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "["
	* f2 -> PrimaryExpression()
	* f3 -> "]"
	*/
	public String visit(ArrayLookup n, String[] argu) throws Exception {

		String arrayReg = n.f0.accept(this, argu);
		String indexReg = n.f2.accept(this, argu);

		String register1,register2,label1,label2;

		register1 = nextVar();
		emit("\t" + register1 + " = load i32, " + arrayReg); /* Get length of the array which is the first word at memoryAddress of arrayReg */
		register2 = nextVar();
		emit("\t" + register2 + " = icmp ult " + indexReg + ", " + register1);/* Check if index < length and index > 0 */
		/* For this check we use index as unsigned int so if index < 0 , we will have overflow so length will be compared with a very big number so we will have index >= length */

		label1 = nextOob();
		label2 = nextOob();
		emit("\tbr i1 " + register2 + ", label %" + label1 + ", label %" + label2);
		emit("");

		emit(label1 + ":");
		register1 = nextVar();
		emit("\t" + register1 + " = add " + indexReg + ", 1"); /* index++ as at the first word we have the length of the array */
		register2 = nextVar();
		emit("\t" + register2 + " = getelementptr i32, " + arrayReg + ", i32 " + register1);/* get the memory address of index position of the array */
		register1 = nextVar();
		emit("\t" + register1 + " = load i32, i32* " + register2); /* get the value which is stored in this memory address */
		label1 = nextOob();
		emit("\tbr label %" + label1);
		emit("");

		emit(label2 + ":");
		emit("\t" + "call void @throw_oob()");
		emit("\t" + "br label %" + label1);
		emit("");

		emit(label1 + ":");

		return "i32 " + register1;

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> "length"
	*/
	public String visit(ArrayLength n, String[] argu) throws Exception {

		String arrayReg = n.f0.accept(this, argu);
		String register;

		register = nextVar();
		emit("\t" + register + " = load i32, " + arrayReg); /* Get length of the array which is the first word at memoryAddress of arrayReg */

		return "i32 " + register;

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( ExpressionList() )?
	* f5 -> ")"
	*/
	public String visit(MessageSend n, String[] argu) throws Exception {

		String[] newArgu = new String[3]; /* We need one more position so as we can know the type of the PrimaryExpression , that means the name of the Class we need to find the offset */
		newArgu[0] = argu[0];
		newArgu[1] = argu[1];
		newArgu[2] = null;

		String objectReg,register1,register2,methodName,classOwner,returnType;
		int offset;
		List <FieldInfo> parameters ;
		int i;
		String paramList,argumList;

		objectReg = n.f0.accept(this, newArgu);
		methodName = n.f2.accept(this, argu);

		/* Find the classOwner of this method. Class owner will be the upper class in inheritance hierarchy which has got this method. */
		/* We need that as the offset of overriden methods is saved only at the upper class. */
		classOwner = SymbolTable.classes.get(newArgu[2]).methodUpperClassOwner(methodName);
		/* Find offset of this method */
		offset = SymbolTable.classesOffset.get(classOwner).getMethOffset(methodName);
		emit("\t; " + newArgu[2] + "." + methodName + " : " + offset/8);
		register1 = nextVar();
		emit("\t" + register1 + " = bitcast " + objectReg + " to i8***");
		register2 = nextVar();
		emit("\t" + register2 + " = load i8**, i8*** " + register1);
		register1 = nextVar();
		emit("\t" + register1 + " = getelementptr i8*, i8** " + register2 + ", i32 " + offset/8);
		register2 = nextVar();
		emit("\t" + register2 + " = load i8*, i8** " + register1); /* get the pointer which points to the methodName function */

		paramList = "i8*";
		parameters = new ArrayList <FieldInfo>( SymbolTable.classes.get(classOwner).getMethod(methodName).getParameters() );
		returnType = SymbolTable.classes.get(classOwner).getMethod(methodName).getType();

		for( i=0; i < parameters.size(); i++ ){
			paramList += "," + typeToLlType(parameters.get(i).getType());
		}

		register1 = nextVar();
		emit("\t" + register1 + " = bitcast i8* " + register2 + " to " + typeToLlType(returnType) +
			 " (" + paramList + ")*");

		argumList = objectReg;

		if( n.f4.present() ){
			argumList += n.f4.accept(this, argu);
		}

		register2 = nextVar();
		emit("\t" + register2 + " = call " + typeToLlType(returnType) + " " + register1 +
			 "(" + argumList + ")");

		if( argu.length == 3 ){ /* It means that we came from a MessageSend so we have to store the type of the object at argu[2] */
			argu[2] = returnType; /* Return type of method */
		}
		return typeToLlType(returnType) + " " + register2;

	}

	/**
	* f0 -> Expression()
	* f1 -> ExpressionTail()
	*/
	public String visit(ExpressionList n, String[] argu) throws Exception {

		String[] argumentList = new String[4]; /* We need also the information from argu */

		argumentList[0] = argu[0];
		argumentList[1] = argu[1];

		if( argu.length == 3 ){
			argumentList[2] = argu[2];
		}
		else{
			argumentList[2] = null;
		}

		argumentList[3] = ", " + n.f0.accept(this, argu);

		n.f1.accept(this, argumentList);
		return argumentList[3];

	}

	/**
	* f0 -> ","
	* f1 -> Expression()
	*/
	public String visit(ExpressionTerm n, String[] argu) throws Exception {

		String newArgu[];

		if( argu[2] != null ){ /* It means that argu at ExpressionList was of length 3 */
			newArgu = new String[3];
			newArgu[0] = argu[0];
			newArgu[1] = argu[1];
			newArgu[2] = argu[2];
		}
		else{
			newArgu = new String[2];
			newArgu[0] = argu[0];
			newArgu[1] = argu[1];
		}

		argu[3] += ", " + n.f1.accept(this, newArgu);
		return null;

	}

	/**
	* f0 -> IntegerLiteral()
	*       | TrueLiteral()
	*       | FalseLiteral()
	*       | Identifier()
	*       | ThisExpression()
	*       | ArrayAllocationExpression()
	*       | AllocationExpression()
	*       | BracketExpression()
	*/
	public String visit(PrimaryExpression n, String[] argu) throws Exception {

		String primExpr = n.f0.accept(this, argu);
		String parts[];
		String idType;
		String register,register2;
		String classOwner;
		int offset;



		if( primExpr.contains(" ") ){ /* it is not an identifier so return it as it is */
			return primExpr;
		}
		else{/* It is not a literal so we must search if it is local variable/parameter or field and what type is  */
			/* We first check if it is local variable/parameter */
			idType = SymbolTable.classes.get(argu[0]).getMethod(argu[1]).getFieldType(primExpr);
			if(idType != null){ /* It is a local variable/parameter */
				register = nextVar();
				emit("\t" + register + " = load " + typeToLlType(idType) + ", " + typeToLlType(idType) + "* %" + primExpr);
				if( argu.length == 3 ){ /* It means that we came from a MessageSend so we have to store the type of the object at argu[2] */
					argu[2] = idType;
				}
				return typeToLlType(idType) + " " + register;
			}
			else{ /* It is a field of this class or of a superClass */

				/* Find the classOwner of this field. Class owner will be the class which did the last hide of this field */
				classOwner = SymbolTable.classes.get(argu[0]).fieldClassOwner(primExpr);
				/* Find offset of this field */
				offset = SymbolTable.classesOffset.get(classOwner).getVarOffset(primExpr);

				idType = SymbolTable.classes.get(argu[0]).getFieldType(primExpr);

				register = nextVar();
				emit("\t" + register + " = getelementptr i8, i8* %this, i32 " + (8+offset) );
				register2 = nextVar();
				emit("\t" + register2 + " = bitcast i8* " + register + " to " + typeToLlType(idType) + "*");
				register = nextVar();
				emit("\t" + register + " = load " + typeToLlType(idType) + ", " + typeToLlType(idType) + "* " + register2 );

				if( argu.length == 3 ){ /* It means that we came from a MessageSend so we have to store the type of the object at argu[2] */
					argu[2] = idType;
				}

				return typeToLlType(idType) + " " + register;
			}
		}

	}

	/**
	* f0 -> <INTEGER_LITERAL>
	*/
	public String visit(IntegerLiteral n, String[] argu) throws Exception {
		return "i32 " + n.f0.toString();
	}

	/**
	* f0 -> "true"
	*/
	public String visit(TrueLiteral n, String[] argu) throws Exception {
		return "i1 1";
	}

	/**
	* f0 -> "false"
	*/
	public String visit(FalseLiteral n, String[] argu) throws Exception {
		return "i1 0";
	}

	/**
	* f0 -> "this"
	*/
	public String visit(ThisExpression n, String[] argu) throws Exception {
		if( argu.length == 3 ){ /* It means that we came from a MessageSend so we have to store the type of the object at argu[2] */
			argu[2] = argu[0];
		}
		return "i8* %this";
	}

	/**
	* f0 -> "new"
	* f1 -> "int"
	* f2 -> "["
	* f3 -> Expression()
	* f4 -> "]"
	*/
	public String visit(ArrayAllocationExpression n, String[] argu) throws Exception {

		String exprRegister = n.f3.accept(this, argu); /* It has the form of "type register" or "type literalValue" */
		String register1,register2;
		String label1,label2;

		register1 = nextVar();
		emit("\t" + register1 + " = icmp slt " + exprRegister + ", 0"); /* Check if Expression > 0 */

		label1 = nextAlloc();
		label2 = nextAlloc();
		emit("\t" + "br i1 " + register1 + ", label %" + label1 + ", label %" + label2);
		emit("");

		emit(label1 + ":");
		emit("\t" + "call void @throw_oob()");
		emit("\t" + "br label %" + label2);
		emit("");

		emit(label2 + ":");
		register1 = nextVar();
		emit("\t" + register1 + " = add " + exprRegister + ", 1"); /* do size++ as we will save length at the first position of the array */
		register2 = nextVar();
		emit("\t" + register2 + " = call i8* @calloc(i32 4, i32 " + register1 + ")");
		register1 = nextVar();
		emit("\t" + register1 + " = bitcast i8* " + register2 + " to i32*");
		emit("\t" + "store " + exprRegister + ", i32* " + register1); /* Save length at the first word of array */

		return "i32* " + register1;

	}

	/**
	* f0 -> "new"
	* f1 -> Identifier()
	* f2 -> "("
	* f3 -> ")"
	*/
	public String visit(AllocationExpression n, String[] argu) throws Exception {

		String id = n.f1.accept(this, argu);
		String objectReg,register1,register2;

		objectReg = nextVar();
		emit("\t" + objectReg + " = call i8* @calloc(i32 1, i32 " + (SymbolTable.classes.get(id).getTotalFieldBytes() + 8) + ")");
		register1 = nextVar();
		emit("\t" + register1 + " = bitcast i8* " + objectReg + " to i8***");
		register2 = nextVar();
		/* Get the memory address at the beggining of the vTable */
		emit("\t" + register2 + " = getelementptr [" + SymbolTable.classes.get(id).getTotalMethodNumber() + " x i8*], ["+
			 SymbolTable.classes.get(id).getTotalMethodNumber() + " x i8*]* @." + id + "_vtable, i32 0, i32 0");
		/* And store it at the beggining of the object ( like *this = &vTable )*/
		emit("\tstore i8** " + register2 + ", i8*** " + register1);

		if( argu.length == 3 ){ /* It means that we came from a MessageSend so we have to store the type of the object at argu[2] */
			argu[2] = id;
		}

		return "i8* " + objectReg;
	}

	/**
	* f0 -> "!"
	* f1 -> Clause()
	*/
	public String visit(NotExpression n, String[] argu) throws Exception {

		String clauseReg = n.f1.accept(this, argu); /* It has the form of "type register" or "type literalValue" but we need only the register/literalValue */
		String register;
		String parts[];

		parts = clauseReg.split(" ");
		clauseReg = parts[1];

		register = nextVar();
		emit("\t" + register + " = xor i1 1, " + clauseReg);

		return "i1 " + register;
	}

	/**
	* f0 -> "("
	* f1 -> Expression()
	* f2 -> ")"
	*/
	public String visit(BracketExpression n, String[] argu) throws Exception {
		return n.f1.accept(this, argu);
	}

	/**
	* f0 -> <IDENTIFIER>
	*/
	public String visit(Identifier n, String[] argu) throws Exception {
		return n.f0.toString();
	}

}
