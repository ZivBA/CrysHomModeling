package meshi.optimizers;

import meshi.energy.TotalEnergy;
import meshi.util.CommandList;

import java.util.LinkedList;


/**
 *This class implements a LBFGS minimizer according to the scheme in: Numerical Optimization by J. Nocendal & 
 *S. J. Wright, Springer 1999, pp 224-226.
 *
 *The BFGS algorithm (general)
 *----------------------------
 *In Newton minimizers an aproximation to the Hessian of the energy function at position Xk is calculated. Then finding the 
 *inverse of that Hessian (Hk), and solving the equation Pk = -Hk*grad(Xk) gives a good search direction Pk. Later, a   
 *line search procedure has to determine just how much to go in that direction (producing the scalar alpha_k). The new 
 *position is given by: Xk+1 = Xk + alpha_k*Pk.In the LBFGS method the inverse Hessian is not computed explicitly. Instead
 *it is updated each step by the values of Pk and the new gradient. The updating formula is (t - transpose):
 *Hk+1 = (I  - Rk*Sk*Ykt)Hk(I  - Rk*Yk*Skt) + Rk*Skt*Sk 
 *where:
 *Sk = Xk+1 - Xk  
 *Yk = grad(Xk+1)-grad(Xk)
 *Rk = 1/(Ykt*Sk)
 *
 *
 *The LBFGS algorithm 
 *--------------------------------------------
 *To make the Light-Memory BFGS, we "cheat" by not maintaining the Hessian matrix. instead we keep a short history 
 *of m elements the vectors Yi, Si, and Ri (called curv in the code) as i = k-1..k-1-m. We use a storage (LinkedList)
 *to store up to m last elements, and in case we pass m elements we discard the oldest element and insert the new one
 *at the beginning of the list. We then calculate the search direction Pk using "two-loop recursion" algorithm 
 *(approximating Hk*grad(Xk)) directly from the elements we stored.
 *We also approximate the Hk0 to be gamma*I so Hk0*grad is just gamma*grad. gamma is calculated by the algorithm provided (default)
 *or is set to 1 (useGamma=false in the parameters) if so desired.
 *
 *For more detail see the mentioned book.
 *
 *
 *General minimization parameters
 *-------------------------------
 *- energy - pointer to an TotalEnergy object, where the energy function is.
 *- tolerance - 1e-6 - Minimization stops when the magnitude of the maximal gradient component drops below tolerance.
 *- maxIteration - 1000 - The maximal number of iteration steps allowed
 *- reoprtEvery - 100 - The frequency of the minimization reports.
 *
 *
 *Parameters Specific to the LBFGS algorithm
 *-----------------------------------------
 *- allowedMaxR - 100*n - In some energy function scenerios the inverse Hessian approximation might become unstable and
 *                        unrealiable. This paramter sets a upper limit on the curvature Rk.
 *                        Higher values would lead to a new kick-start. This value should be somewhere
 *                        in the range 100*n.
 *- maxNumKickStarts - 3 - If the minimzer become unstable for some reason, it could be restarted from the current position.
 *                         This parameter determined how many times this could happen before the minimization is aborted.
 *- m 				 - 20 - The storage size to use. 3-20 is the recomended value range.
 *- useGamma			- true - if to use gamma to get Hk0 (true) or to assume gamma=1 (false)
 *
 *Parameters specific to the Wolf conditions line search
 *------------------------------------------------------
 *The LBFGS algorithm requires a step length finder who finds a step length that also satisfies the Wolf conditions. See the
 *help of this specific line search for explaination of what these conditions are. The parameters of this line search are:
 * 
 *- c1,c2 - 1e-4,0.9 - The two paramters of the Wolf conditions. Must satisfy: 0<c1<c2<1
 *- maxNumEvaluations - 10 - The maximal number of step length trails allowed. This gives an upper limit on the total number of
 *                        evaluations both in the bracketing and Zoom. If line search fails because this number was 
 *                        exceeded try to enlarge 'extendAlphaFactor' or change the initial alpha guess. This error might
 *                        also be caused if the tolerance of the minimizer is set to be extremely small. 
 *- extendAlphaFactor - 3 - After a certain step length fails, the next step length is taken as this multiple of the failing
 *                        last alpha.
 *
 *
 *Steepest Decent module
 *-----------------------
 *In two cases steepest descent minimization is done instead of LBFGS. 
 *1) All runs start with a certain number of steepest descent steps, because difficult scenarios for LBFGS minimization
 *might occur at the start due to atom clashes.
 *2) If the normal operation of the minimizer is disturbed for some reason  (failing to produce a descent direction, 
 *failing to satisfies the wolf conditions, etc.) another set of steepst descent steps (with similar parameters to 
 *case 1) is attempted. If the normal operation is disturbed too many times, the minimization is aborted because
 *this is indicative of a more severe fault, most likely in the energy function.   
 *
 *The steepest descent parameters are as follow:
 *- numSteepestDecent - 50 - The number of steepest descent steps to be taken. If this number is smaller than 1, than at 
 *                      least one steepest descent step is done.
 *- initialStepLength - 1 - parameter of the steepest descent line search. The first step length to be tried after the 
 *                      calculation of the first gradient. This parameter should normally be 1 unless very large gradients 
 *						(such as clashhing of VDW atoms) are expected in the first steps. In that case it should be  set to
 *                      a much smaller value (1e-4 or less). 
 *- stepSizeReduction - 0.5 - parameter of the line search. The step length is multiplied by this factor if no reduction 
 *                      in energy is achieved.	  
 *- stepSizeExpansion - 2 - parameter of the line search. The first step length tried is the step length from previous
 *                      line search multiplied by this factor. (Note that non-positive values to this paramater cause
 *                      special options to be called (see the SimpleStepLength class help).
 *
 *
 *Note
 *-------------------
 *We don't limit the number of atoms to use in the LBFGS like the BFGS.
 *
 *	
 **/

