package org.latoe.layoutanalysis.pdf.labelisation.features;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.latoe.layoutanalysis.pdf.labelisation.ValueComparatorStringInt;
import org.latoe.layoutanalysis.pdf.pdfobject.Chunk_PDF;
import org.latoe.layoutanalysis.pdf.pdfobject.Document_PDF;
import org.latoe.layoutanalysis.pdf.pdfobject.Word_PDF;
import org.melodi.learning.iitb.CRF.DataSequence;
import org.melodi.learning.iitb.Model.FeatureGenImpl;
import org.melodi.learning.iitb.Model.FeatureImpl;
import org.melodi.learning.iitb.Model.FeatureTypes;

public class Feature_SymboleFirstWord extends FeatureTypes {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String fname;

	int stateId;
	int numStates;
	String nameFeatures;

	public Feature_SymboleFirstWord(FeatureGenImpl m) {
		super(m);
		numStates = m.numStates();
		fname = "CommenceParSymbole";
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		// System.err.print("startScanFeaturesAt()   ");
		Document_PDF d = (Document_PDF) data;
		Chunk_PDF currChunk = (Chunk_PDF) d.x(pos);


		if (contientSymboleFirstWord(currChunk)) {

			stateId = 0;
			return true;
		

		} else {
			stateId = numStates;
			return false;
		}
	}
	
	static boolean contientSymboleFirstWord(Chunk_PDF currChunk){
		
		List<Word_PDF> wordList = currChunk.mots;
		String toMatch = wordList.get(0).mot;
		
                // cherche les premiers mots d'un chunk qui contiennent un symbole en premier
                // éventuellement suivi d'un mot ex : 1,-,*,.,-Toto,*Toto,1Toto
		return toMatch.matches("[0-9\\-\\.\\*][a-zA-Z]*");
	}

	
	

	public void processDoc(Document_PDF d) {

	}

	@Override
	public boolean hasNext() {
		return (stateId < numStates);
	}

	@Override
	public void next(FeatureImpl f) {
		// On créé un nouveau feature "vide".
		// Ce nouveau feature vide va être créé
		// pour chacun des labels
		// et chacun avec une valeur de 1.0 au début
		setFeatureIdentifier(stateId, stateId, fname, f);

		f.yend = stateId;
		f.ystart = -1;
		f.val = 1;
		stateId++;
	}

}



