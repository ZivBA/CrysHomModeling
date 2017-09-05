package meshi.energy.alphaTorsion;

import meshi.energy.Parameters;

import java.util.StringTokenizer;

/**
 *Parsing a line in the parameter file of the alpha torsion energy.
 *Each line in the parameter file of the alpha torsion energy is with the following format:
 *{AA type}
 *{Weight = the parabola height}
 *{Starting torsion value value of the HELIX secondary structure}
 *{Finish torsion value value of the HELIX secondary structure}
 *{Starting torsion value value of the SHEET secondary structure}
 *{Finish torsion value value of the SHEET secondary structure}
 **/

class AlphaTorsionParameters extends Parameters {

    public final String aaLetter;
    public final double weightAA;
    public double startAlphaHELIX;
    public double endAlphaHELIX;
    public double startAlphaSHEET;
    public double endAlphaSHEET;


    public AlphaTorsionParameters(String line) {
    	StringTokenizer stok;
       	stok = new StringTokenizer(line);
        aaLetter = stok.nextToken().trim();
        weightAA = Double.valueOf(stok.nextToken());
        if (weightAA < 0.0)
            throw new RuntimeException("Weight must be non-negative\n");      	        
        startAlphaHELIX = Double.valueOf(stok.nextToken());
        if ((startAlphaHELIX < -Math.PI) || (startAlphaHELIX > Math.PI))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
        endAlphaHELIX = Double.valueOf(stok.nextToken());
        if ((endAlphaHELIX < -Math.PI) || (endAlphaHELIX > Math.PI) || (endAlphaHELIX < startAlphaHELIX))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
        startAlphaSHEET = Double.valueOf(stok.nextToken());
        if ((startAlphaSHEET < -Math.PI) || (startAlphaSHEET > Math.PI))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
        endAlphaSHEET = Double.valueOf(stok.nextToken());
        if ((endAlphaSHEET < -Math.PI) || (endAlphaSHEET > Math.PI) || (endAlphaSHEET > startAlphaSHEET))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
    }


	
    public String toString() {
	return "AlphaTorsionParameters\n"+
	    "\t AA Letter   = "+aaLetter+"\n";
    }

}
