package symbol_table;
import java.util.*;


public class FieldInfo extends TypeInfo{

	private String type;

	public FieldInfo(String typeToGive,String nameToGive){
		super(nameToGive);
		type = typeToGive;
	}

	public String getType(){
		return type;
	}

	public void print(){	/* For debugging purpose */
		System.out.println( type + " " + getName() );
	}

}
