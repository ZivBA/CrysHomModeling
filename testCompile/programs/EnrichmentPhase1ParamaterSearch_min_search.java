package programs;

import meshi.util.file.File2StringArray;

import java.util.StringTokenizer;

/**
 * Running:
 * 
 * java EnrichmentPhase1ParamaterSearch_min_search <list of data files> 
 * 
 * @author Nir
 *
 */

class EnrichmentPhase1ParamaterSearch_min_search {
	
	public static void main(String[] args) {
		System.out.println("Reading files from: " + args[0].trim());
		
		// Columns - Parameters matches
		int column1 = 5; // Centroid
		int column2 = 6; // HB SR
		int column3 = 7; //HB LR 
		int column4 = 8;  // EV
		int column5 = 9;  // PROP
		int column6 = 10;  // Clust
		int columnRMS = 4;
		
		// parameter ranges
//		double[] range1 = {-0.2, 0, 0.05, 0.1, 0.25, 0.5, 1.0, 2.0, 4.0};
//		double[] range2 = {-0.2, 0, 0.05, 0.1, 0.25, 0.5, 1.0, 2.0, 4.0};
//		double[] range3 = {1.0};
////		double[] range4 = {-0.2, 0, 0.05, 0.1, 0.25, 0.5, 1.0, 2.0, 4.0};
//		double[] range4 = {1.0, 2.0, 4.0, 8.0, 12.0, 16.0, 23.0, 32.0, 45.0, 64.0};
//		double[] range5 = {-0.2, 0, 0.05, 0.1, 0.25, 0.5, 1.0, 2.0, 4.0};
//		double[] range6 = {0.2, 0, -0.05, -0.1, -0.25, -0.5, -1.0, -2.0, -4.0};

		double[] range1 = {1.0,0};
        double[] range2 = {-0.2, 0, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 3.0};
        double[] range3 = {-0.2, 0, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 3.0};
        double[] range4 = {0.0, 0.1, 0.2, 0.4, 0.6, 0.8, 1.0, 1.5, 2.0, 3.0, 4.0};
        double[] range5 = {-0.2, 0, 0.05, 0.1, 0.2, 0.4, 0.6, 0.8, 1.0, 1.5};
//        double[] range6 = {0.0};
        double[] range6 = {0.2, 0, -0.05, -0.1, -0.2, -0.4, -0.6, -0.8, -1.0, -1.5, -2.0, -2.5, -3.0};

		
		
		// Reading data
		String[] listOfDataFiles = File2StringArray.f2a(args[0].trim());
		double[][][] data = new double[listOfDataFiles.length][][];
		String token;
		for (int loop=0; loop<data.length ; loop++) {
			String[] tmpData = File2StringArray.f2a(listOfDataFiles[loop]);
			data[loop] = new double[8][tmpData.length];
//			data[loop] = new double[8][tmpData.length-1];
			for (int decoy=0 ; decoy<tmpData.length ; decoy++) {
//			for (int decoy=0 ; decoy<(tmpData.length-1) ; decoy++) {
				StringTokenizer st = new StringTokenizer(tmpData[decoy]);
//				StringTokenizer st = new StringTokenizer(tmpData[decoy+1]);
				for (int column=1 ; st.hasMoreTokens() ; column++) {
					token = st.nextToken();
					if (column==columnRMS)
						data[loop][0][decoy] = new Double(token);
					if (column==column1)
						data[loop][1][decoy] = new Double(token);
					if (column==column2)
						data[loop][2][decoy] = new Double(token);
					if (column==column3)
						data[loop][3][decoy] = new Double(token);
					if (column==column4)
						data[loop][4][decoy] = new Double(token);
					if (column==column5)
						data[loop][5][decoy] = new Double(token);
					if (column==column6)
						data[loop][6][decoy] = new Double(token);
//					data[loop][6][decoy] = Math.log(1+(new Double(token)).doubleValue());				
				}
			}
		}
		
		// Do the parameter search
		double[] rmsVec = new double[range1.length*range2.length*range3.length*range4.length*range5.length*range6.length];
		int rmsVecCounter = 0;
		double bestRMS = Double.MAX_VALUE;
		double totalRMS = 0;
		double best1 = -9999;
		double best2 = -9999;
		double best3 = -9999;
		double best4 = -9999;
		double best5 = -9999;
		double best6 = -9999;
		for (double aRange11 : range1)
			for (double aRange2 : range2)
				for (double aRange3 : range3)
					for (double aRange4 : range4)
						for (double aRange5 : range5)
							for (double aRange6 : range6) {
								// Calculating the enrichment
								totalRMS = 0.0;
								for (int loop = 0; loop < data.length; loop++) {
									for (int decoy = 0; decoy < data[loop][0].length; decoy++) {
										data[loop][7][decoy] = aRange11 * data[loop][1][decoy] +
												aRange2 * data[loop][2][decoy] +
												aRange3 * data[loop][3][decoy] +
												aRange4 * data[loop][4][decoy] +
												aRange5 * data[loop][5][decoy] +
												aRange6 * data[loop][6][decoy];
									}
									double minEnergy = Double.MAX_VALUE;
									int minInd = -1;
									for (int decoy = 0; decoy < data[loop][0].length; decoy++) {
										if (minEnergy > data[loop][7][decoy]) {
											minEnergy = data[loop][7][decoy];
											minInd = decoy;
										}
									}
									totalRMS += (data[loop][0][minInd] * data[loop][0][minInd]);
								}
								totalRMS = Math.sqrt(totalRMS / data.length);
								rmsVec[rmsVecCounter] = totalRMS;
								rmsVecCounter++;
								// Is this better?
								if (totalRMS < bestRMS) {
									best1 = aRange11;
									best2 = aRange2;
									best3 = aRange3;
									best4 = aRange4;
									best5 = aRange5;
									best6 = aRange6;
									bestRMS = totalRMS;
									System.out.println(
											"Best: " + bestRMS + " " + best1 + " " + best2 + " " + best3 + " " + best4 + " " + best5 + " " + best6);
								}
							}
		
		// Pritting the best values:
		System.out.println("\nBest values:\n-----------");
		rmsVecCounter=0;
		for (double aRange1 : range1)
			for (double aRange2 : range2)
				for (double aRange3 : range3)
					for (double aRange4 : range4)
						for (double aRange5 : range5)
							for (double aRange6 : range6) {
								if ((rmsVec[rmsVecCounter] - bestRMS) < 0.05) {
									System.out.println(rmsVec[rmsVecCounter] + "   " + aRange1 + " " +
											aRange2 + " " +
											aRange3 + " " +
											aRange4 + " " +
											aRange5 + " " +
											aRange6);
								}
								rmsVecCounter++;
							}

		System.out.println("\nBest: " + bestRMS + " " + best1 + " " + best2 + " " + best3 + " " + best4 + " " + best5 + " " + best6);

	}
	
}