public class LBFGS extends Minimizer {
    private SteepestDecent steepestDecent;
    private WolfConditionLineSearch lineSearch;
    private int n; // number of variables
    private double[] X; // The coordinates vector at iteration K
    private double[] G; // The gradient vector at iteration K	(true gradient! not -Grad)

    private int m; // number of vector history to use
    private boolean useGama;
    private LinkedList<Element> storage; // to hold Element {Yi, Si, Ri} i=k..k-m
    private class Element {
	double[] Y;
	double[] S;
	double   curv;
    }
    // for the two-loop recursion alg.
    private double[] R; // auxilary vector
    private double[] Q; // auxilary vector
    private double[] alpha; // auxilary vector
	private double gama = 1;
	
	private double[][] coordinates; // The position and gradients of the system
    private double[][] bufferCoordinates;
	private int iterationNum; // Iterations counter
	private String bfgsErrorString;
    private int numKickStarts; // The number of times the minimizer was restarted

    // LBFGS paramters
    private int maxNumKickStarts;
    private int allowedMaxR;
    private static final int DEFAULT_ALLOWED_MAX_R_FACTOR = 100; // R <= maxRFactor*n
    //    private static final int DEFAULT_MAX_NUM_KICK_STARTS = 3; // Dont change this number unless necessary
    private static final int DEFAULT_MAX_NUM_KICK_STARTS = 5; // Changed it 27.4.05 version 1.12 I think it is necessary.
    private static final boolean DEFAULT_USE_GAMA = true;
    private static final int DEFAULT_M = 20;

    // Wolf conditions line search parameters
    private double c1;
    private double c2;
    private double extendAlphaFactorWolfSearch;
    private int maxNumEvaluationsWolfSearch;
    private static final double DEFAULT_C1 = 1e-4;
    private static final double DEFAULT_C2 = 0.9;
    private static final double DEFAULT_EXTENDED_ALPHA_FACTOR_WOLF_SEARCH = 3.0;
    private static final int DEFAULT_MAX_NUM_EVALUATIONS_WOLF_SEARCH = 10;

