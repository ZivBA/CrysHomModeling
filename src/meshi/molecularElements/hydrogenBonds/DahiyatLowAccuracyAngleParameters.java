package meshi.molecularElements.hydrogenBonds;

/**
 * 
 * This class is mainly good for side-chain modeling, where the rotamer approximation
 * compromises the angular accuracy of the HB.
 * 
 **/
public class DahiyatLowAccuracyAngleParameters extends
		DahiyatImplementationConstants {
	
	protected double sigmoidBeginsAngleCenterOnH() {
		double sigmoidBeginsAngleCenterOnH = 90.0;
		return sigmoidBeginsAngleCenterOnH;
	}

	protected double sigmoidBeginsNoH() {
		double sigmoidBeginsNoH = 80.0;
		return sigmoidBeginsNoH;
	}

	protected double sigmoidEndsAngleCenterOnH() {
		double sigmoidEndsAngleCenterOnH = 110.0;
		return sigmoidEndsAngleCenterOnH;
	}

	protected double sigmoidEndsNoH() {
		double sigmoidEndsNoH = 100.0;
		return sigmoidEndsNoH;
	}

}
