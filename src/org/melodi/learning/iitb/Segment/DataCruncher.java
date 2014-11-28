/** DataCruncher.java
 *
 * @author Sunita Sarawagi
 * @since 1.0
 * @version 1.3
 */ 
package org.melodi.learning.iitb.Segment;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import org.melodi.learning.iitb.CRF.DataSequence;
import org.melodi.learning.iitb.CRF.Segmentation;

public class DataCruncher {

	/**
	 * 
	 * @param text 
	 * @param delimit A set of delimiters used by the Tokenizer.
	 * @param impDelimit Delimiters to be retained for tagging. 
	 * @return an Array of tokens.
	 */
	protected static String[] getTokenList(String text, String delimit,
			String impDelimit) {
		text = text.toLowerCase();
		StringTokenizer textTok = new StringTokenizer(text, delimit, true);
		//This allocates space for all tokens and delimiters, 
		//but will make a second pass through the String unnecessary.
		ArrayList<String> tokenList = new ArrayList<String>(textTok.countTokens());
		
		while (textTok.hasMoreTokens()) {
			String tokStr = textTok.nextToken();
			if (!delimit.contains(tokStr) || impDelimit.contains(tokStr)) {
				tokenList.add(tokStr);
			}
		}
		//Finally, the storage is trimmed to the actual size.
		return tokenList.toArray(new String[tokenList.size()]);
	}
    
	/**
	 * Reads a block of text ended by a blank line or the end of the file.
	 * The block contains lines of tokens with a label.
	 * @param numLabels The maximal number of labels expected
	 * @param tin 
	 * @param tagDelimit Separator between tokens and tag number
	 * @param delimit Used to define token boundaries
	 * @param impDelimit Delimiters to be retained for tagging
	 * @param t Stores the labels
	 * @param cArray Stores the tokens
	 * @return number of lines read
	 * @throws IOException
	 */
	public static int readRowVarCol(int numLabels, BufferedReader tin,
			String tagDelimit, String delimit, String impDelimit, int[] t,
			String[][] cArray) throws IOException {
		int ptr = 0;
		String line;
        while(true) {
            line = tin.readLine();
            StringTokenizer firstSplit=null;
            if (line!=null) {
                firstSplit=new StringTokenizer(line.toLowerCase(),tagDelimit);
            }
            if ((line==null) || (firstSplit.countTokens()<2)) {
                // Empty Line
                return ptr;
            }
            String w = firstSplit.nextToken();
            int label=Integer.parseInt(firstSplit.nextToken()); 
            t[ptr] = label;
            cArray[ptr++] = getTokenList(w,delimit,impDelimit);
        }
	}

    static int readRowFixedCol(int numLabels, BufferedReader tin, String tagDelimit, 
    		String delimit, String impDelimit, int[] t, String[][] cArray, int labels[])
    		throws IOException {
        String line=tin.readLine();
        if (line == null)
            return 0;
        StringTokenizer firstSplit=new StringTokenizer(line.toLowerCase(),tagDelimit,true);
        int ptr = 0;
        for (int i = 0; (i < labels.length) && firstSplit.hasMoreTokens(); i++) {
            int label = labels[i];
            String w = firstSplit.nextToken();
            if (tagDelimit.indexOf(w)!=-1) {
                continue;
            } else {
                if (firstSplit.hasMoreTokens())
                    // read past the delimiter.
                    firstSplit.nextToken();
            }
            if ((label > 0) && (label <= numLabels)) {
                t[ptr] = label;
                cArray[ptr++] = getTokenList(w,delimit,impDelimit);
            }
        }
        return ptr;
    }

    /**
     * Checks, if the data are available in fixed column format, or variable
     * column format.
     * @param numLabels The maximal number of labels expected
     * @param tin
     * @param tagDelimit A character as String that acts as a delimiter between tokens and label.
     * @return An array with labels if the data are in fixed column format, null otherwise.
     * @throws IOException
     */
	protected static int[] readHeaderInfo(int numLabels, BufferedReader tin,
			String tagDelimit) throws IOException {
		tin.mark(1000);
		String line = tin.readLine();
		if (line == null) {
			throw new IOException("Header row not present in tagged file");
		}
		if (!line.toLowerCase().startsWith("fixed-column-format")) {
			tin.reset();
			return null;
		}

		line = tin.readLine();
		Pattern delimitPattern = Pattern.compile(tagDelimit, Pattern.LITERAL);
		String[] parts = delimitPattern.split(line);
		int labels[] = new int[numLabels];
		
		for (int i = 0, size = parts.length; i < size; ++i) {
			labels[i] = Integer.parseInt(parts[i]);
		}

		return labels;
	}
	
