package symbol_table;
import java.util.*;


public class MethodInfo extends TypeInfo{

	private String returnType;
	private Map <String,FieldInfo> parameters;
	private Map <String,FieldInfo> localVariables;

	public MethodInfo(String returnTypeToGive,String nameToGive){
		super(nameToGive);
		returnType = returnTypeToGive;
		parameters = new LinkedHashMap <String,FieldInfo >();
		localVariables = new LinkedHashMap < String,FieldInfo >();
	}

	public void newParameter(FieldInfo parameterToAdd){
		parameters.put(parameterToAdd.getName(),parameterToAdd);
	}

	public void newField(FieldInfo fieldToAdd){
		localVariables.put(fieldToAdd.getName(),fieldToAdd);
	}

	public boolean isFieldIn(String fieldName){
		return ( parameters.containsKey(fieldName) || localVariables.containsKey(fieldName) );
	}

	public boolean isParameterIn(String paramName){
		return parameters.containsKey(paramName);
	}

	public String getType(){
		return returnType;
	}

	public String getFieldType(String fieldName){

		if( localVariables.containsKey(fieldName) ){
			return localVariables.get(fieldName).getType();
		}
		else if( parameters.containsKey(fieldName) ){
			return parameters.get(fieldName).getType();
		}
		else{
			return null;
		}

	}

	public boolean isSameMethod(TypeInfo methodToCheck){

		List<FieldInfo> paramCurrentMethod = new ArrayList<FieldInfo>( parameters.values() );
		List<FieldInfo> paramMethToCheck = new ArrayList<FieldInfo>( methodToCheck.getParameters() );
		String type1,type2;

		if( returnType.equals(methodToCheck.getType()) ){
			/* Same arguments = same number, same types. */
			if( paramCurrentMethod.size() == paramMethToCheck.size() ){ /* Is same number? */

				for( int i=0; i < paramCurrentMethod.size(); i++ ){

					type1 = paramCurrentMethod.get(i).getType();

					type2 = paramMethToCheck.get(i).getType();

					if( !type1.equals(type2) ){	/* Is same type? */
						return false;
					}

				}
				return true;

			}
			else{
				return false;
			}
		}
		else{
			return false;
		}


	}

	public boolean isSameMethodCall(TypeInfo methodToCheck){

		List<FieldInfo> paramCurrentMethod = new ArrayList<FieldInfo>( parameters.values() );
		List<FieldInfo> paramMethToCheck = new ArrayList<FieldInfo>( methodToCheck.getParameters() );
		String type1,type2;

		/* Same arguments = same number, same types. */
		if( paramCurrentMethod.size() == paramMethToCheck.size() ){ /* Is same number? */

			for( int i=0; i < paramCurrentMethod.size(); i++ ){

				type1 = paramCurrentMethod.get(i).getType();

				type2 = paramMethToCheck.get(i).getType();

				if( !type1.equals(type2) ){	/* Is same type or subtype? */

					if( SymbolTable.isClassName(type1) && SymbolTable.isClassName(type2) ){ /* So as to ensure that both are ClassName types */
						if( !SymbolTable.isSubtype(type2,type1) ){
							return false;
						}
					}
					else{
						return false;
					}

				}

			}
			return true;

		}
		else{
			return false;
		}


	}

	public  Collection<FieldInfo> getParameters(){
		return parameters.values();
	}

	public void print(){	/* For debugging purpose */
		System.out.println("Method " + getName());

		System.out.println("\nParameters :\n");
		for (String s: parameters.keySet()){
			parameters.get(s).print();
		}

		System.out.println("\nLocalVariables :\n");
		for (String s: localVariables.keySet()){
			localVariables.get(s).print();
		}

		System.out.println("---------------------");
	}

}
