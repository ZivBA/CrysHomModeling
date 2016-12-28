package meshi.geometry;

import meshi.util.Double.DoubleList;

public class FrozenCoordinates extends Coordinates {
	public   FrozenCoordinates(Coordinates coordinates) {
	super(coordinates.x(),
	      coordinates.y(),
	      coordinates.z());
		double INFINITY = Double.POSITIVE_INFINITY;
		x[1] = INFINITY;
	y[1] = INFINITY;
	z[1] = INFINITY;
    }
    public void setFx(double fx) {}
    public void setFy(double fy) {}
    public void setFz(double fz) {}
    public void resetForces() {}
}
