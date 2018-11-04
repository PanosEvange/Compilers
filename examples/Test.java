class Test{
    public static void main(String[] a){
	
    }
}

class A{
   
    public boolean foo(int v_key){
	return true ;
    }

    
    public boolean bla(int rn){
	return true ;
    }
    
    
    public boolean hoho(int ln){
	return true ;
    }

}

class B extends A{
   
    public boolean bloo(int v_key){
	return true ;
    }

    
    public boolean foo(int rn){
	return true ;
    }
    

}

class C extends B{
   
    public boolean bla(int v_key){
	return true ;
    }

    
    public boolean bar(int rn){
	return true ;
    }
    
    public boolean foo(int rn){
	return true ;
    }

}

class D extends C{

}

class E{
   
    public boolean Init(int v_key){
	return true ;
    }

    
    public boolean SetRight(int rn){
	return true ;
    }
    
    
    public boolean SetLeft(int ln){
	return true ;
    }

}
