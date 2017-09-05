package alignment;

public class RvalPosition implements Position {

	private final double[] Rfits;
	private final int resType;
	private double gapOpeningScore =  -999999999.9;
	
	public RvalPosition(int resNum, int resType, double[] Rfits) {
		this.Rfits = Rfits;
		this.resType = resType;
	}

	@Override
	public double gapOpeningScore() {
		return gapOpeningScore;
	}

	public void setGapOpeningScore(double newScore) {
		gapOpeningScore = newScore;
	}
	
	@Override
	public double gapAligningScore() {
		return -0.0;
	}

	@Override
	public String string() {
		String aa = "ACDEFGHIKLMNPQRSTVWY";
		return aa.substring(resType, resType+1);
	}

	@Override
	public String gapString() {
		return "-";
	}
	
	public double fitToAA(int aaType) {
		return Rfits[aaType];
	}

}
