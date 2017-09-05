package meshi.applications.loopBuilding;

import java.util.Vector;

class BasicLoopResultVector extends Vector<BasicLoopResult> {

private static final long serialVersionUID = 1L;

public boolean hasNoSimilar(double[][] refLoopCoors, double rmsCriterion) {
	if (rmsCriterion<-0.1)
		return true;
	for (BasicLoopResult basicLoopResult : this)
		if (basicLoopResult.calcRMS(refLoopCoors) < rmsCriterion)
			return false;
	return true;
}

}
