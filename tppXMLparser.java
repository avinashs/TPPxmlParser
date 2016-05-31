package avi;

import java.io.*;
import java.util.Calendar;
import java.util.ResourceBundle;

import javax.xml.stream.*;
import java.sql.*;



public class tppXMLparser {
	
	public void parseXMLDocument(String xmlFile) {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		
		String dataType = null; // pepXML or protXML
		InputStream input = null;
		
		try {
			String file_path = "";
			file_path = GLOBALS.srcDir + GLOBALS.fileSepChar + xmlFile;
			
			input = new FileInputStream( new File(file_path) );
			
		} catch (FileNotFoundException e) {
			System.err.print("\nException getting input XML file.\n");
			e.printStackTrace();
		}
		
		XMLStreamReader xmlStreamReader = null;
		try {
			xmlStreamReader = inputFactory.createXMLStreamReader( input );
		} catch (XMLStreamException e) {
			System.err.print("\nException getting xmlStreamReader object.\n");
			e.printStackTrace();
		}
		
		dataType = getDataType(xmlStreamReader, xmlFile);
		
		if(dataType == null) {
			System.err.printf("\nERROR: '" + xmlFile + "' is not a TPP XML results file. Exiting now.\n");
			System.exit(-1);
		}
		
		
		// Based on dataType, call the appropriate function
		if(dataType.equals("pepXML")) parsePepXML( xmlStreamReader, xmlFile );
		if(dataType.equals("protXML")) parseProtXML( xmlStreamReader, xmlFile );
		
		try {
			xmlStreamReader.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	/*
	 *  Function parses protXML files
	 */
	public void parseProtXML(XMLStreamReader xmlStreamReader, String xmlFile) {
		protXML curGroup  = null;   // current protein group
		String curProtid_ = null;   // need this to get protein description
		String curPep_    = null;   // need this to annotate any AA modifications
		
		int ctr = 1; //used to provided job status reports
		
		if( GLOBALS.printHeader && !GLOBALS.mysql ) { 
			printHeader_protXML();
			GLOBALS.printHeader = false;
		}
		
		try {
			while( xmlStreamReader.hasNext() ) {
				int event = xmlStreamReader.next();
				
				if(event == XMLStreamConstants.START_ELEMENT) { // beginning of new element
					String elementName = xmlStreamReader.getLocalName();
					
					if(elementName.equals("protein_group")) { // beginning of new protein group
						curGroup = new protXML(xmlFile);
						curGroup.parse_protGroup_line(xmlStreamReader);
					}
					
					else if( elementName.equals("protein") )
						curProtid_ = curGroup.parse_protein_line(xmlStreamReader); 
						
					else if( elementName.equals("annotation") ) {
						
						for(int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
							String n = xmlStreamReader.getAttributeLocalName(i);
							String v = xmlStreamReader.getAttributeValue(i);
							if(n.equals("protein_description")) { 
								curGroup.setProtId(v, curProtid_);
								curProtid_ = null;
								break; 
							}
							v = null;
							n = null;
						}
					}
					
					else if( elementName.equals("indistinguishable_protein") ) 
						curProtid_ = curGroup.parse_protein_line(xmlStreamReader);
					
					else if( elementName.equals("peptide") )  // beginning of peptide record
						curPep_ = curGroup.parse_peptide_line(xmlStreamReader);
					
					else if( elementName.equals("mod_aminoacid_mass"))
						curGroup.record_AA_mod_protXML(xmlStreamReader, curPep_);
					
				}
				
				if(event == XMLStreamReader.END_ELEMENT) { // end of a record
					String elementName = xmlStreamReader.getLocalName();
					
					if( elementName.equals("peptide") ) {
						curGroup.annotate_modPeptide_protXML(curPep_);
						curPep_ = null;
					}
					
					else if( elementName.equals("protein") ) { // end of current protein
						if( GLOBALS.mysql || GLOBALS.getSpecCount ) curGroup.write_to_mysql();
						else curGroup.print_tab_delimited();
						
						curGroup.clear_variables();
						curProtid_ = null;
						
					}
					
					else if( elementName.equals("protein_group") ) { // end of protein group
						
						if( GLOBALS.mysql || GLOBALS.getSpecCount )curGroup.write_to_mysql();
						else curGroup.print_tab_delimited();
						
						curGroup.clear_variables();
						curGroup = null;
						curProtid_ = null;
						ctr++;
						if( (ctr%10) == 0 ) System.err.print(".");
						if( (ctr%800) == 0 ) System.err.print("[ " + ctr + " ]\n");
					}
				}
			}
			System.err.print("\n\n");  // this just makes for pretty output
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	
	
	// Prints header line for protXML files
	private void printHeader_protXML() {
		System.out.print("srcFile\t");
		System.out.print("groupId\t");
		System.out.print("siblingGroup\t");
		System.out.print("Pw\t");
		System.out.print("localPw\t");
		System.out.print("protId\t");
		System.out.print("peptide\t");
		System.out.print("modPeptide\t");
		System.out.print("charge\t");
		System.out.print("molwt\t");
		System.out.print("iniProb\t");
		System.out.print("nsp\t");
		System.out.print("wt\t");
		System.out.print("grpWt\t");
		System.out.print("ntt\t");
		System.out.print("nspecs\t");
		
		System.out.print("defline\n");
	}


	/*
	 *  Function written to parse pepXML files
	 */
	public void parsePepXML(XMLStreamReader xmlStreamReader, String xmlFile) {
		
		pepXML curPSM = null;  // current peptide-to-specturm match
		
		try {
			while( xmlStreamReader.hasNext() ) {
				int event = xmlStreamReader.next();
				
				if(event == XMLStreamConstants.START_ELEMENT) { //beginning of new element
					String elementName = xmlStreamReader.getLocalName();
					
					if(elementName.equals("peptideprophet_summary")) xmlStreamReader.next();
					
					else if(elementName.equals("spectrum_query")) { // new peptide record starts
						curPSM = new pepXML(xmlFile);
						curPSM.parse_pepXML_line(xmlStreamReader);
					}
					
					if(elementName.equals("search_hit")) curPSM.parse_pepXML_line(xmlStreamReader);
					
					if(elementName.equals("mod_aminoacid_mass")) curPSM.record_AA_mod(xmlStreamReader);
					
					if(elementName.equals("search_score")) curPSM.parse_search_score_line(xmlStreamReader);
					
					if(elementName.equals("peptideprophet_result")) curPSM.record_iniProb(xmlStreamReader);
				}
				else if(event == XMLStreamConstants.END_ELEMENT) { // end of element
					String elementName = xmlStreamReader.getLocalName();
					
					if(elementName.equals("spectrum_query")) { // end of peptide record
						curPSM.annotate_modPeptide();
						
						if(GLOBALS.mysql) curPSM.write_to_mysql();
						else curPSM.print_tab_delimited();
						curPSM = null;
					}
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
	}


	/*
	 * Function runs through given file to determine if its a pepXML or protXML file
	 */
	private String getDataType(XMLStreamReader xmlStreamReader, String xmlFile) {
		String ret = null;
		
		try {
			while( xmlStreamReader.hasNext() ) {
				int event = xmlStreamReader.next(); //get type of next event in file
				
				if(event == XMLStreamConstants.START_ELEMENT) { //beginning of new element
					String elementName = xmlStreamReader.getLocalName();
					if(elementName.equals("peptideprophet_summary")) {
						ret = "pepXML";
						if(GLOBALS.fileType == null) GLOBALS.fileType = "pep.xml";
						System.err.println("Processing " + xmlFile + " (" + GLOBALS.fileType + ")");
						break;
					}
					if(elementName.equals("protein_summary")) {
						ret = "protXML";
						if(GLOBALS.fileType == null) GLOBALS.fileType = "pep.xml";
						System.err.println("Processing " + xmlFile + " (" + GLOBALS.fileType + ")");
						break;
					}
				}
			}
		} catch (XMLStreamException e) {
			System.err.print("\nException iterating through xmlStreamReader.hasNext() in getDataType().\n");
			e.printStackTrace();
		}
		
		return ret;
	}



	/*
	 *  Function creates pepXML table on mysql server
	 */
	private static void create_mysql_table_pepXML() {
		
		Statement stmt;
		String query1, query2 = null;
		String dbURL = null; 
		Connection con = null;
		
		
		//construct the mysql command that will import the pepXML table data
		String tableFile = null;
		String td = null;
		String os = System.getProperty("os.name");
		
		if(os.contains("Windows")) {
			td = "C:\\\\tmp";
			File tmpDir = new File(td);
			if( !tmpDir.exists() ) tmpDir.mkdir();
			tableFile = "" + td + "\\\\pepXML-" + GLOBALS.timestamp + ".table.txt";
		}
		else {
			td = "/tmp";
			tableFile = "" + td + "/pepXML-" + GLOBALS.timestamp + ".table.txt";
		}
		
		//Parse out data in the dbResource.properties file
		ResourceBundle bundle = ResourceBundle.getBundle("dbResources");
		
		dbURL = "jdbc:mysql://" + 
				bundle.getString("host") + 
				":" + bundle.getString("port") +
				"/" + GLOBALS.DBname;
		
		
		query1 = "CREATE TABLE pepXML( " +
				"  srcFile VARCHAR(200)," +
				"  specId VARCHAR(250)," +
				"  mass DECIMAL(20,10)," +
				"  charge INT(5)," +
				"  peptide VARCHAR(250)," +
				"  modPeptide VARCHAR(250)," +
				"  prevAA VARCHAR(2)," +
				"  nextAA VARCHAR(2), ";
		
		if( GLOBALS.searchEngineSet.contains("XTANDEM") ) {
			query1 += "  hyperscore DECIMAL(10,5)," +
					  "  nextscore DECIMAL(10,5)," +
					  "  xtandem_expect DECIMAL(20,10),";
		}
		
		if( GLOBALS.searchEngineSet.contains("MASCOT") ) {
			query1 += "  mascot_ionscore DECIMAL(15,5), " +
					  "  mascot_identityscore DECIMAL(15,5), " +
					  "  mascot_star INT(10), " +
					  "  mascot_homologyscore DECIMAL(15,5), " +
					  "  mascot_expect DECIMAL(20,10), ";
		}
		
		if( GLOBALS.searchEngineSet.contains("SEQUEST") ) {
			query1 += "  sequest_xcorr DECIMAL(15,5), " +
					  "  sequest_deltacn DECIMAL(15,5), " +
					  "  sequest_deltacnstart DECIMAL(15,5), " +
					  "  sequest_spscore DECIMAL(15,5), " +
					  "  sequest_sprank DECIMAL(15,5), ";
		}
		
		query1 += "  iniProb DECIMAL(8,6), " +
				"  INDEX(specId), " +
				"  INDEX(peptide), " +
				"  INDEX(modPeptide), " +
				"  INDEX(modPeptide, charge) " +
				")ENGINE=MyISAM ";
	
		try {
			Class.forName(bundle.getString("Driver"));
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			con = DriverManager.getConnection(dbURL, bundle.getString("dbuser"), bundle.getString("dbpasswd"));
			stmt = con.createStatement();
			
			stmt.execute("DROP TABLE IF EXISTS pepXML");
			stmt.execute(query1);
			
			query2 = "LOAD DATA LOCAL INFILE '" + tableFile + "' INTO TABLE pepXML";
			
			stmt.execute("LOCK TABLES pepXML WRITE");
			stmt.execute(query2);
			stmt.execute("UNLOCK TABLES");
			
		} catch (SQLException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
		
		
		// now delete tableFile
		File f = new File(tableFile);
		if(!f.exists())
			throw new IllegalArgumentException("Delete: '" + tableFile + "': no such file or directory");
		
		if(!f.canWrite()) 
			throw new IllegalArgumentException("File: '" + tableFile + "' is write protected.");
		
		f.deleteOnExit();
	}

	

	/*
	 *  Function creates protXML table on mysql server
	 */
	private static void create_mysql_table_protXML() {
		
		Statement stmt;
		String query1, query2 = null;
		String dbURL = null; 
		Connection con = null;
		
		//construct the mysql command that will import the pepXML table data
		String tableFile = null;
		String os = System.getProperty("os.name");
		String td = null;
		
		// this is easier than dealing with windows path issues
		if(os.contains("Windows")) {
			td = "C:\\\\tmp";
			File tmpDir = new File(td);
			if( !tmpDir.exists() ) tmpDir.mkdir();
			tableFile = "" + td + "\\\\protXML-" + GLOBALS.timestamp + ".table.txt";
		}
		else {
			td = "/tmp";
			tableFile = "" + td + "/protXML-" + GLOBALS.timestamp + ".table.txt";
		}
		
			
		//Parse out data in the dbResource.properties file
		ResourceBundle bundle = ResourceBundle.getBundle("dbResources");
		
		dbURL = "jdbc:mysql://" + 
				bundle.getString("host") + 
				":" + bundle.getString("port") +
				"/" + GLOBALS.DBname;
		
		
		query1 = "CREATE TABLE RAWprotXML( " +
				"  srcFile VARCHAR(200)," +
				"  groupid INT(10)," +
				"  siblingGroup VARCHAR(5)," +
				"  Pw DECIMAL(8,6)," +
				"  localPw DECIMAL(8,6)," +
				"  protId VARCHAR(250)," +
				"  peptide VARCHAR(250)," +
				"  modPeptide VARCHAR(250)," +
				"  charge INT(10)," +
				"  molwt DECIMAL(20,10)," +
				"  iniProb DECIMAL(8,6)," +
				"  nsp DECIMAL(8,6)," +
				"  wt DECIMAL(8,6)," +
				"  grpWt DECIMAL(8,6)," +
				"  ntt INT(10)," +
				"  nspec INT(10)," +
				"  defline TEXT," +

				"  INDEX(srcfile), " +
				"  INDEX(groupid, siblingGroup)," +
				"  INDEX(srcfile, groupid, siblingGroup)," +
				"  INDEX(peptide)," +
				"  INDEX(modPeptide)," +
				"  INDEX(modPeptide, charge)" +
				")ENGINE=MyISAM ";
		
	
		try {
			System.err.println("\n"+dbURL+"\n");
			String driverName = bundle.getString("Driver");
			Class.forName(driverName);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			con = DriverManager.getConnection(dbURL, bundle.getString("dbuser"), bundle.getString("dbpasswd"));
			stmt = con.createStatement();
			
			stmt.execute("DROP TABLE IF EXISTS RAWprotXML");
			stmt.execute(query1);
			
			query2 = "LOAD DATA LOCAL INFILE '" + tableFile + "' INTO TABLE RAWprotXML";
			
			stmt.execute("LOCK TABLES RAWprotXML WRITE");
			stmt.execute(query2);
			stmt.execute("UNLOCK TABLES");
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		
		// now delete tableFile
		File f = new File(tableFile);
		if(!f.exists())
			throw new IllegalArgumentException("Delete: '" + tableFile + "': no such file or directory");
		
		if(!f.canWrite()) 
			throw new IllegalArgumentException("File: '" + tableFile + "' is write protected.");
		
		f.deleteOnExit();
	}

	
	
	/*
	 *  Function parses files into MySQL database
	 */
	private static void load_mysql() {
		
		// iterate over each file and parse it accordingly
		for(int i = 0; i < GLOBALS.xmlFiles.length; i++) {
			tppXMLparser xmlParser = new tppXMLparser();
			xmlParser.parseXMLDocument( GLOBALS.xmlFiles[i] );
		}
		
		System.err.println("Loading MySQL DB: " + GLOBALS.DBname);
		if(GLOBALS.fileType.equals("pep.xml")) create_mysql_table_pepXML();
		if(GLOBALS.fileType.equals("prot.xml")) create_mysql_table_protXML();
	}

	
	/*
	 * Function prints data to STDOUT in tab delimited form
	 */
	private static void print_to_stdout() {
		// iterate over each file and parse it accordingly
		for(int i = 0; i < GLOBALS.xmlFiles.length; i++) {
			tppXMLparser xmlParser = new tppXMLparser();
			xmlParser.parseXMLDocument( GLOBALS.xmlFiles[i] );
		}
	}

	
	
	
	
/******************************************************************************
 * 
 *                                Main function
 * 
******************************************************************************/

	public static void main(String[] argv) {

		if(argv.length < 1) {
			GLOBALS.printUsage();
			System.exit(-1);
		}
		
		GLOBALS.parseCommandLineArgs(argv);
		File dir = null;
		
		dir = new File(GLOBALS.srcDir);
		
		if( !dir.isDirectory() ) { //verify that user input is a valid directory
			GLOBALS.printError(GLOBALS.DirError);
		}
		
		// Filter file list to only consider GLOBALS.fileType files
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(GLOBALS.fileType);
			}
		};
		GLOBALS.xmlFiles = dir.list(filter);

		// Get current time to use as a time stamp for output files (if necessary)
		Calendar rightNow = Calendar.getInstance();
		GLOBALS.timestamp = "" + rightNow.get(Calendar.YEAR)
						  + (rightNow.get(Calendar.MONTH) + 1)
						  + rightNow.get(Calendar.DAY_OF_MONTH)
						  + rightNow.get(Calendar.HOUR_OF_DAY)
						  + rightNow.get(Calendar.MINUTE)
						  + rightNow.get(Calendar.SECOND);
		rightNow = null;

		if( GLOBALS.mysql) load_mysql(); // This function loads data into MySQL database
		else print_to_stdout();          // Default action, print data to STDOUT

	}
}

