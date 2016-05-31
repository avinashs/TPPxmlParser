package avi;

import java.util.HashSet;
import java.util.Set;





/*
 * This class holds the data of the global variables
 */


public class GLOBALS {
	public static String srcDir = null;
	public static String DBname = null;
	public static String fileType = null;
	public static String[] xmlFiles;
	public static String fileSepChar = System.getProperty("file.separator"); // get either '\' or '/'
	public static boolean printHeader = true;
	public static boolean getSpecCount = false;
	public static boolean mysql = false;
	public static String timestamp = null;
	
	// holds info. about which search engines were used.
	public static Set<String> searchEngineSet = new HashSet<String>();  
	
	//ERROR CODES
	public static int DirError = 1;
	public static int mysqlError = 2;
	public static int missingFileType = 3;
	public static int incompatibleOptions = 4;
	
	public static void printError(int err) {
		
		switch (err) {
			case 1:	System.err.println("\nError: -d '" + GLOBALS.srcDir + "' is not a directory.\n"); break;
			case 2:	System.err.println("\nError: You didn't specify a MySQL database name.\n"); break;
			case 3: System.err.println("\nError: The -d option requires either the -protXML or -pepXML option\n"); break;
			case 4: System.err.println("\nError: You can't load data into MySQL AND get the spectral count. Choose one.\n"); break;
			default: System.err.println("Undefined error."); break;
		}
		
		System.exit(-1000);
	}
	
	
	
	// Function assigns command line arguments to global variables
	public static void parseCommandLineArgs(String[] argv) {
		for(int i = 0; i <= (argv.length - 1); i++) {
			if(argv[i].equals("-d")) GLOBALS.srcDir = argv[++i];
			else if(argv[i].equals("-mysql")) { GLOBALS.DBname = argv[++i]; GLOBALS.mysql = true; }
			else if(argv[i].equals("-protXML")) GLOBALS.fileType = "prot.xml";
			else if(argv[i].equals("-pepXML")) GLOBALS.fileType = "pep.xml";
			else if(argv[i].equals("-sc")) GLOBALS.getSpecCount = true;
		}
		
		if( GLOBALS.srcDir == null ) printError(GLOBALS.DirError);
		
		if( (GLOBALS.fileType == null) && (GLOBALS.getSpecCount == false) )
			printError(GLOBALS.missingFileType);
		
		if( (GLOBALS.mysql == true) && (GLOBALS.getSpecCount == true) )
			printError(GLOBALS.incompatibleOptions);
		
	}



	public static void printUsage() {
		System.err.print("\nUSAGE: java -jar tppXMLparser.jar -d <path> [other options]\n");
		System.err.printf("%5s %-20s path to files\n", "", "-d <path>");
		System.err.printf("%5s %-20s parse ONLY protXML files\n", "", "-protXML");
		System.err.printf("%5s %-20s parse ONLY pepXML files\n", "", "-pepXML");
		//System.err.printf("%5s %-20s parse BOTH XML file types and return spectral count table\n", "", "-sc");
		
		//System.err.printf("%5s %-20s name to give SQLite3 database to parse data into\n", "", "-sqlite3db <DBname>");
		System.err.printf("%5s %-20s name of MySQL database to put data into\n%26s (modify 'dbResources.properties' file accordingly)\n", "", "-mysql <DBname>", "");
		
		
		System.err.print("\n\n");
	}

}