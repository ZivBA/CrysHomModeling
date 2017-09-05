package meshi.energy.ROT1solvation.parameters;

public class WorstParametersGivenCutoff extends AbstractROT1Parameters {

	private double cutoff = -999;
	
	public WorstParametersGivenCutoff(String path, String cutoffString) {
		super(path + "/WORST_" + cutoffString + ".txt");
		cutoff = new Double(cutoffString);
	}
	
	protected double cutoff() {
		return cutoff;
	}
	
	protected double sigmoidRange() {
		return 0.1;
	}
	
}
