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

public class Feature_PositionVerticaleCompriseEntre extends FeatureTypes {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String fname;

	int stateId;
	int numStates;
	String nameFeatures;
        int cinquieme;
        //on découpe la page en 10 parties
        int decoupage = 5;

	public Feature_PositionVerticaleCompriseEntre (FeatureGenImpl m, int cinquieme) {
		super(m);
		numStates = m.numStates();
		fname = "PositionVerticaleDansleDecile"+Integer.toString(cinquieme)+"deLaPage";
                this.cinquieme=  cinquieme;
	}

	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
                Document_PDF d = (Document_PDF) data;
                Chunk_PDF currChunk = (Chunk_PDF) d.x(pos);
                Float tailleQuartPage = (d.hauteurMoyennePages / this.decoupage) *1.0F;

                if ((currChunk.y1 > Math.round((this.cinquieme-1) * tailleQuartPage)) 
                        && currChunk.y2 <= Math.round(this.cinquieme*tailleQuartPage) ) {
                    stateId = 0;
                    return true;
                } 
                else{
                    stateId = numStates;
                    return false;
            }
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

