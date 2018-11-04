package symbol_table;
import java.util.*;

public abstract class TypeInfo{

	private String name;

	public TypeInfo( String nameToGive ){
		name = nameToGive;
	}

	public String getName(){
		return name;
	}

	public Map <String,FieldInfo> getFieldsMap(){
		//implement it in some subclasses
		return null;
	}

	public Map <String,TypeInfo> getMethodsMap(){
		//implement it in some subclasses
		return null;
	}

	public String getParentName(){
		//implement it in some subclasses
		return null;
	}

	public void setName(String newName){
		name = newName;
	}

	public void newField(FieldInfo fieldToAdd){
		//implement it in some subclasses
	}

	public void newParameter(FieldInfo parameterToAdd){
		//implement it in some subclasses
	}

	public void newMethod(TypeInfo methodToAdd){
		//implement it in some subclasses
	}

	public abstract void print(); //implement it in all subclasses /* For debugging purpose */

	public String getType(){
		//implement it in some subclasses
		return null;
	}

	public boolean isFieldIn(String fieldName){
		//implement it in some subclasses
		return false;
	}

	public int isMethodIn(TypeInfo methodToCheck){
		//implement it in some subclasses
		return 1;
	}

	public String isMethodCallIn(TypeInfo methodToCheck){
		//implement it in some subclasses
		return null;
	}

	public int isMethodInParent(TypeInfo methodToCheck){
		//implement it in some subclasses
		return 1;
	}

	public String isMethodCallInParent(TypeInfo methodToCheck){
		//implement it in some subclasses
		return null;
	}

	public boolean isMethodInOffset(String nameToCheck){
		//implement it in some subclasses
		return false;
	}

	public boolean isMethodInParentOffset(String nameToCheck){
		//implement it in some subclasses
		return false;
	}

	public boolean isSameMethod(TypeInfo methodToCheck){
		//implement it in some subclasses
		return true;
	}

	public boolean isSameMethodCall(TypeInfo methodToCheck){
		//implement it in some subclasses
		return true;
	}

	public  Collection<FieldInfo> getParameters(){
		//implement it in some subclasses
		return null;
	}

	public boolean isParameterIn(String paramName){
		//implement it in some subclasses
		return false;
	}

	public boolean isParentType(String finalType){
		//implement it in some subclasses
		return true;
	}

	public String getFieldType(String fieldName){
		//implement it in some subclasses
		return null;
	}

	public TypeInfo getMethod(String methodToGet){
		//implement it in some subclasses
		return null;
	}

	public String fieldClassOwner(String fieldName){
		//implement it in some subclasses
		return null;
	}

	public String methodUpperClassOwner(String methodName){
		//implement it in some subclasses
		return null;
	}

	public void putMyMethodsInVtable( Map <String,String> vTable){
		//implement it in some subclasses
	}

	public int getTotalFieldBytes(){
		//implement it in some subclasses
		return 0;
	}

	public int getTotalMethodNumber(){
		//implement it in some subclasses
		return 0;
	}

}
