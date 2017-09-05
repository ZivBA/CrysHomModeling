package meshi.energy.propensityTorsion;

import meshi.energy.Parameters;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 *This is practically the same file as TwoTorsionsParameters.java . See the documentation there.
 **/

class PropensityTorsionParameters extends Parameters {
	
	public String torsion1Name;
    public String torsion2Name;
    private String secondaryStructure;
    public double limit;
    public double resolution;
    public double[][][] coef;    
    public final int[] mapping = new int[22];
	
	
	public PropensityTorsionParameters(String parametersFileName) {
		String parametersFileName1 = parametersFileName;
    	int c1,c2,c3,ns;
    	String line = "";
    	
	try{
	    FileReader fr = new FileReader(parametersFileName);
	    BufferedReader br = new BufferedReader(fr);

	    line = br.readLine();
	    torsion1Name = line.trim();
	    line = br.readLine();
	    torsion2Name = line.trim();

	    line = br.readLine();
	    limit = Double.valueOf(line.trim());
	    line = br.readLine();
	    resolution = Double.valueOf(line.trim());
	    Long lo = 4 * Math.round(limit / resolution) * Math.round(limit / resolution);
	    int Nsquares = lo.intValue();
	    c2 = 0;
		int numOfEntries = 0;
		for (c1=0; c1<22; c1++) {
		line = br.readLine().trim();
		if ((line.compareTo("0") != 0) && (line.compareTo("1") != 0))
				    throw new RuntimeException("Mapping values (begining of file)" + 
					       parametersFileName + " need to be with values {0,1) only\n");		
		c3 = Integer.valueOf(line.trim());
		if (c3==1) {
		    mapping[c1] = c2;
		    c2++;  
		    numOfEntries++;
		}
		else if (c3==0) {
		    mapping[c1] = -1;
		}
		else {
		    throw new RuntimeException("Values in lines 3-24 of file:" + 
					       parametersFileName + "\nneed to be with values {0,1) only\n");
		}
	    }				
	    coef = new double[numOfEntries][Nsquares][16];
	    for (c1=0; c1< numOfEntries; c1++) {
		for (c2=0; c2<Nsquares; c2++) {
		    for (c3=0; c3<16; c3++) {
			line = br.readLine();
			coef[c1][c2][c3] = Double.valueOf(line.trim());
		    }
		}
	    }
	    fr.close();
	}
	catch(Exception e) {
	    throw new RuntimeException(e.getMessage());
	}
    }


	
    public String toString() {
	return "PropensityTorsionParameters\n"+
	    "\t torsion angle 1   = "+torsion1Name+
	    "\t torsion angle 2   = "+torsion2Name+
	    "\t secondary structure   = "+secondaryStructure;
    }


}
