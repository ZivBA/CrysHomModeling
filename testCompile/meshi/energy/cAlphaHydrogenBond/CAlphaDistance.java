package meshi.energy.cAlphaHydrogenBond;

import meshi.geometry.Distance;
import meshi.molecularElements.Atom;

class CAlphaDistance {
private final double MINdis;
	private final double slope;
	private final double leftLineBound;
	private final double rightLineBound;
	private double distanceAB;
	private double dDistanceDx;
	private double dDistanceDy;
	private double dDistanceDz;
private Atom  A, B; 
private double energy;
double deDxA;
	double deDyA;
	double deDzA;
	double deDxB;
	double deDyB;
	double deDzB;


 public CAlphaDistance(double MINdis, double MAXdis, double slope) {
    this.MINdis = MINdis;
	 this.slope = slope;
    leftLineBound = MINdis+slope;
    rightLineBound = MAXdis-slope;    
  }

 void set(Atom A, Atom B, Distance distance) {
   this.A = A;
   this.B = B;
   distanceAB = distance.distance();
   dDistanceDx = distance.dDistanceDx();
   dDistanceDy = distance.dDistanceDy();
   dDistanceDz = distance.dDistanceDz();   
 }   
 
   /**
    * energy and dirivarives calculation.
    **/
   double  updateEnergy() {
    double d, d2;
	 double de;
	 double b;
	 double a;
	 double k = 1;
	 if (distanceAB < leftLineBound)  {
       a = -2/(slope*slope*slope);
       b = 3/(slope*slope); 
       d = distanceAB - MINdis;
       d2 = d*d;
       energy = k *(a *d2*d+ b *d2);
       de = k *(3* a *d2+2* b *d);
   }
   else
       if (distanceAB > rightLineBound) {
       a = 2/(slope*slope*slope);
       b = -3/(slope*slope); 
       d = distanceAB-rightLineBound;
       d2 = d*d;
       energy = k *(a *d2*d+ b *d2+1);
       de = k *(3* a *d2+2* b *d);
       }
       else {
           energy = k;
          de = 0;
       }
   deDxA = de *dDistanceDx;
   deDyA = de *dDistanceDy;
   deDzA = de *dDistanceDz;
   deDxB = -deDxA;
   deDyB = -deDyA;
   deDzB = -deDzA;      
 return energy;
 }
  
 protected Atom A() {return A;}
 protected Atom B() {return B;}   
 protected double distance(){return distanceAB;}
 protected double energy(){return energy;}
   
   public String toString() {
       return "CAlphaDistanceElement:\n"+A+ "\n"+B+"\nenergyAB = "+energy+
       "\ndistanceAB = "+distanceAB;       
   }
}
