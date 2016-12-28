package meshi.energy.hydrogenBondsAngle;

import meshi.energy.NonBondedEnergyElement;
import meshi.energy.hydrogenBond.HB_AtomAttribute;
import meshi.geometry.Angle;
import meshi.geometry.Distance;
import meshi.geometry.DistanceMatrix;
import meshi.molecularElements.Atom;

/**
 * Created by IntelliJ IDEA.
 * User: amilev
 * Date: 13/03/2006
 * Time: 10:49:48
 * implements the main code for energyElements of energy terms
 * that punish angles of Hydrogen Bonds
 */
public abstract class AbstractPunishAngleEnergyElement extends NonBondedEnergyElement {
        //-------------------------------- data fields -----------------------------------

    Atom oAtom;
	Atom hAtom;
	Atom theirdAtom;
	double distanceEnergy;
    private Distance distance;
    final DistanceMatrix distanceMatrix;
    double distanceValue;
	private final double X_MAX=4;
	private final double X_MAX2 = X_MAX*X_MAX;
	private final double X_ANGLE = 150.0*Math.PI/180.0;
	private final double X_ANGLE2 = X_ANGLE*X_ANGLE;   //TODO try 160
    private final double c1=10.0;
	private final double c2=40.0;
	final double a2=c2/X_ANGLE2;
	final double b2=-2.0*c2/X_ANGLE;
    //new way to look at this function
	
	final double weight;
	double energy;
    double angleEnergy;
    double dAngleEnergyDxOAtom;
	double dAngleEnergyDyOAtom;
	double dAngleEnergyDzOAtom;
    double dAngleEnergyDxHAtom;
	double dAngleEnergyDyHAtom;
	double dAngleEnergyDzHAtom;
    double dAngleEnergyDxTheidAtom;
	double dAngleEnergyDyTheidAtom;
	double dAngleEnergyDzTheidAtom;
    private int oFactor;
	private int hFactor;
    private double deDxOAtom;
	private double deDyOAtom;
	private double deDzOAtom;
    private double deDxHAtom;
	private double deDyHAtom;
	private double deDzHAtom;
    double deDxTheidAtom;
	double deDyTheidAtom;
	double deDzTheidAtom;
    Angle angle;
    double angleValue;
	double dAngleEnergyDAngle;

    //----------------------------------- constructors --------------------------------------

    AbstractPunishAngleEnergyElement(DistanceMatrix distanceMatrix, double weight)
    {
        this.distanceMatrix = distanceMatrix;
        this.weight = weight;
    }


    //---------------------------------- methods --------------------------------------------

    protected abstract String comment();

    protected abstract void setTheirdAtom();

    public void set(Object obj){ //obj should be Distance of hydrogen-oxygen pair
        distance = (Distance) obj;
        atoms = distance.atoms();
	    Atom atom1 = distance.atom1();
	    Atom atom2 = distance.atom2();
        HB_AtomAttribute atom1_attribute = (HB_AtomAttribute) atom1.getAttribute(HB_AtomAttribute.key);
        if (atom1_attribute.isH) {
            hFactor = oFactor = -1;
            hAtom = atom1;
            oAtom = atom2;
        }
        else {
            hFactor = oFactor = 1;
            hAtom = atom2;
            oAtom = atom1;
        }

        setTheirdAtom();
    }

    /**
     * energy calculation and atom forces updating.
     **/
    public double evaluate() {
        updateEnergy();
        updateAtoms();
        return energy * weight;
    }

     public double evaluate(double weight) {
        updateEnergy();
        updateAtoms(weight);
        return energy * weight;
    }

    protected abstract void setAngle();

    protected abstract void setDAngleEnergyDatoms();


