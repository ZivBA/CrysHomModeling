package meshi.util.crossLinking;

import meshi.util.file.File2StringArray;

/**
 * Simple reading of a sequence from a FASTA file. 
 * @author Nir 
 **/
class FastaSeq {

    private String fileName = null;
    private String title = null;
	private String seq = null;
	
	public FastaSeq(String fileName) {
    	this.fileName = fileName;
        String[] lines = File2StringArray.f2a(fileName);
        String title = null;
        String seq = "";
		for (String line : lines) {
			if (!line.startsWith("#")) {  // Skipping comments
				if (line.startsWith(">")) { // found a title
					title = line.substring(1).trim();
				} else {
					seq += line.trim();
				}
			}
		}
		this.title = title;
		this.seq = seq;
	}
	
	public String title() {
		return title;
	}

	public String seq() {
		return seq;
	}

	public String fileName() {
		return fileName;
	}	
}
