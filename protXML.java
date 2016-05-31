package avi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;


/*
 * Class defining the protXML object
*/
public class protXML {
	private String srcFile;
	private int groupid;
	private double Pw;
	private double localPw;
	private String siblingGroup;
	private HashMap<String, String> protIds; // holds protIds and deflines
	private HashMap<String, pepXML> peptides; //holds modPeps and their pepXML objects
	
	public protXML() {}; // default constructor
	
	public protXML(String txt) { 
		srcFile = txt; 
		this.protIds = new HashMap<String, String>();
		this.peptides = new HashMap<String, pepXML>();
	}
	
	
	// public GET functions
	public int getGroupId() { return groupid; }
	public double getPw() { return Pw; }
	public double getLocalPw() { return localPw; }
	public String getSiblingGroup() { return siblingGroup; }
	
	/*
	 *  Function to parse protXML file lines
	 */
	public void parse_protGroup_line(XMLStreamReader xmlStreamReader) {
		String attrName  = null;
		String attrValue = null;
		
		for(int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
			attrName  = xmlStreamReader.getAttributeLocalName(i);
			attrValue = xmlStreamReader.getAttributeValue(i);
			
			if(attrName.equals("group_number")) this.groupid = Integer.parseInt(attrValue);
			if(attrName.equals("probability")) this.Pw = Double.parseDouble(attrValue);
		}
	}

	
	/*
	 *  Function to parse protein line in protXML file
	 */
	public String parse_protein_line(XMLStreamReader xmlStreamReader) {
		String attrName  = null;
		String attrValue = null;
		String protid_  = null;
		
		for(int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
			attrName  = xmlStreamReader.getAttributeLocalName(i);
			attrValue = xmlStreamReader.getAttributeValue(i);
			
			if(attrName.equals("protein_name")) protid_ = attrValue;
			if(attrName.equals("probability")) this.localPw = Double.parseDouble(attrValue);
			if(attrName.equals("group_sibling_id")) this.siblingGroup = attrValue;
		}
		
		return(protid_);
	}

	/*
	 *  Function records the current protein ID and it's description
	 */
	public void setProtId(String defline, String protid_) {
		
		if(defline.length() <= 2) this.protIds.put(protid_, "No Description");
		else this.protIds.put(protid_, defline);
	}

	
	/*
	 *  Function records the current peptide into protXML object
	 */
	public String parse_peptide_line(XMLStreamReader xmlStreamReader) {
		pepXML curPep = null;
		curPep = new pepXML();
		String attrName  = null;
		String attrValue = null;
		
		for(int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
			attrName  = xmlStreamReader.getAttributeLocalName(i);
			attrValue = xmlStreamReader.getAttributeValue(i);
			
			if(attrName.equals("peptide_sequence")) curPep.setPeptide(attrValue);
			if(attrName.equals("charge")) curPep.setCharge(attrValue);
			if(attrName.equals("initial_probability")) curPep.setIniProb(attrValue);
			if(attrName.equals("nsp_adjusted_probability")) curPep.setNSP(attrValue);
			if(attrName.equals("weight")) curPep.setWt(attrValue);
			if(attrName.equals("group_weight")) curPep.setGrpWt(attrValue);
			if(attrName.equals("n_enzymatic_termini")) curPep.setNTT(attrValue);
			if(attrName.equals("n_instances")) curPep.setNspecs(attrValue);
			if(attrName.equals("calc_neutral_pep_mass")) curPep.setMass(attrValue);
		}
		
		String k = null;
		k = "" + curPep.getPeptide() + 
		    "-" + curPep.getMass() + 
		    "-" + curPep.getCharge() +
		    "-" + curPep.getIniProb();
		
		peptides.put(k, curPep);
		curPep = null;
		
		return(k);
	}

	
	/*
	 * Function generates tab-delimited output for protein object data
	 */
	public void print_tab_delimited() {
		
		// First construct the string that will contain all the peptide information
		// for the protein entries currently in 'protIds'. All of these proteins
		// share the same of peptide evidence.
		
		Set<String> protKeys = this.protIds.keySet();
		Iterator<String> protIter = protKeys.iterator();
		
		String defline = "";
		String k = "";
				
		while(protIter.hasNext()) { // iterate over proteins
			k = (String) protIter.next();  //actually extracts the hash key object
			
			String protein_data = this.srcFile + "\t";
			protein_data += this.groupid + "\t";
			protein_data += this.siblingGroup + "\t";
			protein_data += this.Pw + "\t";
			protein_data += this.localPw + "\t";
			protein_data += k + "\t";
			
			defline = this.protIds.get(k);
			
			/*
			 *  This code has to be here to iterate over each peptide for every
			 *  protein in the protIds hashMap
			 */
			Set<String> pepKeys = peptides.keySet();
			Iterator<String> pepIter = pepKeys.iterator();
			String pepId = "";
			
			while(pepIter.hasNext()) { // iterate over peptides
				pepId = (String) pepIter.next();
				this.print_tab_delimited_peptide( pepId, protein_data, defline);
			}
			pepIter = null;
			
		}
	}

	
	/*
	 *  Function prints a tab-delimited string of peptide data
	 */
	private void print_tab_delimited_peptide(String pepId, String protein_data, String defline) {
		String peptide_data = "";
		
		pepXML curPep = null;
		curPep = this.peptides.get(pepId);
		
		peptide_data += curPep.getPeptide() + "\t";
		peptide_data += curPep.getModPeptide() + "\t";
		peptide_data += curPep.getCharge() + "\t";
		peptide_data += curPep.getMass() + "\t";
		peptide_data += curPep.getIniProb() + "\t";
		peptide_data += curPep.getNSP() + "\t";
		peptide_data += curPep.getWt() + "\t";
		peptide_data += curPep.getGrpWt() + "\t";
		peptide_data += curPep.getNTT() + "\t";
		peptide_data += curPep.getNspecs() + "\t";
		
		String final_line = protein_data + peptide_data + defline;
		
		System.out.println(final_line);
	}

	
	/*
	 *  Function returns tab-delimited string of peptide data
	 */
	public String get_tab_delimited_peptide(String pepId, String protein_data, String defline) {
		String peptide_data = "";
		
		pepXML curPep = null;
		curPep = this.peptides.get(pepId);
		
		peptide_data += curPep.getPeptide() + "\t";
		peptide_data += curPep.getModPeptide() + "\t";
		peptide_data += curPep.getCharge() + "\t";
		peptide_data += curPep.getMass() + "\t";
		peptide_data += curPep.getIniProb() + "\t";
		peptide_data += curPep.getNSP() + "\t";
		peptide_data += curPep.getWt() + "\t";
		peptide_data += curPep.getGrpWt() + "\t";
		peptide_data += curPep.getNTT() + "\t";
		peptide_data += curPep.getNspecs() + "\t";
		
		String final_line = protein_data + peptide_data + defline + "\n";
		return(final_line);
	}
	
	
	
