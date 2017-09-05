package alignment;

class AminoAcidSequence extends Sequence {
	
	public AminoAcidSequence(String seq) {
		for (int c=0 ; c<seq.length() ; c++) {
			double gapExtension = -0.0;
			double gapOpening = -99999999.9;
			add(new AminoAcidPosition(seq.charAt(c), gapOpening, gapExtension));
		}
	}

}