    // Steepest descent module paramters
    private int numStepsSteepestDecent;
    private double initStepSteepestDecent;
    private double stepSizeReductionSteepestDecent;
    private double stepSizeExpansionSteepestDecent;
    private static final int DEFAULT_NUM_STEP_STEEPEST_DECENT = 250;
    private static final double DEFAULT_INIT_STEP_STEEPEST_DECENT = 0.00000001;
    private static final double DEFAULT_STEP_SIZE_REDUCTION_STEEPEST_DECENT = 0.5;
    private static final double DEFAULT_STEP_SIZE_EXPENTION_STEEPEST_DECENT = 1.1;
		
    
    public LBFGS(TotalEnergy energy, CommandList commands) {
	super(energy, commands);
	setParameters(DEFAULT_ALLOWED_MAX_R_FACTOR * energy.coordinates().length,
	     DEFAULT_MAX_NUM_KICK_STARTS,
		 DEFAULT_M,
		 DEFAULT_USE_GAMA,
	     DEFAULT_C1, DEFAULT_C2,
	     DEFAULT_EXTENDED_ALPHA_FACTOR_WOLF_SEARCH,
	     DEFAULT_MAX_NUM_EVALUATIONS_WOLF_SEARCH,
	     DEFAULT_NUM_STEP_STEEPEST_DECENT, DEFAULT_INIT_STEP_STEEPEST_DECENT,
	     DEFAULT_STEP_SIZE_REDUCTION_STEEPEST_DECENT, 
	     DEFAULT_STEP_SIZE_EXPENTION_STEEPEST_DECENT);
    }
	
    public LBFGS(TotalEnergy energy) {
	this(energy,
	     DEFAULT_TOLERANCE,
	     DEFAULT_MAX_ITERATIONS,
	     DEFAULT_REPORT_EVERY,
	     DEFAULT_ALLOWED_MAX_R_FACTOR * energy.coordinates().length,
	     DEFAULT_MAX_NUM_KICK_STARTS,
		 DEFAULT_M,
		 DEFAULT_USE_GAMA,
	     DEFAULT_C1, DEFAULT_C2,
	     DEFAULT_EXTENDED_ALPHA_FACTOR_WOLF_SEARCH,
	     DEFAULT_MAX_NUM_EVALUATIONS_WOLF_SEARCH,
	     DEFAULT_NUM_STEP_STEEPEST_DECENT, DEFAULT_INIT_STEP_STEEPEST_DECENT,
	     DEFAULT_STEP_SIZE_REDUCTION_STEEPEST_DECENT, 
	     DEFAULT_STEP_SIZE_EXPENTION_STEEPEST_DECENT);
    }

    public LBFGS(TotalEnergy energy,
		double tolerance, 
		int maxIteration,
		int reportEvery){
	this(energy,
	     tolerance,
	     maxIteration,
	     reportEvery,
	     DEFAULT_ALLOWED_MAX_R_FACTOR * energy.coordinates().length,
	     DEFAULT_MAX_NUM_KICK_STARTS,
		 DEFAULT_M,
		 DEFAULT_USE_GAMA,
	     DEFAULT_C1, DEFAULT_C2,
	     DEFAULT_EXTENDED_ALPHA_FACTOR_WOLF_SEARCH,
	     DEFAULT_MAX_NUM_EVALUATIONS_WOLF_SEARCH,
	     DEFAULT_NUM_STEP_STEEPEST_DECENT, DEFAULT_INIT_STEP_STEEPEST_DECENT,
	     DEFAULT_STEP_SIZE_REDUCTION_STEEPEST_DECENT, 
	     DEFAULT_STEP_SIZE_EXPENTION_STEEPEST_DECENT);
    }

