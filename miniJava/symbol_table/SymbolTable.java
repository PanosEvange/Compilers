package symbol_table;
import java.util.*;


public final class SymbolTable{

	public static  Map <String,TypeInfo> classes = new LinkedHashMap < String,TypeInfo >();
	public static  Map <String,ClassOffset> classesOffset = new LinkedHashMap <String,ClassOffset >();
	public static  String specialClassName;

	public static void setSpecialClassName(String name){
		specialClassName = name;
	}

	public static void clearSymbolTable(){
		classes.clear();
		classesOffset.clear();
	}

	public static boolean isClassName(String nameToCheck){
		return classes.containsKey(nameToCheck);
	}

	public static boolean isBasicType(String typeToCheck){
		return ( typeToCheck.equals("int") || typeToCheck.equals("int[]") || typeToCheck.equals("boolean") );
	}

	public static boolean isSubtype(String nameToCheck,String finalType){
		return classes.get(nameToCheck).isParentType(finalType);
	}

	public static String whatTypeIs(String idToCheck,String className,String methodName){

		String type;

		TypeInfo classToCheck = classes.get( className );

		TypeInfo methodToCheck = classToCheck.getMethod( methodName );

		type = methodToCheck.getFieldType(idToCheck); /* Check if it local variable or parameter in the method */

		if( type == null ){ /* It is not local variable or parameter so check if it is field of current class */
			type = classToCheck.getFieldType(idToCheck);
		}

		return type;

	}

	public static void makeOffsetTable(){

		int varOffset ;
		int methOffset ;
		ClassOffset tempClassOffset;
		String type,parent;

		for( String className: classes.keySet() ){
			varOffset = 0;
			methOffset = 0;

			tempClassOffset = new ClassOffset(className);

			parent = classes.get(className).getParentName();

			if( parent != null){
				varOffset = classesOffset.get(parent).getLastVarOffset();
				methOffset = classesOffset.get(parent).getLastMethOffset();
			}

			Map <String,FieldInfo> fieldsMap = classes.get(className).getFieldsMap();

			for( String fieldName: fieldsMap.keySet() ){

				tempClassOffset.newVarOffset(fieldName,varOffset);

				type = fieldsMap.get(fieldName).getType();
				if( isBasicType(type) ){
					if( type.equals("int") ){
						varOffset += 4;
					}
					else if(type.equals("boolean") ){
						varOffset += 1;
					}
					else{ /*it is int[] which is pointer */
						varOffset += 8;
					}
				}
				else{  /*It is a className so it is pointer */
					varOffset += 8;
				}

			}

			Map <String,TypeInfo> methodMap = classes.get(className).getMethodsMap();

			for( String methodName: methodMap.keySet() ){

				if( !methodMap.get(methodName).getType().equals("static void") ){
					if( !classes.get(className).isMethodInOffset(methodName) ){
						tempClassOffset.newMethOffset(methodName,methOffset);
						methOffset += 8;
					}
				}

			}

			tempClassOffset.setLastOffset(varOffset,methOffset);
			classesOffset.put(className,tempClassOffset);

		}

	}

	public static void printOffset(){

		for( String s: classesOffset.keySet() ){
			if( !s.equals(SymbolTable.specialClassName)){
				classesOffset.get(s).print();
				System.out.println("");
			}
		}

	}

}
