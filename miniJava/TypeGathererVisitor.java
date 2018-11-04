import java.io.*;
import syntaxtree.*;
import visitor.GJDepthFirst;
import symbol_table.*;

public class TypeGathererVisitor extends GJDepthFirst<String, TypeInfo>{


	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> "public"
	* f4 -> "static"
	* f5 -> "void"
	* f6 -> "main"
	* f7 -> "("
	* f8 -> "String"
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
	public String visit(MainClass n, TypeInfo argu) throws Exception {
		String _ret=null;

		String name = n.f1.accept(this, argu);

		TypeInfo specialClass = new ClassInfo(name);

		TypeInfo main = new MethodInfo("static void","main");

		String paramName = n.f11.accept(this, argu);
		FieldInfo param = new FieldInfo("String[]",paramName);

		main.newParameter(param);

		n.f14.accept(this,main);

		specialClass.newMethod(main);

		SymbolTable.classes.put(specialClass.getName(),specialClass);
		SymbolTable.setSpecialClassName(name);
		
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
	public String visit(ClassDeclaration n, TypeInfo argu) throws Exception {
		String _ret=null;

		String name = n.f1.accept(this, argu);

		if( SymbolTable.isClassName(name) ){  /* Check if class with name already exists */
			throw new TypeCheckingException("Error! Class with name " + name + " already exists!");
		}

		TypeInfo tempClass = new ClassInfo(name);

		if( n.f3.present() ){
			n.f3.accept(this, tempClass);
		}

		if( n.f4.present() ){
			n.f4.accept(this, tempClass);
		}

		SymbolTable.classes.put(tempClass.getName(),tempClass);

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
	public String visit(ClassExtendsDeclaration n, TypeInfo argu) throws Exception {
		String _ret=null;

		String name =  n.f1.accept(this, argu);

		if( SymbolTable.isClassName(name) ){ /* Check if class with name already exists */
			throw new TypeCheckingException("Error! Class with name " + name + " already exists!");
		}

		String parent = n.f3.accept(this, argu);

		if( !SymbolTable.isClassName(parent) ){ /* Parent of this class should exist or it is error */
			throw new TypeCheckingException("Error! No class " + parent + " has been defined.So invalid declaration of class " + name + " !");
		}

		TypeInfo tempClass = new ClassInfo(name,parent);

		if( n.f5.present() ){
			n.f5.accept(this, tempClass);
		}

		if( n.f6.present() ){
			n.f6.accept(this, tempClass);
		}

		SymbolTable.classes.put(tempClass.getName(),tempClass);

		return _ret;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	* f2 -> ";"
	*/
	public String visit(VarDeclaration n, TypeInfo argu) throws Exception {
		String _ret=null;

		String type = n.f0.accept(this, argu);
		String name = n.f1.accept(this, argu);

		if( argu.isFieldIn(name) ){ /* Fields ( field of a class or localVariables ) must be unique */
			throw new TypeCheckingException("Error! Variable " + name + " is already defined!");
		}

		FieldInfo tempVar = new FieldInfo(type,name);

		argu.newField(tempVar);

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
	public String visit(MethodDeclaration n, TypeInfo argu) throws Exception {
		String _ret=null;

		String type = n.f1.accept(this, argu);

		String name = n.f2.accept(this, argu);

		TypeInfo tempMethod = new MethodInfo(type,name);

		if( n.f4.present() ){
			n.f4.accept(this, tempMethod);
		}

		if( n.f7.present() ){
			n.f7.accept(this, tempMethod);
		}

		if( argu.isMethodIn(tempMethod) != 1 ){ /* Check if this methodName already exists in this class and check if this method overrides a method from a parent class */
			throw new TypeCheckingException("Error! Method " + name + " is already defined or it is diferrent from the method that is defined in a superClass!");
		}

		argu.newMethod(tempMethod);

		return _ret;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	*/
	public String visit(FormalParameter n, TypeInfo argu) throws Exception {
		String _ret=null;

		String type = n.f0.accept(this, argu);
		String name = n.f1.accept(this, argu);

		if( argu.isParameterIn(name) ){ /* Parameter name must be unique */
			throw new TypeCheckingException("Error! Parameter " + name + " is already defined!");
		}

		FieldInfo tempParam = new FieldInfo(type,name);

		argu.newParameter(tempParam);

		return _ret;
	}

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	public String visit(ArrayType n, TypeInfo argu) throws Exception {
		return n.f0.toString() + n.f1.toString() + n.f2.toString();
	}

	/**
	* f0 -> "boolean"
	*/
	public String visit(BooleanType n, TypeInfo argu) throws Exception {
		return n.f0.toString();
	}

	/**
	* f0 -> "int"
	*/
	public String visit(IntegerType n, TypeInfo argu) throws Exception {
		return n.f0.toString();
	}

	/**
	* f0 -> <IDENTIFIER>
	*/
	public String visit(Identifier n, TypeInfo argu) throws Exception {
		return n.f0.toString();
	}

}