	// to be used with more detailed parameters
    public LBFGS(TotalEnergy energy,
		double tolerance, 
		int maxIteration,
		int reportEvery,
		int m,
		boolean useGama){
	this(energy,
	     tolerance,
	     maxIteration,
	     reportEvery,
	     DEFAULT_ALLOWED_MAX_R_FACTOR * energy.coordinates().length,
	     DEFAULT_MAX_NUM_KICK_STARTS,
	     m,
	     useGama,
	     DEFAULT_C1, DEFAULT_C2,
	     DEFAULT_EXTENDED_ALPHA_FACTOR_WOLF_SEARCH,
	     DEFAULT_MAX_NUM_EVALUATIONS_WOLF_SEARCH,
	     DEFAULT_NUM_STEP_STEEPEST_DECENT, DEFAULT_INIT_STEP_STEEPEST_DECENT,
	     DEFAULT_STEP_SIZE_REDUCTION_STEEPEST_DECENT, 
	     DEFAULT_STEP_SIZE_EXPENTION_STEEPEST_DECENT);
    }
	     
   //Full constructor
   private LBFGS(TotalEnergy energy,
                 double tolerance,
                 int maxIteration,
                 int reportEvery,
                 int allowedMaxR,
                 int maxNumKickStarts,
                 int m,
                 boolean useGama,
                 double c1, double c2,
                 double extendAlphaFactorWolfSearch,
                 int maxNumEvaluationsWolfSearch,
                 int numStepsSteepestDecent, double initStepSteepestDecent,
                 double stepSizeReductionSteepestDecent,
                 double stepSizeExpansionSteepestDecent) {
    	super(energy,tolerance,maxIteration);
	if (maxIteration <=  numStepsSteepestDecent) 
	    throw new RuntimeException(" numStepsSteepestDecent "+numStepsSteepestDecent+
				       " >= maxIteration "+ maxIteration+"\n"+
				       " please use SteepstDecent class instead.");
//	if (m >=  energy.coordinates().length/2) 
//	    throw new RuntimeException("m too large for this settings: \n"+
//				       "if m ("+m+") >= n/2 ("+ energy.coordinates().length/2 +") "+
//				       "then you are better off using the regular BFGS.");
	System.out.println("LBFGS starts with "+energy.coordinates().length+" coordinates");
    	this.reportEvery = reportEvery;
	this.allowedMaxR = allowedMaxR;
    	this.maxNumKickStarts = maxNumKickStarts;
	this.m = m;
	this.useGama = useGama;
    	this.c1 = c1;
    	this.c2 = c2;
    	this.extendAlphaFactorWolfSearch = extendAlphaFactorWolfSearch;
    	this.maxNumEvaluationsWolfSearch = maxNumEvaluationsWolfSearch;
    	this.numStepsSteepestDecent = numStepsSteepestDecent;
		if (this.numStepsSteepestDecent<1)
			this.numStepsSteepestDecent = 1;  
    	this.initStepSteepestDecent = initStepSteepestDecent;
    	this.stepSizeReductionSteepestDecent = stepSizeReductionSteepestDecent;
    	this.stepSizeExpansionSteepestDecent = stepSizeExpansionSteepestDecent;
    }
   //Full constructor
   private void setParameters(
		   int allowedMaxR,
		   int maxNumKickStarts,
		   int m,
		   boolean useGama,
		   double c1, double c2,
		   double extendAlphaFactorWolfSearch,
		   int maxNumEvaluationsWolfSearch,
		   int numStepsSteepestDecent, double initStepSteepestDecent,
		   double stepSizeReductionSteepestDecent,
		   double stepSizeExpansionSteepestDecent) {
	if (maxIteration <=  numStepsSteepestDecent) 
	    throw new RuntimeException(" numStepsSteepestDecent "+numStepsSteepestDecent+
				       " >= maxIteration "+ maxIteration+"\n"+
				       " please use SteepstDecent class instead.");
//	if (m >=  energy.coordinates().length/2) 
//	    throw new RuntimeException("m too large for this settings: \n"+
//				       "if m ("+m+") >= n/2 ("+ energy.coordinates().length/2 +") "+
//				       "then you are better off using the regular BFGS.");
 System.out.println("LBFGS starts with "+energy.coordinates().length+" coordinates");
	   this.allowedMaxR = allowedMaxR;
    	this.maxNumKickStarts = maxNumKickStarts;
	this.m = m;
	this.useGama = useGama;
    	this.c1 = c1;
    	this.c2 = c2;
    	this.extendAlphaFactorWolfSearch = extendAlphaFactorWolfSearch;
    	this.maxNumEvaluationsWolfSearch = maxNumEvaluationsWolfSearch;
    	this.numStepsSteepestDecent = numStepsSteepestDecent;
		if (this.numStepsSteepestDecent<1)
			this.numStepsSteepestDecent = 1;  
    	this.initStepSteepestDecent = initStepSteepestDecent;
    	this.stepSizeReductionSteepestDecent = stepSizeReductionSteepestDecent;
    	this.stepSizeExpansionSteepestDecent = stepSizeExpansionSteepestDecent;
    }
               
