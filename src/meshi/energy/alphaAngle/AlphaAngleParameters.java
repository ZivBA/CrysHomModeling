package meshi.energy.alphaAngle;

import meshi.energy.Parameters;

import java.util.StringTokenizer;

/**
 *Parsing a line in the parameter file of the alpha angle energy.
 *Each line in the parameter file of the alpha angle energy is with the following format:
 *{AA type}
 *{Weight = the parabola height}
 *{Starting angle value of the ALL secondary structure}
 *{Finish angle value of the ALL secondary structure}
 *{Starting angle value of the HELIX secondary structure}
 *{Finish angle value of the HELIX secondary structure}
 *{Starting angle value of the SHEET secondary structure}
 *{Finish angle value of the SHEET secondary structure}
 *{Starting angle value of the COIL secondary structure}
 *{Finish angle value of the COIL secondary structure}
 **/

class AlphaAngleParameters extends Parameters {

    public final String aaLetter;
    public final double weightAA;
	public double startAlphaHELIX;
    public double endAlphaHELIX;
    public double startAlphaSHEET;
    public double endAlphaSHEET;
    public double startAlphaCOIL;
    public double endAlphaCOIL;


    public AlphaAngleParameters(String line) {
    	StringTokenizer stok;
       	stok = new StringTokenizer(line);
        aaLetter = stok.nextToken().trim();
        weightAA = Double.valueOf(stok.nextToken());
        if (weightAA < 0.0)
            throw new RuntimeException("Weight must be non-negative\n");
	    double startAlphaALL = Double.valueOf(stok.nextToken());
        if ((startAlphaALL < 0.0) || (startAlphaALL > Math.PI))
            throw new RuntimeException("Wrong values in the parameters file\n");
	    double endAlphaALL = Double.valueOf(stok.nextToken());
        if ((endAlphaALL < 0.0) || (endAlphaALL > Math.PI) || (endAlphaALL < startAlphaALL))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
        startAlphaHELIX = Double.valueOf(stok.nextToken());
        if ((startAlphaHELIX < 0.0) || (startAlphaHELIX > Math.PI))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
        endAlphaHELIX = Double.valueOf(stok.nextToken());
        if ((endAlphaHELIX < 0.0) || (endAlphaHELIX > Math.PI) || (endAlphaHELIX < startAlphaHELIX))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
        startAlphaSHEET = Double.valueOf(stok.nextToken());
        if ((startAlphaSHEET < 0.0) || (startAlphaSHEET > Math.PI))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
        endAlphaSHEET = Double.valueOf(stok.nextToken());
        if ((endAlphaSHEET < 0.0) || (endAlphaSHEET > Math.PI) || (endAlphaSHEET < startAlphaSHEET))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
        startAlphaCOIL = Double.valueOf(stok.nextToken());
        if ((startAlphaCOIL < 0.0) || (startAlphaCOIL > Math.PI))
            throw new RuntimeException("Wrong values in the parameters file\n");      	
        endAlphaCOIL = Double.valueOf(stok.nextToken());
        if ((endAlphaCOIL < 0.0) || (endAlphaCOIL > Math.PI) || (endAlphaCOIL < startAlphaCOIL))
            throw new RuntimeException("Wrong values in the parameters file\n");      	    	
    }


	
    public String toString() {
	return "AlphaAngleParameters\n"+
	    "\t AA Letter   = "+aaLetter+"\n";
    }

}
