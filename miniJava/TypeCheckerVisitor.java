import java.io.*;
import syntaxtree.*;
import visitor.GJDepthFirst;
import symbol_table.*;

public class TypeCheckerVisitor extends GJDepthFirst<String, String[]>{

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

		if( n.f14.present() ){
			n.f14.accept(this, argu);
		}

		if( n.f15.present() ){
			n.f15.accept(this, names);
		}

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

		if( n.f3.present() ){
			n.f3.accept(this, argu);
		}

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

		if( n.f5.present() ){
			n.f5.accept(this, argu);
		}

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

		if( !SymbolTable.isBasicType(type) ){
			if( !SymbolTable.isClassName(type) ){
				throw new TypeCheckingException("Error! There is no class " + type + " so variable declaration " + type + " " + n.f1.accept(this, argu) + " is invalid!");
			}
		}

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
		argu[1] = n.f2.accept(this, argu); /* Method name */

		if( n.f4.present() ){
			n.f4.accept(this, argu);
		}

		if( n.f7.present() ){
			n.f7.accept(this, argu);
		}

		if( n.f8.present() ){
			n.f8.accept(this, argu);
		}

		String returnType = n.f10.accept(this, argu);

		if( SymbolTable.isBasicType(returnType) ){
			if( !returnType.equals(type) ){
				throw new TypeCheckingException("Error! At method " + argu[1] + " return type " + returnType
				+ " does not match with the return type of the method which is " + type +" !");
			}
		}
		else{ /* It is a class type so check if it subtype */
			if( SymbolTable.isClassName(returnType) ){
				if( !SymbolTable.isSubtype(returnType,type) ){
					throw new TypeCheckingException("Error! At method " + argu[1] + " return type " + returnType
					+ " does not match with the return type of the method which is " + type +" !");
				}
			}
			else{
				throw new TypeCheckingException("Error! At method " + argu[1] + " return type " + returnType
				+ " does not match with the return type of the method which is " + type +" !");
			}
		}


		return _ret;

	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	*/
	public String visit(FormalParameter n, String[] argu) throws Exception {

		String _ret=null;

		String type = n.f0.accept(this, argu);

		if( !SymbolTable.isBasicType(type) ){
			if( !SymbolTable.isClassName(type) ){
				throw new TypeCheckingException("Error! There is no class " + type + " so parameter " + type + " " + n.f1.accept(this, argu) + " is invalid!");
			}
		}

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

		String id = n.f0.accept(this, argu);

		String idType = SymbolTable.whatTypeIs(id,argu[0],argu[1]);
		if( idType == null ){ /* variable is not defined */
			throw new TypeCheckingException("Error! Variable " + id + " is not defined!");
		}

		String exprType = n.f2.accept(this, argu);

		if( SymbolTable.isBasicType(idType) ){
			if( !exprType.equals(idType) ){
				throw new TypeCheckingException("Error! At method " + argu[1] + " the type of identifier " + id
				+ " is " + idType + ". The type of the expression right the AssignmentStatement is " + exprType
				+ " which does not match with the type of the identifier. So this AssignmentStatement is invalid!");
			}
		}
		else{ /* It is a class type so check if it subtype */
			if( SymbolTable.isClassName(exprType) && SymbolTable.isClassName(idType) ){ /* we should check if exprType is className as it may be basic type */
				if( !SymbolTable.isSubtype(exprType,idType) ){
					throw new TypeCheckingException("Error! At method " + argu[1] + " the type of identifier " + id
					+ " is " + idType + ". The type of the expression right the AssignmentStatement is " + exprType
					+ " which does not match with the type of the identifier. So this AssignmentStatement is invalid!");
				}
			}
			else{
				throw new TypeCheckingException("Error! At method " + argu[1] + " the type of identifier " + id
				+ " is " + idType + ". The type of the expression right the AssignmentStatement is " + exprType
				+ " which does not match with the type of the identifier. So this AssignmentStatement is invalid!");
			}
		}

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

		String id = n.f0.accept(this, argu);

		String idType = SymbolTable.whatTypeIs(id,argu[0],argu[1]);

		if( !idType.equals("int[]") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of identifier " + id
			+ " is " + idType + ". As we have ArrayAssignmentStatement it should be int[] . So this ArrayAssignmentStatement is invalid!");
		}

		String exprType1 = n.f2.accept(this, argu);

