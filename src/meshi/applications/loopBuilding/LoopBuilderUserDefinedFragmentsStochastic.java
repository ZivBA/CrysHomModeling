package meshi.applications.loopBuilding;

import meshi.applications.corpus.Corpus;
import meshi.molecularElements.Protein;
import meshi.util.CommandList;
import meshi.util.MeshiProgram;

import java.util.Calendar;
import java.util.Vector;


/**
 * This class implements stochastic fragment assembly of the loop.
 * 
 * @author Nir
 *
 */

public class LoopBuilderUserDefinedFragmentsStochastic extends
		AbstractLoopBuilderUserDefinedFragments {

	final int howOftenToPrintFinalLoop = 100; // How often to print the final loop data to screen.
	final String PRINTING_HEADER_COARSE = "999111111"; // This string will be printed at the begining of the coarse loop printout.
	/**
	 * How much time should the loop building run, before early termination. 
	 */
	final long MAX_SECONDS_TO_RUN = 7200;
	/**
	 * How many loops will be built for each one that will succeed to data saving. 
	 */
	protected final double 	TOTAL_TO_ACCEPTED_RATIO = 2e6;
	
	private int[] maxTakeFromLib;

	LoopBuilderUserDefinedFragmentsStochastic(CommandList commands,
	                                          String writePath, Corpus corpus, Protein prot, Protein ref,
	                                          int resStart, int resEnd, double rmsMatchCO, double rmsCutOff,
	                                          Vector<int[]> fragsDescription) {
		super(commands, writePath, corpus, prot, ref, resStart, resEnd,
				rmsMatchCO, rmsCutOff, fragsDescription);
	}
	
	private void calcToTakeFactor() {
//		double allPossible = 1.0;
//		for (int c=0; c<libs.length ; c++)
//			allPossible *= fragsDescription.get(c)[3];
//		double total_to_accepted_ratio = Math.pow(10.0, libs.length); // This is empirical - I don't know why it works
//		toTakeFactor = Math.pow(maxNumberOfLoopGenerated*total_to_accepted_ratio/allPossible , 1.0/libs.length);
//		toTakeFactor = 1.0/libs[libs.length-1].libSize();
//		System.out.println("The toTakeFactor is: " + toTakeFactor);
		maxTakeFromLib = new int[libs.length];
		double takeFromEachLib = 3.0;
		for (int c = 0; c<libs.length ; c++)
			maxTakeFromLib[c] = (int) (takeFromEachLib + c);
		maxTakeFromLib[0] = (int) (takeFromEachLib);		
		maxTakeFromLib[1] = (int) (takeFromEachLib);
	}

	protected void buildLibraries() {
		super.buildLibraries();
		calcToTakeFactor(); 
	}
	
	
	// Every thing is accepted
	private void addCoarseLoopToResults(InternalLoopData results) {
		allResults.add(new BasicLoopResult(results.evEnergy,results.propEnergy,results.bbHBenergy,0.0/*score is ignored*/,results.RMS,fragRank,saveLoopCoordinates(),saveLoopCoordinatesNCaC(),savePhiPsiOfLoop()));
	}

	@Override
	protected void buildCoarsFragments() {
		long startingTime = Calendar.getInstance().getTimeInMillis();
		long nowTime = startingTime;
		/*
	  This parameter determine how far to go in the libray to search. The HIGHER it is, the LESS likely that frags with high index in the library
	  be selected. The chance of selecting the last fragment in the library is about exp(EXPLORATORY_FACTOR) less likely than the first fragment.
	 */
		int rounds = 0;
		/*
	  How many times should the loop building start, ideally this should be 2-3. If it above this number than
	  the TOTAL_TO_ACCEPTED_RATIO should be higher.
	 */
		int MAX_NUMBER_OF_ROUNDS = 1000000000;
		while (!gotEnoughCoarseModels && (rounds < MAX_NUMBER_OF_ROUNDS) &&
				((nowTime-startingTime)/1000 < MAX_SECONDS_TO_RUN)) {
			addFromLib(0);
			rounds++;
			nowTime = Calendar.getInstance().getTimeInMillis();
		}
		if (rounds >= MAX_NUMBER_OF_ROUNDS)
			System.out.println("\n\n\nWARNING: The number of rounds has reached " + MAX_NUMBER_OF_ROUNDS + " where it should be ideally 1. The parametrization is way wrong.\n\n\n");
		if ((nowTime-startingTime)/1000 >= MAX_SECONDS_TO_RUN) 
			System.out.println("\n\n\nWARNING: The run timeouted.\n\n\n");
		printStats();
	}
	
	void 	printStats() {
	    System.out.println("No stats to print");	
	}
	
	@Override
	protected int chooseFrag(int libCounter) {
		int libFrag;
		libFrag = (int) (libs[libCounter].libSize()* MeshiProgram.randomNumberGenerator().nextDouble());
//		libFrag = (int) (libs[libCounter].libSize()*(Math.exp(EXPLORATORY_FACTOR*MeshiProgram.randomNumberGenerator().nextDouble())-1.0)/PRECALCULATED_DENOMINATOR);
		return libFrag;
	}

	@Override
	protected boolean continueSearch(int libCounter, int c) {
		return (c<maxTakeFromLib[libCounter]);
//		return (c<takeFromEachLib);
	}

	// Always accepted
	protected boolean loopClosingCondition() {
		return true;
	}

	protected void setClosureAtoms() {
		// Not define in this class
	}	

	protected void analyzedCloseLoop() {
		InternalLoopData results;
		try {
			results = evaluateScores(resStart ,resEnd); 	
		}
		catch (Exception e) {
			System.out.println("Warning: an error occured in the energy calculation. The loop will be dicarded.\n the error is:" + e.getMessage());
			return;
		}
		if (results.evEnergy<EV_CUTOFF) {
			numberOfCoarseGenerated++;
			double score = 2.0*results.bbHBenergy;
			if (numberOfCoarseGenerated>maxNumberOfLoopGenerated)
				gotEnoughCoarseModels = true;
			if (numberOfCoarseGenerated/howOftenToPrintFinalLoop == (numberOfCoarseGenerated /(1.0*howOftenToPrintFinalLoop))) {
				System.out.print(PRINTING_HEADER_COARSE + " " + numberOfCoarseGenerated + " " +
						fmt2.format(results.propEnergy) + " " +
						fmt2.format(results.evEnergy) + " " + 
						fmt2.format(results.bbHBenergy) + " " +
						fmt2.format(999) + " " + 
						fmt2.format(results.RMS) + "          ");
				for (int aFragRank : fragRank) {
					System.out.print(aFragRank + " ");
					score += 0.0 * (Math.log(1 + aFragRank));
				}
				System.out.print("       ");
				for (int anActuallyTaken : actuallyTaken) {
					System.out.print(anActuallyTaken + " ");
				}
				System.out.println();
			}
			addCoarseLoopToResults(results);
		}
	}

	//Returns a 'InternalLoopData' object with updated energies on the current loop configuration.
	InternalLoopData evaluateScores(int rmsCalcStart, int rmsCalcEnd) {
		double totProp = 0.0;
		for (int res=resStart ; res<=resEnd ; res++) {
			if (prepro[res-resStart])
				totProp += corpus.propensityAll.preProVal(pp[res][0], pp[res][1]);
			else
				totProp += corpus.propensityAll.propVal(seq[res-resStart], pp[res][0], pp[res][1]); 
		}
		try {
			energy.update();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (ref != null) {
			return new InternalLoopData(energyTermEVloopFinal.evaluate(),
					totProp,
					energyTermHB.evaluate(),
					calcRMS(rmsCalcStart, rmsCalcEnd));
		}
		else {
			return new InternalLoopData(energyTermEVloopFinal.evaluate(),
					totProp,
					energyTermHB.evaluate(),
					-1);
		}
	}
		
}
