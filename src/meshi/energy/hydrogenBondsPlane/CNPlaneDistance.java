package meshi.energy.hydrogenBondsPlane;

import meshi.geometry.Distance;
import meshi.molecularElements.Atom;

class CNPlaneDistance {
	private final double MINdis;
	private final double MAXdis;
	private final double slope;
	private final double leftLineBound;
	private final double rightLineBound;
	private double distanceVal, dDistanceDx, dDistanceDy, dDistanceDz;
private Atom atom1, atom2;
private double energy;
double deDx1;
	double deDy1;
	double deDz1;
	double deDx2;
	double deDy2;
	double deDz2;
private int swapFactor;

 CNPlaneDistance(double MINdis, double MAXdis, double slope) {
    this.MINdis = MINdis;
    this.MAXdis = MAXdis;
    this.slope = slope;
    leftLineBound = MINdis+slope;
    rightLineBound = MAXdis-slope;
 }

 void set(Atom atom1, Atom atom2, Distance distance, int swapFactor) {
   this.atom1 = atom1;
   this.atom2 = atom2;
   this.swapFactor = swapFactor;
   distanceVal = distance.distance();
   dDistanceDx =  distance.dDistanceDx();
   dDistanceDy = distance.dDistanceDy();
   dDistanceDz = distance.dDistanceDz();
 }

    /**
    * energy and dirivarives calculation.
    **/
    double  updateEnergy() {
    double d, d2, de;

    if (distanceVal <= MINdis || distanceVal >= MAXdis) {
            energy = 0;
            deDx1 = deDy1 = deDz1 = 0;
            deDx2 = deDy2 = deDz2 = 0;
            return energy;
    }
	 double b;
	 double a;
	 double k = 1;
	 if (distanceVal < leftLineBound)  {
       a = -2./(slope*slope*slope);
       b = 3./(slope*slope);
       d = distanceVal - MINdis;
       d2 = d*d;
       energy = k *(a *d2*d+ b *d2);
       de = k *(3* a *d2+2* b *d);
   }
   else
       if (distanceVal > rightLineBound) {
       a = 2./(slope*slope*slope);
       b = -3./(slope*slope);
       d = distanceVal-rightLineBound;
       d2 = d*d;
       energy = k *(a *d2*d+ b *d2+1);
       de = k *(3* a *d2+2* b *d);
       }
       else {
          energy = k;
          de = 0;
       }
   de = de*swapFactor;
   deDx1 = de*dDistanceDx;
   deDy1 = de*dDistanceDy;
   deDz1 = de*dDistanceDz;
   deDx2 = -deDx1;
   deDy2 = -deDy1;
   deDz2 = -deDz1;
 return energy;
 }

 protected Atom A() {return atom1;}
 protected Atom B() {return atom2;}
 protected double distance(){return distanceVal;}
 protected double energy(){return energy;}

 public String toString() {
       return "\nDistanceElement:\n"+atom1+ "\n"+atom2+"\nenergy = "+energy+
       "\ndistance = "+distanceVal;
   }
}
