package meshi.energy.ROT1solvation.parameters;

public class CentroidParametersGivenCutoff extends AbstractCBParameters {

	private double cutoff = -999;
	
	public CentroidParametersGivenCutoff(String path, String cutoffString) {
		super(path + "/CENTROID_" + cutoffString + ".txt");
		cutoff = new Double(cutoffString);
	}
	
	protected double cutoff() {
		return cutoff;
	}
	
	protected double sigmoidRange() {
		return 0.1;
	}
	
}