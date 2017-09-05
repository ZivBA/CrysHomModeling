package meshi.IMP;

class CrossLinkedLysine {

	private final int resNum;
	private final double x;
	private final double y;
	private final double z;
	
	public CrossLinkedLysine(int resNum, double x, double y, double z) {
		this.resNum = resNum;
		this.x = x;
		this.y = y;
		this.z = z;	
	}

	public int resNum() {
		return resNum;
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public double z() {
		return z;
	}
	

}
