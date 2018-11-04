package symbol_table;
import java.util.*;


public class ClassInfo extends TypeInfo{

	private String parentName;
	private Map <String,FieldInfo> fields;
	private Map <String,TypeInfo> methods;

	public ClassInfo(String nameToGive,String parentNameToGive){
		super(nameToGive);
		parentName = parentNameToGive;
		fields = new LinkedHashMap < String,FieldInfo >();
		methods = new LinkedHashMap < String,TypeInfo >();
	}

	public ClassInfo(){
		this(null,null);
	}

	public ClassInfo(String nameToGive){
		this(nameToGive,null);
	}

	public Map <String,FieldInfo> getFieldsMap(){
		return fields;
	}

	public Map <String,TypeInfo> getMethodsMap(){
		return methods;
	}

	public String getParentName(){
		return parentName;
	}

	public void setClass(String nameToGive,String parentNameToGive){
		super.setName(nameToGive);
		parentName = parentNameToGive;
	}

	public void newField(FieldInfo fieldToAdd){
		fields.put(fieldToAdd.getName(),fieldToAdd);
	}

	public void newMethod(TypeInfo methodToAdd){
		methods.put(methodToAdd.getName(),methodToAdd);
	}

	public boolean isFieldIn(String fieldName){
		return fields.containsKey(fieldName);
	}

	public int isMethodIn(TypeInfo methodToCheck){

		if( methods.containsKey( methodToCheck.getName() ) ){
			return -1;
		}
		else if( parentName != null ){
			return SymbolTable.classes.get(parentName).isMethodInParent(methodToCheck);
		}
		else{
			return 1;
		}

	}

	public int isMethodInParent(TypeInfo methodToCheck){

		if( methods.containsKey( methodToCheck.getName() ) ){

			if( methods.get( methodToCheck.getName() ).isSameMethod(methodToCheck) ){
				return 1;
			}
			else{
				return -1;
			}

		}
		else if( parentName != null ){  /* Method does not exist in this class so check parent class if exists */
			return SymbolTable.classes.get(parentName).isMethodInParent(methodToCheck);
		}
		else{
			return 1;
		}

	}

	public boolean isMethodInOffset(String nameToCheck){

		if( parentName != null ){
			return SymbolTable.classes.get(parentName).isMethodInParentOffset(nameToCheck);
		}
		else{
			return false;
		}

	}

	public boolean isMethodInParentOffset(String nameToCheck){

		if( methods.containsKey( nameToCheck ) ){
			return true;
		}
		else if( parentName != null ){  /* Method does not exist in this class so check parent class if exists */
			return SymbolTable.classes.get(parentName).isMethodInParentOffset(nameToCheck);
		}
		else{
			return false;
		}

	}

	public String isMethodCallIn(TypeInfo methodToCheck){

		if( methods.containsKey( methodToCheck.getName() ) ){

			if( methods.get( methodToCheck.getName() ).isSameMethodCall(methodToCheck) ){
				return methods.get(methodToCheck.getName()).getType();
			}
			else{
				return null;
			}

		}
		else if( parentName != null ){
			return SymbolTable.classes.get(parentName).isMethodCallInParent(methodToCheck);
		}
		else{
			return null;
		}

	}

	public String isMethodCallInParent(TypeInfo methodToCheck){

		if( methods.containsKey( methodToCheck.getName() ) ){

			if( methods.get( methodToCheck.getName() ).isSameMethodCall(methodToCheck) ){
				return methods.get(methodToCheck.getName()).getType();
			}
			else{
				return null;
			}

		}
		else if( parentName != null ){  /* Method does not exist in this class so check parent class if exists */
			return SymbolTable.classes.get(parentName).isMethodCallInParent(methodToCheck);
		}
		else{
			return null;
		}

	}

	public boolean isParentType(String finalType){

		if( finalType.equals( getName() ) ){
			return true;
		}
		else if( parentName != null ){
			return SymbolTable.classes.get(parentName).isParentType(finalType);
		}
		else{
			return false;
		}

	}

	public TypeInfo getMethod(String methodToGet){
		return methods.get(methodToGet);
	}

	public String getFieldType(String fieldName){

		if( fields.containsKey(fieldName) ){
			return fields.get(fieldName).getType();
		}
		else if( parentName != null ){
			return SymbolTable.classes.get(parentName).getFieldType(fieldName);
		}
		else{
			return null;
		}

	}

	public void putMyMethodsInVtable( Map <String,String> vTable ){
		if( parentName != null ){
			SymbolTable.classes.get(parentName).putMyMethodsInVtable(vTable);
		}
		for (String s: methods.keySet()){
			/* If there is already a methodName( key ) with different className( value ) it will be replace with current className */
			if( !("static void".equals(methods.get(s).getType()) ) ){ /* Static methods aren't stored in vTable */
				vTable.put( methods.get(s).getName(), this.getName() ); /* (methodName,className) */
			}

		}
	}

	public String fieldClassOwner(String fieldName){
		if( fields.containsKey(fieldName) ){
			return getName();
		}
		else if( parentName != null ){
			return SymbolTable.classes.get(parentName).fieldClassOwner(fieldName);
		}
		else{
			return null;
		}
	}

	public String methodUpperClassOwner(String methodName){

		String result;

		if( parentName != null ){
			result = SymbolTable.classes.get(parentName).methodUpperClassOwner(methodName);
			if( result == null ){
				if( methods.containsKey(methodName) ){
					return getName();
				}
				else{
					return null;
				}
			}
			else{	/* We found the upper class owner of methodName */
				return result;
			}
		}
		else{	/* We are at the upper class and we will check if it has got this method */
			if( methods.containsKey(methodName) ){
				return getName();
			}
			else{
				return null;
			}
		}

	}

	public int getTotalFieldBytes(){
		if( fields.isEmpty() ){
			if( parentName != null ){
				return SymbolTable.classes.get(parentName).getTotalFieldBytes();
			}
			else{
				return 0;
			}
		}
		else{
			return SymbolTable.classesOffset.get(getName()).getLastVarOffset();
		}
	}

	public int getTotalMethodNumber(){
		if( methods.isEmpty() ){
			if( parentName != null ){
				return SymbolTable.classes.get(parentName).getTotalMethodNumber();
			}
			else{
				return 0;
			}
		}
		else{
			return (SymbolTable.classesOffset.get(getName()).getLastMethOffset()) / 8 ;
		}
	}

	public void print(){  /* For debugging purpose */

		if( parentName != null ){
			System.out.println("Class " + getName() + " extends " + parentName );
		}
		else{
			System.out.println("Class " + getName() );
		}


		System.out.println("\nFields: \n");
		for (String s: fields.keySet()){
			fields.get(s).print();
		}
		System.out.println("+++++++++++++++");
		System.out.println("Methods: \n");
		for (String s: methods.keySet()){
			methods.get(s).print();
		}

		System.out.println("================");
	}

}
