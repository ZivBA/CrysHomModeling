package meshi.energy.LennardJones;

import meshi.energy.NonBondedEnergyElement;
import meshi.geometry.Distance;
import meshi.geometry.DistanceMatrix;
import meshi.molecularElements.Atom;

public  class LennardJonesEnergyElement extends NonBondedEnergyElement {
	private Atom atom1, atom2;
	private double epsilon, sigma, sigma6, sigma6EpsilonFour, minusTwelveSigma6;
	private boolean frozen;
	private double dEdX;
	private double dEdY;
	private double dEdZ;
	private double energy;
	private double weight;
	private double breakEnergy;
	private double breakEnergy4;
	private double breakEnergySquare4;
	private double rMax;
	private Distance distance;
	private LennardJonesParametersList parametersList;
	
	private double dis = -1;
	
	// ------------------------------------------------------

	
	
	
	public  LennardJonesEnergyElement() {}
	public  LennardJonesEnergyElement(LennardJonesParametersList parametersList,
	                                  DistanceMatrix distanceMatrix, double weight){
		this.parametersList = parametersList;
		this.weight = weight;
		double MAX_ENERGY = 30;
		breakEnergy = MAX_ENERGY /3;
		breakEnergy4 = breakEnergy*4;
		breakEnergySquare4 =  breakEnergy4*breakEnergy;
		rMax = DistanceMatrix.rMax();
	}

	protected void setAtoms(){
		throw new RuntimeException("setAtoms() may not be used by LennardJonesEnergyElement for "+
		"efficiency.");
	}

	public void set(Object obj) {
		distance = (Distance) obj;
		atoms = distance.atoms();
		atom1 = distance.atom1();
		atom2 = distance.atom2();
		LennardJonesParameters parameters = (LennardJonesParameters) parametersList.parameters(distance);
		epsilon = parameters.epsilon;
		sigma = parameters.sigma;
		sigma6 = parameters.sigma6;
		sigma6EpsilonFour = parameters.sigma6EpsilonFour;
		minusTwelveSigma6 = parameters.minusTwelveSigma6;
	}        

	public double evaluate() {
		dis = distance.distance();
		if (dis>rMax)
			return 0.0;
		updateEnergy();
		if (((atom1.name().length()==1)  || atom1.name().equals("CA") || atom1.name().equals("CB")) &&
				(atom2.name().equals("CB") || atom2.name().equals("CA") || (atom2.name().length()==1))) {
		updateAtoms(weight);
		return (energy*weight);
		}
		else {
			updateAtoms(weight*1.0);
			return (energy*weight*1.0);			
		}
	}

	private double updateEnergy() {
		double rMaxMinusDis = rMax - dis;
		double invD = distance.invDistance();
		double invD2 = invD * invD;
		double invD6 = invD2 * invD2 * invD2;
		double invD7 = invD6 * invD;
		double invD12 = invD6 * invD6;
		double invD13 = invD12 * invD;
		double LJ = sigma6EpsilonFour * (sigma6 * invD12 - invD6);
		double dLJdD = sigma6EpsilonFour * (minusTwelveSigma6 * invD13 + 6 * invD7);

		//smooth high energies
		double dE1dD;
		double energy1;
		if (LJ >= breakEnergy) {
			double LJPlus = LJ + breakEnergy;
			energy1 = breakEnergy4* LJ / LJPlus -breakEnergy;
			dE1dD =breakEnergySquare4/(LJPlus * LJPlus)* dLJdD;
		}
		else {
			energy1 = LJ;
			dE1dD = dLJdD;
		}

		//quench to zero in rMax
		double rMaxMinusDisSquare = rMaxMinusDis * rMaxMinusDis;
		double ALPHA = 0.1;
		double rMaxMinusDisSquarePlusAlpha = rMaxMinusDisSquare + ALPHA;
		double rMaxMinusDisTimesAlpha = rMaxMinusDis * ALPHA;
		double rMaxMinusDisSquarePlusAlphaSquare = rMaxMinusDisSquarePlusAlpha * rMaxMinusDisSquarePlusAlpha;
		double contact = rMaxMinusDisSquare / rMaxMinusDisSquarePlusAlpha;
		double dCdD = -2 * rMaxMinusDisTimesAlpha / rMaxMinusDisSquarePlusAlphaSquare;
		energy = energy1 * contact;
		double dEdD = dE1dD * contact + dCdD * energy1;

		dEdX = dEdD *distance.dDistanceDx();
		dEdY = dEdD *distance.dDistanceDy();
		dEdZ = dEdD *distance.dDistanceDz();
		return energy;
	}        

	private void updateAtoms(double tmpW){
		if (! atom1.frozen()) {
			atom1.addToFx(-1*dEdX*tmpW); // force = -derivative   
			atom1.addToFy(-1*dEdY*tmpW);
			atom1.addToFz(-1*dEdZ*tmpW);
		}
		if (! atom2.frozen()) {
			atom2.addToFx(dEdX*tmpW);
			atom2.addToFy(dEdY*tmpW);
			atom2.addToFz(dEdZ*tmpW);
		}
	}

	public String toString() {
		if ((atom1 == null) & (atom2 == null)) return "LennardJonesEnergyElement - atoms not set yet";
		if ((atom1 == null) | (atom2 == null)) throw new RuntimeException("This is weird\n"+
				"atom1 = "+atom1+"\n"+
				"atom2 = "+atom2); 
		return ("LennardJonesEnergyElement sigma = "+sigma+" epsilon = "+epsilon+
				" rMax = "+rMax+"\n"+atom1.verbose()+"\n"+atom2.verbose());
	}

}
