
package meshi.energy.hydrogenBond;

// --Commented out by Inspection START (16:34 31/10/16):
///*
// * List of Distance elements that represent pairs of Hydrogen and Oxygen
// */
//public class HydrogenOxygenPairsList extends SortableMeshiList {
//
//    protected IsHO isHO = new IsHO();
//    private static final IsGoodHOPair isGoodHOPair = new IsGoodHOPair();
//
//	public HydrogenOxygenPairsList() {
//		super(isGoodHOPair);
//	}
//
//    public Iterator iterator() {
//        return new HOPairIterator(this);// HOPairIterator is an internal class.
//    }
//
//    //--------------------------- internal class IsNot13 ----------------------------------
//
//    /* this filter Accept Distance between Atoms just if there are at list 3 atoms away on the sequense,
//     *  (atomes that are less then 3 atoms away on the sequense never create Hydrogen bonds with each other)
//    */
//    static class IsNot13 implements Filter{
//        public boolean accept(Object obj) {
//            Distance dis = (Distance)obj;
//        	if (!dis.atom1().chain().equalsIgnoreCase(dis.atom1().chain()))
//        		return true;
//            int residueNumber1 = dis.atom1().residueNumber();
//            int residueNumber2 = dis.atom2().residueNumber();
//            return (!(Math.abs(residueNumber1-residueNumber2) <= 2));
//        }
//    }
//
//    //--------------------------- internal class GoodResiduesForHB --------------------------------
//
//    static class IsGoodPair implements Filter{
//    	private final IsHO isHO = new IsHO();
//    	private final IsNot13 isNot13 = new IsNot13();
//    	private final GoodSS goodSS = new GoodSS();
//
//    	public boolean accept(Object obj) {
//            Distance dis = (Distance)obj;
//            return isHO.accept(obj) && isNot13.accept(obj) && goodSS.accept(obj);
//        }
//    }
//
//    //--------------------------- internal class GoodSS ---------------------------
//
//    static class GoodSS implements Filter{
//    	public boolean accept(Object obj) {
//            Distance dis = (Distance)obj;
//    		String atom1SS = dis.atom1().residue().secondaryStructure();
//    		String atom2SS = dis.atom2().residue().secondaryStructure();
//    		if (atom1SS == "COIL" || atom2SS == "COIL")
//    			return false;
//    		if (atom1SS == "HELIX" && atom2SS == "HELIX")
//    		{
//    			int residueNumber1 = dis.atom1().residueNumber();
//        		int residueNumber2 = dis.atom2().residueNumber();
//    			return (Math.abs(residueNumber1-residueNumber2) == 4);
//    		}
//            return !((atom1SS == "HELIX" && atom2SS == "SHEET") ||
//                    (atom1SS == "SHEET" && atom2SS == "HELIX"));
//        }
//    }
//
//    //--------------------------- internal class IsGoodHOPair ---------------------------
//
//    static class IsGoodHOPair implements Filter{
//        public boolean accept(Object obj) {
//         	return  (obj instanceof Distance);
//          }
//    }
//
//    //--------------------------- internal class IsWithInRmax ---------------------------
//
//    static class IsWithInRmax implements Filter{
//
//        private double dis;
//	    private final double rMax;
//        public IsWithInRmax(){
//            super();
//            rMax = DistanceMatrix.rMax();
//        }
//
//        public boolean accept(Object obj) {
//            Distance distance = (Distance)obj;
//            return ((rMax-distance.distance()) >= 0);
//        }
//    }
//
//
//
//    //--------------------------- internal class HOPairIterator ---------------------------
//
//    private  class HOPairIterator extends MeshiIterator {
//        final IsWithInRmax isWithInRmax = new IsWithInRmax();
//
//        public HOPairIterator(MeshiList list){
//            super(list);
//        }
//
//        /*
//         * @return the next element that itas 2 atoms are within Rmax or Null if there is no such element
//         */
//        public Object next() {
//            return super.next(isWithInRmax);
//        }
//            /*            if (modCount != list.modCount())
//                throw new RuntimeException("List has changed - iterator is unusable");
//            HydrogenOxygenPair hoPair =  (HydrogenOxygenPair)currecnt();
//            if (hoPair != null){
//                boolean withinRmax = hoPair.withinRmax();
//                while (!withinRmax) {
//                    current++;
//                    if (current >= size) return null;
//                    hoPair =  (HydrogenOxygenPair)internalArray[current];
//                    withinRmax = hoPair.withinRmax();
//                }
//            }
//            current++;
//            return internalArray[current-1];*/
//
//    }
//}
// --Commented out by Inspection STOP (16:34 31/10/16)