		if( !exprType1.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of Expression in [ ] left to the ArrayAssignmentStatement is "
			+ exprType1 + " . It should be int , so this ArrayAssignmentStatement is invalid!");
		}

		String exprType2 = n.f5.accept(this, argu);

		if( !exprType2.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of Expression in right to the ArrayAssignmentStatement is "
			+ exprType1 + " . It should be int , so this ArrayAssignmentStatement is invalid!");
		}

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

		String _ret=null;

		String exprType = n.f2.accept(this, argu);

		if( !exprType.equals("boolean") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of Expression in IfStatement is "
			+ exprType + " . It should be boolean , so this IfStatement is invalid!");
		}

		n.f4.accept(this, argu);
		n.f6.accept(this, argu);

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
		String _ret=null;

		String exprType = n.f2.accept(this, argu);

		if( !exprType.equals("boolean") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of Expression in WhileStatement is "
			+ exprType + " . It should be boolean , so this WhileStatement is invalid!");
		}

		n.f4.accept(this, argu);

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

		String exprType = n.f2.accept(this, argu);

		if( !exprType.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of Expression in PrintStatement is "
			+ exprType + " . It should be int , so this PrintStatement is invalid!");
		}

		return _ret;

	}

	/**
	* f0 -> Clause()
	* f1 -> "&&"
	* f2 -> Clause()
	*/
	public String visit(AndExpression n, String[] argu) throws Exception {

		String exprType1 = n.f0.accept(this, argu);

		if( !exprType1.equals("boolean") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of Clause in AndExpression left to the && operator is "
			+ exprType1 + " . It should be boolean , so this AndExpression is invalid!");
		}

		String exprType2 = n.f2.accept(this, argu);

		if( !exprType2.equals("boolean") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of Clause in AndExpression right to the && operator is "
			+ exprType2 + " . It should be boolean , so this AndExpression is invalid!");
		}

		return "boolean";

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "<"
	* f2 -> PrimaryExpression()
	*/
	public String visit(CompareExpression n, String[] argu) throws Exception {

		String exprType1 = n.f0.accept(this, argu);

		if( !exprType1.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in CompareExpression left to the < operator is "
			+ exprType1 + " . It should be int , so this CompareExpression is invalid!");
		}

		String exprType2 = n.f2.accept(this, argu);

		if( !exprType2.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in CompareExpression right to the < operator is "
			+ exprType2 + " . It should be int , so this CompareExpression is invalid!");
		}

		return "boolean";

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	public String visit(PlusExpression n, String[] argu) throws Exception {

		String exprType1 = n.f0.accept(this, argu);

		if( !exprType1.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in PlusExpression left to the + operator is "
			+ exprType1 + " . It should be int , so this PlusExpression is invalid!");
		}

		String exprType2 = n.f2.accept(this, argu);

		if( !exprType2.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in PlusExpression right to the + operator is "
			+ exprType2 + " . It should be int , so this PlusExpression is invalid!");
		}

		return "int";

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "-"
	* f2 -> PrimaryExpression()
	*/
	public String visit(MinusExpression n, String[] argu) throws Exception {

		String exprType1 = n.f0.accept(this, argu);

		if( !exprType1.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in MinusExpression left to the - operator is "
			+ exprType1 + " . It should be int , so this MinusExpression is invalid!");
		}

		String exprType2 = n.f2.accept(this, argu);

		if( !exprType2.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in MinusExpression right to the - operator is "
			+ exprType2 + " . It should be int , so this MinusExpression is invalid!");
		}

		return "int";

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "*"
	* f2 -> PrimaryExpression()
	*/
	public String visit(TimesExpression n, String[] argu) throws Exception {

		String exprType1 = n.f0.accept(this, argu);

		if( !exprType1.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in TimesExpression left to the * operator is "
			+ exprType1 + " . It should be int , so this TimesExpression is invalid!");
		}

		String exprType2 = n.f2.accept(this, argu);

		if( !exprType2.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in TimesExpression right to the * operator is "
			+ exprType2 + " . It should be int , so this TimesExpression is invalid!");
		}

		return "int";

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "["
	* f2 -> PrimaryExpression()
	* f3 -> "]"
	*/
	public String visit(ArrayLookup n, String[] argu) throws Exception {


		String exprType1 = n.f0.accept(this, argu);

		if( !exprType1.equals("int[]") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in ArrayLookup left to [ ] is "
			+ exprType1 + " . It should be int[] , so this ArrayLookup is invalid!");
		}

		String exprType2 = n.f2.accept(this, argu);

		if( !exprType2.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in ArrayLookup in [ ] is "
			+ exprType2 + " . It should be int , so this ArrayLookup is invalid!");
		}

		return "int";

	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> "length"
	*/
	public String visit(ArrayLength n, String[] argu) throws Exception {

		String exprType = n.f0.accept(this, argu);

		if( !exprType.equals("int[]") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in ArrayLength left to . is "
			+ exprType + " . It should be int[] , so this ArrayLength is invalid!");
		}

		return "int";

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

		String primExprType = n.f0.accept(this, argu);

		String idName = n.f2.accept(this, argu);

		String argumentTypeList,className;

		if( n.f4.present() ){
			argumentTypeList = n.f4.accept(this, argu);
		}
		else{
			argumentTypeList = null;
		}

		if( SymbolTable.isBasicType(primExprType) ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in MessageSend left to . is "
			+ primExprType + " . It should be this or className , so this MessageSend is invalid!");
		}

		if( SymbolTable.isClassName(primExprType) ){
			className = primExprType;
		}
		else{
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of PrimaryExpression in MessageSend left to . is "
			+ primExprType + " . It should be this or className , so this MessageSend is invalid!");
		}

		TypeInfo tempMethod;

		tempMethod = new MethodInfo(null,idName);

		if( argumentTypeList != null ){

			FieldInfo tempParam;
			String difName;
			if( argumentTypeList.contains(",") ){

				String[] argumentTypes = argumentTypeList.split(",");

				for( int i=0; i<argumentTypes.length; i++ ){
					difName = "a"+i;		/* We need to give different name as we have hashing */
					tempParam = new FieldInfo(argumentTypes[i],difName);
					tempMethod.newParameter(tempParam);
				}

			}
			else{ /* We have only one argument */
				tempParam = new FieldInfo(argumentTypeList,"a");
				tempMethod.newParameter(tempParam);
			}
		}

		String returnType = SymbolTable.classes.get(className).isMethodCallIn(tempMethod);

		if( returnType == null ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " in MessageSend right to . method " + idName +" has wrong arguments or it is undefined."
			+ " So this MessageSend is invalid!");
		}

		return returnType;

	}

	/**
	* f0 -> Expression()
	* f1 -> ExpressionTail()
	*/
	public String visit(ExpressionList n, String[] argu) throws Exception {

		String[] argumentTypeList = new String[3]; /* We need also the information from argu */

		argumentTypeList[0] = argu[0];
		argumentTypeList[1] = argu[1];
		argumentTypeList[2] = n.f0.accept(this, argu);

		n.f1.accept(this, argumentTypeList);
		return argumentTypeList[2];

	}

	/**
	* f0 -> ","
	* f1 -> Expression()
	*/
	public String visit(ExpressionTerm n, String[] argu) throws Exception {
		argu[2] += "," + n.f1.accept(this, argu);
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

		String primExprType = n.f0.accept(this, argu);

		if( SymbolTable.isBasicType(primExprType) || SymbolTable.isClassName(primExprType) ){
			return primExprType;
		}

		if( primExprType.equals("this") ){
			return argu[0];
		}

		if( primExprType.contains(" ") ){ /* it came from an AllocationExpression so we need to return class Name as type. primExprType here is something like "class nameOfClass" */
			String[] substrings = primExprType.split(" ");
			return substrings[1];
		}

		/* If we are here it means that primExprType is a variable/identifier */
		String idType = SymbolTable.whatTypeIs(primExprType,argu[0],argu[1]);

		if( idType == null ){ /* variable is not defined */
			throw new TypeCheckingException("Error! Variable " + primExprType + " is not defined!");
		}

		return idType;
	}

	/**
	* f0 -> <INTEGER_LITERAL>
	*/
	public String visit(IntegerLiteral n, String[] argu) throws Exception {
		return "int";
	}

	/**
	* f0 -> "true"
	*/
	public String visit(TrueLiteral n, String[] argu) throws Exception {
		return "boolean";
	}

	/**
	* f0 -> "false"
	*/
	public String visit(FalseLiteral n, String[] argu) throws Exception {
		return "boolean";
	}

	/**
	* f0 -> "this"
	*/
	public String visit(ThisExpression n, String[] argu) throws Exception {
		return "this";
	}

	/**
	* f0 -> "new"
	* f1 -> "int"
	* f2 -> "["
	* f3 -> Expression()
	* f4 -> "]"
	*/
	public String visit(ArrayAllocationExpression n, String[] argu) throws Exception {

		String exprType = n.f3.accept(this, argu);

		if( !exprType.equals("int") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of Expression in ArrayAllocationExpression in [ ] is "
			+ exprType + " . It should be int , so this ArrayAllocationExpression is invalid!");
		}

		return "int[]";

	}

	/**
	* f0 -> "new"
	* f1 -> Identifier()
	* f2 -> "("
	* f3 -> ")"
	*/
	public String visit(AllocationExpression n, String[] argu) throws Exception {

		String name = n.f1.accept(this, argu);
		/* Here Identifier should be a class name else it is error */
		if( !SymbolTable.isClassName(name) ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " in AllocationExpression the identifier "
			+ name + " is not a class name so this AllocationExpression is invalid!");
		}

		return "class " + name;
	}

	/**
	* f0 -> "!"
	* f1 -> Clause()
	*/
	public String visit(NotExpression n, String[] argu) throws Exception {

		String exprType = n.f1.accept(this, argu);

		if( !exprType.equals("boolean") ){
			throw new TypeCheckingException("Error! At method " + argu[1] + " the type of Clause in NotExpression is "
			+ exprType + " . It should be boolean , so this NotExpression is invalid!");
		}

		return "boolean";
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
	* f0 -> <IDENTIFIER>
	*/
	public String visit(Identifier n, String[] argu) throws Exception {
		return n.f0.toString();
	}

}
