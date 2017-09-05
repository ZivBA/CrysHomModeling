/*
 * Created on 20/12/2004
 *
 * 
 */
package meshi.energy.hydrogenBondsPlane;

import meshi.geometry.Distance;
import meshi.molecularElements.Atom;
import meshi.parameters.AtomTypes;
import meshi.util.filters.Filter;

import java.util.Arrays;

public class IsCN implements Filter,AtomTypes{
    private static int[] sortBB_Nitrogens;
	private static int[] sortBB_Carbons;
	
	//create a new SORT copy of BB_NITROGENS & BB_CARBONS that are defined in interface AtomType
    public IsCN(){
        sortBB_Nitrogens = new int[BB_NITROGENS.length];
	    System.arraycopy(BB_NITROGENS, 0, sortBB_Nitrogens, 0, BB_NITROGENS.length);
        sortBB_Carbons = new int[BB_CARBONS.length];
	    System.arraycopy(BB_CARBONS, 0, sortBB_Carbons, 0, BB_CARBONS.length);
        Arrays.sort(sortBB_Nitrogens);
        Arrays.sort(sortBB_Carbons);
    }
    
    public IsCN(Atom[] a){
        this();
	    int[] atoms = new int[a.length];
    	for(int i=0;i<a.length;i++){
    		if(isC(a[i]))
    			atoms[i]=1;
    		else if(isN(a[i]))
    			atoms[i]=2;
    		else atoms[i]=0;
    	}
    }

    public static boolean isC(Atom atom){
	    return (Arrays.binarySearch(sortBB_Carbons,atom.type) >= 0);
    }

    public static  boolean isN(Atom atom){
	    return (Arrays.binarySearch(sortBB_Nitrogens,atom.type) >= 0);
    }


    public boolean accept(Object obj){
        Distance dis = (Distance)obj;
        CN_AtomAttribute atom1Attribute =
            (CN_AtomAttribute) (dis.atom1().getAttribute(CN_AtomAttribute.key));
    	CN_AtomAttribute atom2Attribute =
            (CN_AtomAttribute) (dis.atom2().getAttribute(CN_AtomAttribute.key));
	if ( atom1Attribute == null || atom2Attribute == null)//meens that at list one of the atom is not N or C
//since we add attribute just to C or N
	    return false;
        return (atom1Attribute.isC &&
                atom2Attribute.isN)
                || (atom1Attribute.isN &&
                atom2Attribute.isC);
    }
}
