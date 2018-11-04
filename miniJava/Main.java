import syntaxtree.*;
import visitor.*;
import java.io.*;
import symbol_table.*;

class Main {

	public static void main (String [] args) throws Exception{

		if( args.length < 1 ){
			System.err.println("Usage: java Driver <inputFile>");
			System.exit(1);
		}
		FileInputStream fis = null;
		MiniJavaParser parser;
		TypeGathererVisitor tgv;
		TypeCheckerVisitor tcv;
		LlvmVisitor lv;
		Goal root;
		BufferedWriter llfile = null;

		for( int i=0; i<args.length; i++ ){

			try{

				fis = new FileInputStream(args[i]);
				parser = new MiniJavaParser(fis);
				System.err.println("File " + args[i] +" parsed successfully.");

				tgv = new TypeGathererVisitor();
				root = parser.Goal();
				root.accept(tgv, null);

				tcv = new TypeCheckerVisitor();
				root.accept(tcv, null);

				System.out.println("Type checking on file " + args[i] +" was successful.\n\n");
				SymbolTable.makeOffsetTable();

				llfile = new BufferedWriter(new FileWriter("./Output/" + SymbolTable.specialClassName +".ll"));
				lv = new LlvmVisitor(llfile);
				root.accept(lv,null);

			}

			catch(ParseException ex){
				System.out.println("#At file " + args[i] + ": " + ex.getMessage() + "\n");
			}

			catch(FileNotFoundException ex){
				System.err.println("#At file " + args[i] + ": " + ex.getMessage() + "\n");
			}

			catch(TypeCheckingException ex){
				System.err.println("#At file " + args[i] + ": " + ex.getMessage()+ "\n");
			}

			finally{

				try{
					if(fis != null) fis.close();
				}
				catch(IOException ex){
					System.err.println("#At file " + args[i] + ": " + ex.getMessage()  + "\n");
				}

				try{
					if(llfile != null) llfile.close();
				}
				catch(IOException ex){
					System.err.println("Error when closing llfile " + SymbolTable.specialClassName + ".ll :" + ex.getMessage()  + "\n");
				}

			}
			SymbolTable.clearSymbolTable();
			
		}

	}

}
