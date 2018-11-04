package symbol_table;
import java.util.*;

public class ClassOffset {

	private String name;
	private Map <String,Integer> varOffset;
	private Map <String,Integer> methOffset;
	private int lastVarOffset;
	private int lastMethOffset;

	public ClassOffset(String name){
		varOffset = new LinkedHashMap < String,Integer >();
		methOffset = new LinkedHashMap < String,Integer >();
		this.name = name;
	}

	public void setLastOffset(int lastVarOffset,int lastMethOffset){
		this.lastVarOffset = lastVarOffset;
		this.lastMethOffset = lastMethOffset;
	}

	public int getLastVarOffset(){
		return lastVarOffset;
	}

	public int getLastMethOffset(){
		return lastMethOffset;
	}

	public void newVarOffset(String name,int offset){
		varOffset.put(name,offset);
	}

	public void newMethOffset(String name,int offset){
		methOffset.put(name,offset);
	}

	public boolean isMethodIn(String methName){
		return methOffset.containsKey(methName);
	}

	public int getVarOffset(String varName){
		return varOffset.get(varName);
	}

	public int getMethOffset(String methName){
		return methOffset.get(methName);
	}

	public void print(){

		System.out.println("-----------Class " + name + "-----------");

		System.out.println("---Variables---");
		for( String s: varOffset.keySet() ){
			System.out.println( name + "." + s + " : " + varOffset.get(s) ) ;
		}

		System.out.println("---Methods---");
		for( String k: methOffset.keySet() ){
			System.out.println( name + "." + k + " : " + methOffset.get(k) ) ;
		}

	}

}
