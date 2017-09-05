package meshi.molecularElements.hydrogenBonds.newParameters;

public class DahiyatHighAccuracyAngleParamaters extends
		DahiyatImplementationConstants {
	
	protected double sigmoidBeginsAngleCenterOnH() {
		double sigmoidBeginsAngleCenterOnH = 90.0;
		return sigmoidBeginsAngleCenterOnH;
	}

	protected double sigmoidBeginsNoH() {
		double sigmoidBeginsNoH = 90.0;
		return sigmoidBeginsNoH;
	}

	protected double sigmoidEndsAngleCenterOnH() {
		double sigmoidEndsAngleCenterOnH = 150.0;
		return sigmoidEndsAngleCenterOnH;
	}

	protected double sigmoidEndsNoH() {
		double sigmoidEndsNoH = 110.0;
		return sigmoidEndsNoH;
	}

	protected double continueAfterSigmoid() {
		double continueAfterSigmoid = 0.6;
		return continueAfterSigmoid;
	}
	
	protected double valAtp1() {
		double valAtp1 = 0.98;
		return valAtp1;
	}
	
	protected double valAtp2() {
		double valAtp2 = 0.04;
		return valAtp2;
	}	
	
	
}
