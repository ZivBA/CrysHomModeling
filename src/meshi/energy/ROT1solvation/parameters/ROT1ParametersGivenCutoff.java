package meshi.energy.ROT1solvation.parameters;

public class ROT1ParametersGivenCutoff extends AbstractROT1Parameters {

	private double cutoff = -999;
	
	public ROT1ParametersGivenCutoff(String path, String cutoffString) {
		super(path + "/ROT1_" + cutoffString + ".txt");
		cutoff = new Double(cutoffString);
	}
	
	protected double cutoff() {
		return cutoff;
	}
	
	protected double sigmoidRange() {
		return 0.1;
	}
	
}
