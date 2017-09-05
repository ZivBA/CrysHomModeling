package meshi.energy.ROT1solvation.parameters;

public class CBParametersGivenCutoff extends AbstractCBParameters {

	private double cutoff = -999;
	
	public CBParametersGivenCutoff(String path, String cutoffString) {
		super(path + "/CB_" + cutoffString + ".txt");
		cutoff = new Double(cutoffString);
	}
	
	protected double cutoff() {
		return cutoff;
	}
	
	protected double sigmoidRange() {
		return 0.1;
	}
	
}