	public static TrainData readTagged(int numLabels, String tfile,
			String rfile, String delimit, String tagDelimit, String impDelimit,
			LabelMap labelMap) {
		try {
			ArrayList<DCTrainRecord> td = new ArrayList<DCTrainRecord>();
			BufferedReader tin = new BufferedReader(new FileReader(tfile
					+ ".tagged"));
			BufferedReader rin = new BufferedReader(new FileReader(rfile
					+ ".raw"));
			boolean fixedColFormat = false;
			String rawLine;
			StringTokenizer rawTok;
			int t[] = new int[0];
			String cArray[][] = new String[0][0];
			int[] labels = null;
			// read list of columns in the header of the tag file
			labels = readHeaderInfo(numLabels, tin, tagDelimit);
			if (labels != null) {
				fixedColFormat = true;
			}
			while ((rawLine = rin.readLine()) != null) {
				rawTok = new StringTokenizer(rawLine, delimit, true);
				int len = rawTok.countTokens();
				if (len > t.length) {
					t = new int[len];
					cArray = new String[len][0];
				}
				int ptr = 0;
				if (fixedColFormat) {
					ptr = readRowFixedCol(numLabels, tin, tagDelimit, delimit,
							impDelimit, t, cArray, labels);
				} else {
					ptr = readRowVarCol(numLabels, tin, tagDelimit, delimit,
							impDelimit, t, cArray);
				}
				if (ptr == 0) {
					break;
				}
				int at[] = new int[ptr];
				String[][] c = new String[ptr][0];
				for (int i = 0; i < ptr; i++) {
					at[i] = labelMap.map(t[i]);
					c[i] = cArray[i];
				}
				td.add(new DCTrainRecord(at, c));
			}
			return new DCTrainData(td);
		} catch (IOException e) {
			System.err.println("I/O Error" + e);
			System.exit(-1);
		}
		return null;
	}
	
    public static void readRaw(Vector<String[]> data,String file,String delimit,String impDelimit) {
        try {
            BufferedReader rin=new BufferedReader(new FileReader(file+".raw"));
            String line;
            while((line=rin.readLine())!=null) {
                StringTokenizer tok=new StringTokenizer(line.toLowerCase(),delimit,true);
                String seq[]=new String[tok.countTokens()];
                int count=0;
                for(int i=0 ; i<seq.length ; i++) {
                    String tokStr=tok.nextToken();
                    if (delimit.indexOf(tokStr)==-1 || impDelimit.indexOf(tokStr)!=-1) {
                        seq[count++]=new String(tokStr);
                    } 
                }
                String aseq[]=new String[count];
                for(int i=0 ; i<count ; i++) {
                    aseq[i]=seq[i];
                }
                data.add(aseq);
            }
            rin.close();
        } catch(IOException e) {
            System.out.println("I/O Error"+e);
            System.exit(-1);
        }
    }

    /**
     * 
     * @param file
     * @param tagDelimit A character as String that acts as a delimiter between tokens and label.
     */
	public static void createRaw(String file, String tagDelimit) {
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new FileReader(file + ".tagged"));
			out = new PrintWriter(new FileOutputStream(file + ".raw"));
			String line;
			StringBuilder rawLine;
			rawLine = new StringBuilder(200);
            while((line=in.readLine())!=null) {
                StringTokenizer t=new StringTokenizer(line,tagDelimit);
                if(t.countTokens()<2) {
					out.println(rawLine);
					rawLine.setLength(0);
                } else {
                    rawLine.append(" ");
					rawLine.append(t.nextToken());
                }
            }
			out.println(rawLine);
		} catch (IOException e) {
			System.out.println("I/O Error" + e);
			System.exit(-1);
		} finally {
			if (in != null) {
				try { in.close();} catch (IOException e) {}
			}
			if (out != null) {
				out.close();
			}
		}
	}
}
