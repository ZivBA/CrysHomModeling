/*
 * Created on 20/12/2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package meshi.energy.hydrogenBond;

import meshi.geometry.Distance;
import meshi.molecularElements.Atom;
import meshi.parameters.AtomTypes;
import meshi.util.filters.Filter;

import java.util.Arrays;

/**
 * @author amilev
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class IsHO implements Filter,AtomTypes {
    private static int[] sortBB_Hydrogens;
	private static int[] sortBB_Oxygens;
	// backbond oxygen (2) or else (0)

    //create a new SORT copy of BB_HYDROGENS & BB_OXYGENS that are defined in interface AtomType
    public IsHO(){
              sortArrays();
    }

    private static void sortArrays(){
        if(sortBB_Hydrogens == null){
            sortBB_Hydrogens = new int[BB_HYDROGENS.length];
	        System.arraycopy(BB_HYDROGENS, 0, sortBB_Hydrogens, 0, BB_HYDROGENS.length);
            sortBB_Oxygens = new int[BB_OXYGENS.length];
	        System.arraycopy(BB_OXYGENS, 0, sortBB_Oxygens, 0, BB_OXYGENS.length);
            Arrays.sort(sortBB_Hydrogens);
            Arrays.sort(sortBB_Oxygens);
        }
    }
    
    public IsHO(Atom[] a){
        sortArrays();
	    int[] atoms = new int[a.length];
    	for(int i=0;i<a.length;i++){
    		if(isH(a[i]))
    			atoms[i]=1;
    		else if(isO(a[i]))
    			atoms[i]=2;
    		else atoms[i]=0;
    	}
    }

    public static boolean isH(Atom atom){
        sortArrays();
        return (Arrays.binarySearch(sortBB_Hydrogens,atom.type) >= 0);
    }

    public static  boolean isO(Atom atom){
         sortArrays();
        return (Arrays.binarySearch(sortBB_Oxygens,atom.type) >= 0);
    }
    
    //This version uses the methods isBackbondHydrogen() and isBackbondOxygen() of Atom to find out if they are BackbondHydrogen or BackbondOxygen.
    /*
     * @parm obj is Distance
     */
    public boolean accept(Object obj){
        Distance dis = (Distance)obj;
        HB_AtomAttribute atom1Attribute =
            (HB_AtomAttribute) (dis.atom1().getAttribute(HB_AtomAttribute.key));
    	HB_AtomAttribute atom2Attribute =
            (HB_AtomAttribute) (dis.atom2().getAttribute(HB_AtomAttribute.key));
	if ( atom1Attribute == null || atom2Attribute == null)//meens that at list one of the atom is not H or O 
//since we add attribute just to H or O
	    return false;
        return ( ( atom1Attribute.isH &&
                   atom2Attribute.isO )
                 ||  
                 ( atom1Attribute.isO &&
                   atom2Attribute.isH ) );
        		
    }

   //  public boolean accept(Object obj){
//         Distance dis = (Distance)obj;
//         HB_DistanceAttribute distanceAttribute =
//             (HB_DistanceAttribute) dis.getAttribute(HB_DistanceAttribute.key);
//         if (distanceAttribute == null)
//             return isHO(dis);
//         else
//             return distanceAttribute.isHbond;
//     }
}
