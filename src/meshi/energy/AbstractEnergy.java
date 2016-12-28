package meshi.energy;

import meshi.molecularElements.Atom;
import meshi.util.MeshiList;
import meshi.util.Updateable;
import meshi.util.UpdateableException;
import meshi.util.filters.Filter;

import java.util.Iterator;

/**
 * A super class for all meshi energy terms. 
 * Currently oriented towards drivable energy functions. See meshi.energy.bond.BondEnergy. 
 **/
public abstract class AbstractEnergy implements Updateable  {
    /**
     * Energy function/term name.
     **/
    /**
     * A short name for the energy term.
     **/
    protected String comment = "Abstract energy";

    /** 
     * The weight of the energy term within the total energy.
     **/
    protected double weight;

    /**
     * The energy term is evaluated only if it is on.
     **/
    protected boolean on = true;

    
    public static final Filter filter = new IsEnergy();

    /**
     * 1/0 good for dubugging.
     **/
    public static final double INFINITY = Double.POSITIVE_INFINITY;
    /**
     * sqrt(-1) good for dubugging.
     **/
    public static final double NaN = Math.sqrt(-1.0); 

// --Commented out by Inspection START (16:35 31/10/16):
//    // For derivation error testing:
//    protected double[][] coordinates = new double[3][];
// --Commented out by Inspection STOP (16:35 31/10/16)
    protected final double  DX = 1e-7;  /* should be roughly the sqrare root of the machine's relative precision.
					In java this is about sqrt(1e-15) */
    protected final double relativeDiffTolerance = 0.01; // should be 3-4 orders of magnitude higher than DX.
    protected final double verySmall = Math.exp(-15);
    protected final String XYZ = "XYZ";
    
    /**
     * A list of all the parameters needed by the energy term.
     **/
    protected ParametersList parametersList;

    /*
     * A list of all the updateable resources needed by the energy term. For a detailed description 
     * of MESHI's treatment of updatable resources see the documentation of TotalEnergy.
     **/
    private UpdateableList updateableResources;

    /**
     * Construct a dummy object that cannot be evaluated. Such an object is useful as a key 
     * during searches.
     **/
    protected AbstractEnergy() {}

    /** 
     * construct An energy term. 
     **/
    protected AbstractEnergy(Object[] updateableResources,
                             ParametersList parametersList,
                             double weight) {
	this.updateableResources = new UpdateableList(updateableResources);
	this.weight = weight;
	this.parametersList = parametersList;
    }

    /**
     * construct An energy term without parameters List.
     **/
    protected AbstractEnergy(Object[] updateableResources,
                             double weight) {
	this(updateableResources, null, weight);
    }

    /**
     * Updates the updatable resources. For a detailed description 
     * of MESHI's treatment of updatable resources see the documentation of TotalEnergy.
     **/
    public void update(int numberOfUpdates) throws UpdateableException{
	updateableResources.update(numberOfUpdates);
    }
    
   /**
    * Evaluates the energy term and <b> update </b> the derivatives.
    **/ 
    public abstract double evaluate();
  /**
    * Evaluates the energy term and devides the energy between the atoms. The energy field of
    * each atom is assigned a value - its contribution to the total energy sum. 
    **/ 
    public abstract void evaluateAtoms();
   
    /**
     * Turnes the energyTerm ON.
     **/
    public void on() {
	on = true;
    }

    /**
     * Turnes the energyTerm OFF.
     **/
    public void off() {
	on = false;
    }


    /**
     * Looking for one "criminal" atom whose derivation is wrong.
     */
    public abstract void test(TotalEnergy totalEnergy,Atom atom);


    //----------------------------------------- housekeeping ---------------------------------------------
    protected void handleMissingParameters(Object obj) {
	throw new RuntimeException("Missing parameters for:\n"+obj);
    }

    public String comment() {return comment;}

    public  String toString() {return comment;}



    //--------------------------------------- auxiliary methods--------------------------------------------------
    /**
     * Generates an empty array.
     **/
    protected static Object[] toArray() {
	    return new Object[]{};
    }

     /**
     * Generates an aray with one element - the parameter.
     **/
   protected static Object[] toArray(Object o1) {
	   return new Object[]{o1};
    }
    protected static Object[] toArray(Object o1,Object o2) {
	    return new Object[]{o1,o2};
    }
    protected static Object[] toArray(Object o1,Object o2,Object o3) {
	    return new Object[]{o1,o2,o3};
    }
    // ------------------------------------- internal auxiliary classes -----------------------------------------

    /**
     * A list of updateable elements (implementing the meshi.util.Updateable interface). 
     **/
    class UpdateableList extends MeshiList {
	public UpdateableList(Object[] array) {
	    super(new IsUpdatable(), array);
	}

// --Commented out by Inspection START (16:35 31/10/16):
//	public UpdateableList() {
//	    super(new IsUpdatable());
//	}
// --Commented out by Inspection STOP (16:35 31/10/16)

	/**
	 * Iterates over the list updates each of its elements.
	 **/ 
	public void update(int numberOfUpdates) throws UpdateableException{
	    Iterator resources = iterator();
	    Updateable resource;
	    while((resource = (Updateable) resources.next()) != null) {
		resource.update(numberOfUpdates);
	    }
	}
    }

public boolean isOn() {return on;}

    private static class IsUpdatable implements Filter {
	public boolean accept(Object obj) {
	    return (obj instanceof Updateable);
	}
    }

    private static class IsEnergy implements Filter {
	public boolean accept(Object obj) {
	    return (obj instanceof AbstractEnergy);
	}
    } 
}


  
