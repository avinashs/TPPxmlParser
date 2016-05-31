package avi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.stream.XMLStreamReader;

/*
 * Class defining the pepXML object
 */
public class pepXML {
	//private String searchEngine;   // stores the name/type of search tool used
	private String srcFile;
	private String specId;
	private double mass;
	private int charge;
	private String peptide;
	private char prevAA;
	private char nextAA;
	private String modPeptide;
	private double iniProb;
	private double wt; // variable used in protXML files
	private double grpWt; // variable used in protXML files
	private double nsp; // variable used in protXML files
	private int ntt; // variable used in protXML files
	private int nspecs; // variable used in protXML files

	private HashMap<Integer, Integer> aaMods; // holds the AA modification
											  // positions


	// Variables for XTANDEM search results
	private double hyperscore;
	private double nextscore;
	private double xtandem_expect;
	
	// Variables for MASCOT search results
	private double mascot_ionscore;
	private double mascot_identityscore;
	private int mascot_star;
	private double mascot_homologyscore;
	private double mascot_expect;
	
	// Variables for SEQUEST search results
	private double sequest_xcorr;
	private double sequest_deltacn;
	private double sequest_deltacnstar;
	private double sequest_spscore;
	private double sequest_sprank;

	public pepXML() {
	}; // default constructor

	public pepXML(String txt) {
		srcFile = txt;
	}

	// public SET functions (need them for parsing protXML files)
	public void setPeptide(String txt) {
		this.peptide = txt;
	}
	
	public void setCharge(String txt) {
		this.charge = Integer.parseInt(txt);
	}

	public void setIniProb(String txt) {
		this.iniProb = Double.parseDouble(txt);
	}

	public void setNSP(String txt) {
		this.nsp = Double.parseDouble(txt);
	}

	public void setWt(String txt) {
		this.wt = Double.parseDouble(txt);
	}

	public void setGrpWt(String txt) {
		this.grpWt = Double.parseDouble(txt);
	}

	public void setMass(String txt) {
		this.mass = Double.parseDouble(txt);
	}

	public void setNTT(String txt) {
		this.ntt = Integer.parseInt(txt);
	}

	public void setNspecs(String txt) {
		this.nspecs = Integer.parseInt(txt);
	}

	// public GET functions
	public String getSpecId() {
		return specId;
	}

	public double getMass() {
		return mass;
	}

	public int getCharge() {
		return charge;
	}

	public String getPeptide() {
		return peptide;
	}

	public char getPrevAA() {
		return prevAA;
	}

	public char getNextAA() {
		return nextAA;
	}

	public String getModPeptide() {
		return modPeptide;
	}

	public double getHyperscore() {
		return hyperscore;
	}

	public double getNextscore() {
		return nextscore;
	}

	public double getXtandem_expect() {
		return xtandem_expect;
	}

	public double getIniProb() {
		return iniProb;
	}

	public double getWt() {
		return wt;
	}

	public double getGrpWt() {
		return grpWt;
	}

	public double getNSP() {
		return nsp;
	}

	public int getNTT() {
		return ntt;
	}

	public int getNspecs() {
		return nspecs;
	}

	// MASCOT variables
	public double getMascot_ionscore() {
		return mascot_ionscore;
	}
	
	public double getMascot_identityscore() {
		return mascot_identityscore;
	}
	
	public int getMascot_star() {
		return mascot_star;
	}
	
	public double getMascot_homologyscore() {
		return mascot_homologyscore;
	}
	
	public double getMascot_expect() {
		return mascot_expect;
	}
	
	
	
	// SEQUEST variables
	public double getSequest_xcorr() {
		return sequest_xcorr;
	}
	
	public double getSequest_deltacn() {
		return sequest_deltacn;
	}
	
	public double getSequest_deltacnstar() {
		return sequest_deltacnstar;
	}
	
	public double getSequest_spscore() {
		return sequest_spscore;
	}
	
	public double getSequest_sprank() {
		return sequest_sprank;
	}
	
	
	
