package meshi.energy.hydrogenBondsPlane;

import meshi.geometry.DisposableAngle;
import meshi.molecularElements.Atom;

class CNPlaneAngle {
	private final double MINangle;
	private final double MAXangle;
	private final double slope;
	private final double leftLineBound;
	private final double rightLineBound;
private DisposableAngle angle;
private Atom  atom1, atom2, atom3;
private double energy;
double deDx1;
	double deDy1;
	double deDz1;
	double deDx2;
	double deDy2;
	double deDz2;
	double deDx3;
	double deDy3;
	double deDz3;

CNPlaneAngle(double MINangle, double MAXangle, double slope) {
    this.MINangle = MINangle;
    this.MAXangle = MAXangle;
    this.slope = slope;
    leftLineBound = MINangle+slope;
    rightLineBound = MAXangle-slope;
}


void set(Atom atom1, Atom atom2, Atom atom3, DisposableAngle angle) {
   this.atom1 = atom1;
   this.atom2 = atom2;
   this.atom3 = atom3;   
   this.angle = angle;   
 }   

  /**
    * energy and dirivarives calculation.
    **/
  double  updateEnergy() {
    double angleVal, ang, ang2, de;
    double a, b;
    
   angleVal = angle.angle();
	 double k = 1;
	 if ((Math.abs(angleVal) > MAXangle) || (Math.abs(angleVal) < MINangle)){
       energy = 0;
       de = 0;
   }
   else
   if (angleVal < leftLineBound)  {
       a = -2./(slope*slope*slope);
       b = 3./(slope*slope); 
       ang = angleVal - MINangle;
       ang2 = ang*ang;
       energy = k *(a*ang2*ang+b*ang2);
       de = k *(3*a*ang2+2*b*ang);
   }
   else
       if (angleVal > rightLineBound) {
       a = 2./(slope*slope*slope);
       b = -3./(slope*slope); 
       ang = angleVal - rightLineBound;
       ang2 = ang*ang;
       energy = k *(a*ang2*ang+b*ang2+1);
       de = k *(3*a*ang2+2*b*ang);
       }
       else {
          energy = k;
          de = 0;
       }
   
   deDx1 = de*angle.dangleDx1();
   deDy1 = de*angle.dangleDy1();
   deDz1 = de*angle.dangleDz1();

   deDx2 = de*angle.dangleDx2();
   deDy2 = de*angle.dangleDy2();
   deDz2 = de*angle.dangleDz2();

   deDx3 = de*angle.dangleDx3();
   deDy3 = de*angle.dangleDy3();
   deDz3 = de*angle.dangleDz3();
 return energy;
 }
 
 
 protected Atom atom1() {return atom1;}
 protected Atom atom2() {return atom2;}   
 protected Atom atom3() {return atom3;}   
 protected double angle(){return angle.angle();}
 double energy(){return energy;}
   
 public String toString() {
       return "\nCN AngleElement:\n"+atom1+ "\n"+atom2+ "\n"+atom3 +
       "\nenergy = " + energy +
       "\nangle = " + angle.angle()*180/Math.PI;
 }
}


