package meshi.geometry;

/**
 * Class ArcCos.
 *
 * @author <a href="mailto:ygleyzer@cs.bgu.ac.il">Yanik</a>
 * @version 1.0
 */
class ArcCos{
   private final static double verySmall = Math.exp(-60);
   private final static double minusVerySmall = -1*verySmall;
    private static boolean USE_FAST_ARCCOS = false;
    // size of lookup table
    private static final int LOOKUP_SIZE = 10000;
    // pre-valuated acos
    private static double[]ACOS_LOOKUP;
    /**
       Set flag for using fast acos and initialize acos lookup table
     */
    public static void useFastArcCos(){
        USE_FAST_ARCCOS = true;
        // init lookup table
        ACOS_LOOKUP = new double[LOOKUP_SIZE];
        for(int i=0;i<LOOKUP_SIZE;i++){
            ACOS_LOOKUP[i] = Math.acos((2*i)/(double)LOOKUP_SIZE - 1);
        }
    }
    /*
      Calculation of acos by Taylor series till 4-th differencial
      lookup of near value in ACOS_LOOKUP
                                      1
                 acos' x  =    - ------------
                                            2
                                  SQRT(1 - x )
                                  
                                        x
                acos'' x  =      - -----------
                                         2 3/2
                                   (1 - x )

                                   2         2
                             (1 - x )  +  3 x
            acos''' x =   - -------------------------
                                         2 5/2
                                   (1 - x )

                                        2          3
                              9 x (1 - x ) +   15 x
           acos'''' x =   - -------------------------
                                         2 7/2
                                   (1 - x )
    */
    public static double acos(double x){
	    if ((x < -0.95) | (x > 0.95)) return StrictMath.acos(x);
        if(!USE_FAST_ARCCOS) return StrictMath.acos(x);
	int ind = (int)((1+x)*LOOKUP_SIZE)/2;
        double a = (2*ind)/(double)LOOKUP_SIZE - 1;
        double a_acos = ACOS_LOOKUP[ind];
        double x_a = x-a;
        double term = (1-a*a);
	if ((term > minusVerySmall) & (term < verySmall)) return a_acos;  
        double sqrt = Math.sqrt(term);
	    //	if (Math.abs(out-StrictMath.acos(x)) > 0.00000001)
//			System.out.println("XXXXXXXXXXXXXXXXXXXX "+x+" "+out+" "+
        return a_acos
            - x_a/sqrt
            - (x_a*x_a*a)/(2*term*sqrt)
            - (x_a*x_a*x_a*(term + 3*a*a))/(6*term*term*sqrt)
            - (x_a*x_a*x_a*x_a*(9*a*term + 15*a*a*a))/(24*term*term*term*sqrt);
    }

    // error for some values of NUMBER_OF_ITERATIONS vs Math.acos() :
    private static final int NUMBER_OF_ITERATIONS = 10;
    //               Pi         N  1*3*5..(2n-1)x
    //   arccos x = ---- - x - SUM ------------------ , |x|<1
    //               2         n=1 2*4*6..(2n)(2n+1)
    public static double acos_chebychev(double x){
        if(x < -0.7 || x > 0.7)
            return Math.acos(x);
        double arccos=x;
        double a=x;
        for (int i=1;i<NUMBER_OF_ITERATIONS;i++){
            a*=x*x*(2*i-1)*(2*i-1)/((2*i+1)*2*i);
            arccos+=a;
        }
        return 1.57079632679489661923-arccos;
    }
}