    private void init() {
	steepestDecent = new SteepestDecent(energy,tolerance,numStepsSteepestDecent,reportEvery,initStepSteepestDecent,
					    stepSizeReductionSteepestDecent,stepSizeExpansionSteepestDecent);
	lineSearch = new WolfConditionLineSearch(energy,c1,c2,
						 extendAlphaFactorWolfSearch,maxNumEvaluationsWolfSearch);
        coordinates = energy.coordinates();
        n = coordinates.length;
	
    	bufferCoordinates = new double[n][2];  
    	X = new double[n];
    	G = new double[n];  
	
	R = new double[n];
	Q = new double[n];
	alpha = new double[m];
	gama = 1;
	
	storage = new LinkedList<>();
		
	numKickStarts = 0;
	iterationNum = 0;
    }
    
    public String run() throws MinimizerException,LineSearchException {
	
	int i,j; // auxilary counters
	String str; // auxilary string	 
	
    	init();
	isConverged = 0; 
	
        // Starting the LBFGS minimization by a few steepest descent steps, followed by inverse Hessian, gradients (G),
        // and position (X) initialization 
    	kickStart();
    	// The main LBFGS loop
	    double magnitudeForce = 100000000;
	    while ((iterationNum < maxIteration) &&
	       ((magnitudeForce = TotalEnergy.getGradMagnitude(coordinates)) > tolerance) &&
	       (! terminator.dead())) {
		    int bfgsError = 0;
	    
	    // Gama as Hk0: 9.6 in page 226
	    if (useGama) {
		double a=0, b=0;
		if (!storage.isEmpty()) {
		    Element e = storage.getFirst();
		    for (i=0 ; i<n ; i++) {
			a += e.S[i]*e.Y[i];
			b += e.Y[i]*e.Y[i];
		    }
		    gama = a/b;
		} else 
		    gama = 1;
			}
			
			// algorithm 9.1 p. 225 ("L-BFGS two-loop recursion" ...which isn't recursive)
			for (j=0; j<n; j++)
				Q[j] = G[j];
		
		    Element e1;
		    for (i=0; i<storage.size(); i++) {
				e1 = storage.get(i);
				alpha[i] = 0;
				for (j=0; j<n; j++)
					alpha[i] += e1.S[j]*Q[j];
				alpha[i] *= e1.curv;
				for (j=0; j<n; j++)
					Q[j] -= alpha[i]* e1.Y[j];
			}

			for (j=0; j<n; j++)
				R[j] = gama*Q[j];
			
			for (i=storage.size()-1; i>=0; i--) {
				e1 = storage.get(i);
				double beta = 0;
				for (j=0; j<n; j++)
					beta += e1.Y[j]*R[j];
				beta *= e1.curv;
				for (j=0; j<n; j++)
					R[j] += e1.S[j] * (alpha[i] - beta);
			}	
			// end alg. 9.1
			// now Pk = -R

        	// Do the line search
        	try
        	{
        		for (i=0; i<n ; i++) {
        			bufferCoordinates[i][0] = coordinates[i][0];
        			bufferCoordinates[i][1] = -R[i];
        		}
				lineSearch.findStepLength(bufferCoordinates);
        	}
        	catch (LineSearchException e)
        	{
        		// return the energy coordinates to those before the line search 
		    System.out.println("exception code =  "+e.code);
        		for (i=0; i<n ; i++) 
        			coordinates[i][0] = bufferCoordinates[i][0];
        		energy.evaluate();
        		// handling the error
        		bfgsErrorString = e.getMessage();
        		switch (e.code) {
        				case 0:
        					throw e;
        				case 1:
        					bfgsError = 1; //Not a descent direction.
        					break;
        				case 2:
        					bfgsError = 2; // Wolf conditions not met.
        					break;
        				default:
        					throw e;
        	    }
        	}

        	// Calculate Gk+1,Sk,Yk and the curvature Yk*Sk. Check for pathological curvature
        	if (bfgsError == 0) {
				if (storage.size() < m) {
					e1 = new Element();
					e1.Y = new double[n];
					e1.S = new double[n];
				} else {
					e1 = storage.removeLast();
				}
        		e1.curv = 0;
        		for (j=0 ; j<n ; j++) {
        			e1.Y[j] = -coordinates[j][1] - G[j];
        			e1.S[j] = coordinates[j][0] - X[j];
        			G[j] = -coordinates[j][1];
        			X[j] = coordinates[j][0];
        			e1.curv += e1.Y[j]* e1.S[j];
        		}
        		if (e1.curv > allowedMaxR)
        			bfgsError = 3;
        		else {
        			e1.curv = 1/ e1.curv;
					storage.addFirst(e1);
				}        	
			}
       		iterationNum++;

        	// If any of the correct BFGS conditions is not met, the simulation is kick-started or terminated.
        	if (bfgsError != 0) {
        		if (numKickStarts < maxNumKickStarts) {
        			str = "\n\n WARNING: had to restart because ";
					switch (bfgsError) {
        				case 1:
        					str += "The search direction is not a descent direction\n";
        					break;
        				case 2:
        					str += "The Wolf condition was not satisfied in the line search\n";
        					str += bfgsErrorString;
        					break;
        				case 3:
        					str += "The inverse Hessian is very badly scaled, and is unreliable\n";
        					break;
        				default:
        					str += "of Unknown cause\n";
        			}
					System.out.println(str);
					kickStart();
        			numKickStarts++;
        		}
        		else {
			    energy.atomList().print();
        			str = "\n\nThe simulation was restarted for " + maxNumKickStarts + " times which is more than allowed.\n" +
        				"So many restarts are indicative of an ill-shaped energy function or an energy differentiation\n" +
        				"problem. The last restart was caused by:\n";
        			switch (bfgsError) {
        				case 1:
        					str += "Search direction is not a descent direction\n";
        					break;
        				case 2:
        					str += "The Wolf condition was not satisfied in the line search\n";
        					str += bfgsErrorString;
        					break;
        				case 3:
        					str += "The inverse Hessian is very badly scaled, and is unreliable\n";
        					break;
        				default:
        					str += "Unknown\n";
        			}
        			str += "The current minimizer run is aborted, and the minimum was not reached.\n";
        			throw new MinimizerException(3,str);
        		}
        	} 
            // Issue a report
            if (iterationNum%reportEvery == 0)
            	System.out.println(energy.report(iterationNum));
        }

	String finalMessage;
	if (terminator.dead()) {
	    finalMessage = terminator.message();
	    isConverged = 0;
	}
	else if (magnitudeForce > tolerance) {
	    finalMessage = "MNC";
	    isConverged = 0;
	}
	else {
	    finalMessage = "MC";
	    isConverged = 1;
	}
	return energy.report(iterationNum)+finalMessage;
                                               
    }
    
    
	// Starting the LBFGS minimization by a few steepest descent steps, followed by inverse Hessian initialization 
	private void kickStart() throws MinimizerException, LineSearchException {
		System.out.println(
				"\nA kick start has occurred in iteration:"
					+ iterationNum
					+ "\n");
		steepestDecent.run();
		iterationNum += numStepsSteepestDecent;
		lineSearch.Reset(steepestDecent.lastStepLength());
		energy.evaluate();
		for (int i = 0; i < n; i++) {
			X[i] = coordinates[i][0];
			G[i] = -coordinates[i][1];
		}
		storage.clear();
	}
		

    public String toString() {
	return ("LBFGS\n"+
		"\t maxIteration \t"+maxIteration+"\n"+
		"\t tolerance \t"+tolerance);
    }
}
	
		
