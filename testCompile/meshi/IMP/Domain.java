package meshi.IMP;

import java.util.StringTokenizer;

public class Domain {

	private final String protName;
	private final String domainName;
	private final int[][] residues;
	private double x,y,z;
	
	public Domain(String protName , String domainName, int[][] residues) {
		this.protName = protName;
		this.domainName = domainName;
		this.residues = residues;
	}
	
	Domain(String oneLongString) {
		StringTokenizer st = new StringTokenizer(oneLongString);
		this.protName = st.nextToken();
		this.domainName = st.nextToken();
		residues = new int[st.countTokens()/2][2];
		for (int c=0 ; c<residues.length ; c++) {
			residues[c][0] = Integer.valueOf(st.nextToken());
			residues[c][1] = Integer.valueOf(st.nextToken());
		}
	}

	public String domainName() {
		return domainName;
	}

	public String proteinName() {
		return protName;
	}
	
	public int numberOfResidues() {
		int counter=0;
		for (int[] residue : residues) {
			counter += (residue[1] - residue[0]);
		}
		return counter;
	}
	
	public boolean isResNumInDomain(int resNum) {
		boolean inDom = false;
		for (int[] residue : residues) {
			if ((resNum <= residue[1]) && (resNum >= residue[0])) {
				inDom = true;
			}
		}
		return inDom;
	}

	public String toString() {
		String str = protName+" "+domainName;
		for (int[] residue : residues) {
			str += (" " + residue[0] + " " + residue[1]);
		}
		return str + "\n";
	}

	public int[][] domainResidues() {
		return residues;
	}

	public double x() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double y() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double z() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public void setXYZ(double x,double y,double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int firstRes() {
		return residues[0][0];		
	}

	public int lastRes() {
		return residues[residues.length-1][1];		
	}

}