	/*
	 * Function parses the given XML stream and records the relevant information
	 * found in it.
	 */
	public void parse_pepXML_line(XMLStreamReader xmlStreamReader) {
		String attrName = null;
		String attrValue = null;

		for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
			attrName = xmlStreamReader.getAttributeLocalName(i);
			attrValue = xmlStreamReader.getAttributeValue(i);

			if (attrName.equals("spectrum"))
				this.specId = attrValue;
			if (attrName.equals("assumed_charge"))
				this.charge = Integer.parseInt(attrValue);
			if (attrName.equals("precursor_neutral_mass"))
				this.mass = Double.parseDouble(attrValue);

			if (attrName.equals("peptide"))
				this.peptide = attrValue;
			if (attrName.equals("peptide_prev_aa"))
				this.prevAA = attrValue.charAt(0);
			if (attrName.equals("peptide_next_aa"))
				this.nextAA = attrValue.charAt(0);

		}
	}

	/*
	 * Function parses amino acid modifications into aaMods variable
	 */
	public void record_AA_mod(XMLStreamReader xmlStreamReader) {
		String attrName = null;
		String attrValue = null;
		int k = -1;
		int v = 0;

		if (this.aaMods == null)
			this.aaMods = new HashMap<Integer, Integer>();

		for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
			attrName = xmlStreamReader.getAttributeLocalName(i);
			attrValue = xmlStreamReader.getAttributeValue(i);

			if (attrName.equals("position"))
				k = Integer.parseInt(attrValue) - 1;
			if (attrName.equals("mass")) {
				v = (int) Math.round(Double.parseDouble(attrValue));

				if (k > -1 && v > 0)
					this.aaMods.put(k, v);
				else {
					System.err.printf("\nERROR: mod_aminoacid_mass line pepXML::record_AA_mod()\n");
					System.err.println(this.specId + "\n");
					System.exit(-1);
				}
			}
		}
	}

	/*
	 * Function parses search_score lines
	 */
	public void parse_search_score_line(XMLStreamReader xmlStreamReader) {
		String attrValue = null;

		for (int i = 0, j = 1; i < xmlStreamReader.getAttributeCount(); i++, j++) {
			attrValue = xmlStreamReader.getAttributeValue(i);

			/*
			 *   X!Tandem search scores
			 */
			if (attrValue.equals("hyperscore")) {
				GLOBALS.searchEngineSet.add("XTANDEM");
				this.hyperscore = Double.parseDouble(xmlStreamReader
						.getAttributeValue(j));
			}
			if (attrValue.equals("nextscore"))
				this.nextscore = Double.parseDouble(xmlStreamReader
						.getAttributeValue(j));
			if (attrValue.equals("expect"))
				this.xtandem_expect = Double.parseDouble(xmlStreamReader
						.getAttributeValue(j));
			
			
			/*
			 *   Mascot search scores
			 */
			if (attrValue.equals("ionscore")) {
				GLOBALS.searchEngineSet.add("MASCOT");
				this.mascot_ionscore = Double.parseDouble(xmlStreamReader.
						getAttributeValue(j));
			}
			if (attrValue.equals("identityscore"))
				this.mascot_identityscore = Double.parseDouble(xmlStreamReader.
						getAttributeValue(j));
			if (attrValue.equals("star"))
				this.mascot_star = Integer.parseInt(xmlStreamReader.
						getAttributeValue(j));
			if (attrValue.equals("homologyscore")) 
				this.mascot_homologyscore = Double.parseDouble(xmlStreamReader.
						getAttributeValue(j));
			if (attrValue.equals("expect"))
				this.mascot_expect = Double.parseDouble(xmlStreamReader
						.getAttributeValue(j));
			
			
			/*
			 *   Sequest search scores
			 */
			if (attrValue.equals("xcorr")) {
				GLOBALS.searchEngineSet.add("SEQUEST");
				this.sequest_xcorr = Double.parseDouble(xmlStreamReader.
						getAttributeValue(j));
			}
			if (attrValue.equals("deltacn"))
				this.sequest_deltacn = Double.parseDouble(xmlStreamReader.
						getAttributeValue(j));
			if (attrValue.equals("deltacnstar"))
				this.sequest_deltacnstar = Double.parseDouble(xmlStreamReader.
						getAttributeValue(j));
			if (attrValue.equals("spscore"))
				this.sequest_spscore = Double.parseDouble(xmlStreamReader.
						getAttributeValue(j));
			if (attrValue.equals("sprank"))
				this.sequest_sprank = Double.parseDouble(xmlStreamReader.
						getAttributeValue(j));
		}
	}

	
	/*
	 * Function parses out peptide probability
	 */
	public void record_iniProb(XMLStreamReader xmlStreamReader) {
		String attrName = null;
		String attrValue = null;

		for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
			attrName = xmlStreamReader.getAttributeLocalName(i);
			attrValue = xmlStreamReader.getAttributeValue(i);

			if (attrName.equals("probability"))
				this.iniProb = Double.parseDouble(attrValue);
		}
	}

	/*
	 * Function annotates modPeptide
	 */
	public void annotate_modPeptide() {
		if (this.aaMods == null)
			this.modPeptide = this.peptide; // peptide has no modifications
		else {
			modPeptide = "";
			for (int i = 0; i < this.peptide.length(); i++) {
				this.modPeptide += this.peptide.charAt(i);
				if (this.aaMods.containsKey(i))
					this.modPeptide += "[" + this.aaMods.get(i) + "]";
			}
		}
	}

	

	// Prints header line for pepXML files
	private void printHeader_pepXML() {
		
		System.out.print("srcFile\t");
		System.out.print("specId\t");
		System.out.print("mass\t");
		System.out.print("peptide\t");
		System.out.print("modPeptide\t");
		System.out.print("prevAA\t");
		System.out.print("nextAA\t");
		System.out.print("charge\t");
		
		
		// For X!TANDEM results
		if( GLOBALS.searchEngineSet.contains("XTANDEM") ) {
			System.out.print("hyperscore\t");
			System.out.print("nextscore\t");
			System.out.print("xtandem_expect\t");
		}
		
		// For MASCOT results
		if( GLOBALS.searchEngineSet.contains("MASCOT") ) {
			System.out.print("mascot_ionscore\t");
			System.out.print("mascot_identityscore\t");
			System.out.print("mascot_star\t");
			System.out.print("mascot_homologyscore\t");
			System.out.print("mascot_expect\t");
		}
		
		// For SEQUEST results
		if( GLOBALS.searchEngineSet.contains("SEQUEST") ) {
			System.out.print("sequest_xcorr\t");
			System.out.print("sequest_deltacn\t");
			System.out.print("sequest_deltacnstar\t");
			System.out.print("sequest_spscore\t");
			System.out.print("sequest_sprank\t");
		};
		
		System.out.print("iniProb\n");
	}

	
	
	
	/*
	 * Function generates tab-delimited output for peptide object data.
	 */
	public void print_tab_delimited() {
		
		if( GLOBALS.printHeader && !GLOBALS.mysql ) { 
			printHeader_pepXML();
			GLOBALS.printHeader = false;
		}
		
		System.out.print(srcFile + "\t");
		System.out.print(specId + "\t");
		System.out.print(mass + "\t");
		System.out.print(peptide + "\t");
		System.out.print(modPeptide + "\t");
		System.out.print(prevAA + "\t");
		System.out.print(nextAA + "\t");
		System.out.print(charge + "\t");
		
		// For X!TANDEM results
		if( GLOBALS.searchEngineSet.contains("XTANDEM") ) {
			System.out.print(hyperscore + "\t");
			System.out.print(nextscore + "\t");
			System.out.print(xtandem_expect + "\t");
		}
		
		// For MASCOT results
		if( GLOBALS.searchEngineSet.contains("MASCOT") ) {
			System.out.print(mascot_ionscore + "\t");
			System.out.print(mascot_identityscore + "\t");
			System.out.print(mascot_star + "\t");
			System.out.print(mascot_homologyscore + "\t");
			System.out.print(mascot_expect + "\t");
		}
		
		// For SEQUEST results
		if( GLOBALS.searchEngineSet.contains("SEQUEST") ) {
			System.out.print(sequest_xcorr + "\t");
			System.out.print(sequest_deltacn + "\t");
			System.out.print(sequest_deltacnstar + "\t");
			System.out.print(sequest_spscore + "\t");
			System.out.print(sequest_sprank + "\t");
		}
		
		System.out.print(iniProb + "\n");
	}

	/*
	 * Function writes current peptide info to tab-delimited file for import
	 * into MySQL database.
	 */
	public void write_to_mysql() {

		FileWriter outFile = null;
		String fileName = null;

		String os = System.getProperty("os.name");
		String td = null;
		
		if(os.contains("Windows")) {
			td = "C:\\\\tmp";
			File tmpDir = new File(td);
			if( !tmpDir.exists() ) tmpDir.mkdir();
			fileName = "" + td + "\\\\pepXML-" + GLOBALS.timestamp + ".table.txt";
		}
		else {
			td = "/tmp";
			fileName = "" + td + "/pepXML-" + GLOBALS.timestamp + ".table.txt";
		}
		
		try {
			outFile = new FileWriter(fileName, true); // opens file for appending (if it exists)
			
			
			outFile.append(srcFile + "\t");
			outFile.append(specId + "\t");
			outFile.append(mass + "\t");
			outFile.append(charge + "\t");
			outFile.append(peptide + "\t");
			outFile.append(modPeptide + "\t");
			outFile.append(prevAA + "\t");
			outFile.append(nextAA + "\t");
			
			// For XTANDEM results
			if( GLOBALS.searchEngineSet.contains("XTANDEM") ) {
				outFile.append(hyperscore + "\t");
				outFile.append(nextscore + "\t");
				outFile.append(xtandem_expect + "\t");
			}
			
			// For MASCOT results
			if( GLOBALS.searchEngineSet.contains("MASCOT") ) {
				outFile.append(mascot_ionscore + "\t");
				outFile.append(mascot_identityscore + "\t");
				outFile.append(mascot_star + "\t");
				outFile.append(mascot_homologyscore + "\t");
				outFile.append(mascot_expect + "\t");
			}
			
			// For SEQUEST results
			if( GLOBALS.searchEngineSet.contains("SEQUEST") ) {
				outFile.append(sequest_xcorr + "\t");
				outFile.append(sequest_deltacn + "\t");
				outFile.append(sequest_deltacnstar + "\t");
				outFile.append(sequest_spscore + "\t");
				outFile.append(sequest_sprank + "\t");
			}
			
			outFile.append(iniProb + "\n");

			outFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
