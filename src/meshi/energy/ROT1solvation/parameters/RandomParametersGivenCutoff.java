package meshi.energy.ROT1solvation.parameters;

public class RandomParametersGivenCutoff extends AbstractROT1Parameters {

	private double cutoff = -999;
	
	public RandomParametersGivenCutoff(String path, String cutoffString) {
		super(path + "/RANDOM_" + cutoffString + ".txt");
		cutoff = new Double(cutoffString);
	}
	
	protected double cutoff() {
		return cutoff;
	}
	
	protected double sigmoidRange() {
		return 0.1;
	}
	
}