	/*
	 *  Function clears out variables for next protein block in group
	 */
	public void clear_variables() {
		this.protIds.clear();
		this.peptides.clear();
		this.localPw = 0.0;
		this.siblingGroup = null;
	}

	
	
	/*
	 *  Function records the current peptide's modifications (if any)
	 */
	public void record_AA_mod_protXML(XMLStreamReader xmlStreamReader, String k) {
		pepXML curPep = peptides.get(k);
		curPep.record_AA_mod(xmlStreamReader);
		peptides.put(k, curPep);
		curPep = null;
	}

	
	/*
	 *  Function to annotate modPeptide string
	 */
	public void annotate_modPeptide_protXML(String k) {
		pepXML curPep = peptides.get(k);
		
		if(curPep.getModPeptide() == null) {
			curPep.annotate_modPeptide();
			peptides.put(k,curPep);
			curPep = null;
		}
	}

	
	/*
	 *  Function writes current protein group info to tab-delimited file for 
	 *  import into MySSQL database.
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
			fileName = "" + td + "\\\\protXML-" + GLOBALS.timestamp + ".table.txt";
		}
		else {
			td = "/tmp";
			fileName = "" + td + "/protXML-" + GLOBALS.timestamp + ".table.txt";
		}
		
		try{
			outFile = new FileWriter(fileName, true); // opens file for appending (if it exists)
			
			Set<String> protKeys = this.protIds.keySet();
			Iterator<String> protIter = protKeys.iterator();
			
			String defline = "";
			String k = "";
						
			while(protIter.hasNext()) {
				k = (String) protIter.next();
				String protein_data = this.srcFile + "\t";
				protein_data += this.groupid + "\t";
				protein_data += this.siblingGroup + "\t";
				protein_data += this.Pw + "\t";
				protein_data += this.localPw + "\t";
				protein_data += k + "\t";
				
				defline = this.protIds.get(k);
				
				/*
				 *  This code has to be here to iterate over each peptide for every
				 *  protein in the protIds hashMap
				 */
				Set<String> pepKeys = peptides.keySet();
				Iterator<String> pepIter = pepKeys.iterator();
				String pepId = "";
				String curLine = null;
				
				while(pepIter.hasNext()) { // iterate over peptides
					pepId = (String) pepIter.next();
					curLine = this.get_tab_delimited_peptide( pepId, protein_data, defline);
					outFile.append(curLine);
				}
				pepIter = null;
			}
			
			outFile.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
}