    /**
     * energy and dirivarives calculation.
     **/
    private double updateEnergy() {
         setAngle();
        //now we take care of the distance part:
        //--------------------------------------
	    double dDistanceEnergy_Ddistance = 0;
        distanceEnergy = 0;
        angleEnergy=0;
        angleValue =0.1;
        distanceValue = distance.distance();
        angleValue = angle.angle();
        if(distanceValue >= X_MAX || angleValue >= X_ANGLE) //good angle or not a candidate energy =0
            {
                deDxOAtom = 0;
                deDyOAtom = 0;
                deDzOAtom = 0;
                deDxHAtom = 0;
                deDyHAtom = 0;
                deDzHAtom = 0;
                deDxTheidAtom = 0;
                deDyTheidAtom = 0;
                deDzTheidAtom = 0;

                energy = 0;
            }
        else //bad angle
            {
	            double distanceValue2 = distanceValue * distanceValue;
	            double b1 = -2.0 * c1 / X_MAX;
	            double a1 = c1 / X_MAX2;
	            distanceEnergy = a1 * distanceValue2 + b1 *distanceValue+c1;

                dDistanceEnergy_Ddistance = 2* a1 *distanceValue+ b1;
	
	            double dDistanceEnergyDx = dDistanceEnergy_Ddistance * distance.dDistanceDx();
	            double dDistanceEnergyDy = dDistanceEnergy_Ddistance * distance.dDistanceDy();
	            double dDistanceEnergyDz = dDistanceEnergy_Ddistance * distance.dDistanceDz();
	
	            double dDistanceEnergyDxOAtom = dDistanceEnergyDx * oFactor;
	            double dDistanceEnergyDyOAtom = dDistanceEnergyDy * oFactor;
	            double dDistanceEnergyDzOAtom = dDistanceEnergyDz * oFactor;
	            double dDistanceEnergyDxHAtom = -1 * dDistanceEnergyDx * hFactor;
	            double dDistanceEnergyDyHAtom = -1 * dDistanceEnergyDy * hFactor;
	            double dDistanceEnergyDzHAtom = -1 * dDistanceEnergyDz * hFactor;

                //AngleElement Part:
                //-------------------------------------
	            double angleValue2 = angleValue * angleValue;
                angleEnergy = a2* angleValue2 +b2*angleValue+c2;
	
	            double y1X_Angle2 = 40.0 / X_ANGLE2;
	            angleEnergy = y1X_Angle2 *(angleValue - X_ANGLE)*(angleValue - X_ANGLE );

                setDAngleEnergyDatoms();

                //combine the angle and the distance:
                //---------------------------------------
                //The formula: deDXOAtom = deDXOAtom[distanceEnergy*angleEnergy] = dDistanceEnergyDXOAtom*angleEnergy + distanceEnergy * dAngleEnergyDXOAtom :: where X = x,y,z
                deDxOAtom = (dDistanceEnergyDxOAtom * angleEnergy) + (distanceEnergy * dAngleEnergyDxOAtom);
                deDyOAtom = (dDistanceEnergyDyOAtom * angleEnergy) + (distanceEnergy * dAngleEnergyDyOAtom);
                deDzOAtom = (dDistanceEnergyDzOAtom * angleEnergy) + (distanceEnergy * dAngleEnergyDzOAtom);
                deDxHAtom = (dDistanceEnergyDxHAtom * angleEnergy) + (distanceEnergy * dAngleEnergyDxHAtom);
                deDyHAtom = (dDistanceEnergyDyHAtom * angleEnergy) + (distanceEnergy * dAngleEnergyDyHAtom);
                deDzHAtom = (dDistanceEnergyDzHAtom * angleEnergy) + (distanceEnergy * dAngleEnergyDzHAtom);
                deDxTheidAtom =                                    		  distanceEnergy * dAngleEnergyDxTheidAtom ;
                deDyTheidAtom =                                   		  distanceEnergy * dAngleEnergyDyTheidAtom ;
                deDzTheidAtom =                                   		  distanceEnergy * dAngleEnergyDzTheidAtom ;


                energy = distanceEnergy * angleEnergy;
            }
        if (energy < 0){
            System.out.println(comment()+" smaller then 0: "+energy+" distanceEnergy: "+distanceEnergy+" angleEnergy: "+angleEnergy+" dis: "+distanceValue+" angle: "+angleValue);
            energy = 0;
            deDxOAtom = 0;
            deDyOAtom = 0;
            deDzOAtom = 0;
            deDxHAtom = 0;
            deDyHAtom = 0;
           deDzHAtom = 0;
           deDxTheidAtom = 0;
           deDyTheidAtom = 0;
           deDzTheidAtom = 0;

        }

        return energy;
    }

    private void updateAtoms(){
              updateAtoms(weight);
    }

    private void updateAtoms(double weight){
        if (! oAtom.frozen()) {
            oAtom.addToFx(-1 * deDxOAtom * weight); // force = -derivative
            oAtom.addToFy(-1 * deDyOAtom * weight); // force = -derivative
            oAtom.addToFz(-1 * deDzOAtom * weight); // force = -derivative
        }
        if (! hAtom.frozen()) {
            hAtom.addToFx(-1 * deDxHAtom * weight);
            hAtom.addToFy(-1 * deDyHAtom * weight);
            hAtom.addToFz(-1 * deDzHAtom * weight);
        }

        updateTheirdAtom(weight);

    }

    protected abstract void updateTheirdAtom(double weight);
    public abstract void updateTheirdAtom();

    protected void setAtoms(){
        throw new RuntimeException("setAtoms() may not be used by HydrogenBondsEnergyElement for "+
                                   "efficiency.");
    }

}
