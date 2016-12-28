package meshi.IMPmy;

import meshi.molecularElements.Atom;
import meshi.molecularElements.AtomList;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

public class Domain {
	
	private final Vector<AnchorPosition> positions = new Vector<>();
	private AnchorPosition center = null;
	private final String proteinName;
	private final String domainName;
	private final int[][] residues;
	private final boolean structured;
	
	public Domain(String protName , String domainName, int[][] residues , boolean structured) {
		this.proteinName = protName;
		this.domainName = domainName;
		this.residues = residues;
		this.structured = structured;
	}
	
	public Domain(String oneLongString) {
		StringTokenizer st = new StringTokenizer(oneLongString);
		this.proteinName = st.nextToken();
		this.domainName = st.nextToken();
		residues = new int[(st.countTokens()-1)/2][2];
		for (int c=0 ; c<residues.length ; c++) {
			residues[c][0] = Integer.valueOf(st.nextToken());
			residues[c][1] = Integer.valueOf(st.nextToken());
		}
		String structuredString = st.nextToken();
		structured = structuredString.charAt(0) == 'S';
	}

	public String domainName() {
		return domainName;
	}

	public String proteinName() {
		return proteinName;
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
		String str = proteinName+" "+domainName;
		for (int[] residue : residues) {
			str += (" " + residue[0] + " " + residue[1]);
		}
		return str + "Radius:" + center().R() + "\n";
	}
	
	public int firstRes() {
		return residues[0][0];		
	}

	public int lastRes() {
		return residues[residues.length-1][1];		
	}
	
	public AnchorPosition center() {
		return  center;
	}
	
	public Vector<AnchorPosition> positions() {
		return  positions;
	}
	
	public boolean structured() {
		return structured;
	}
	
	private AnchorPosition findPosition(int resNum) {
		for (AnchorPosition pos : positions) {
			if (pos.resNum() == resNum) {
				return pos;
			}
		}
		return null;
	}
	
	public AnchorPosition addPosition(AtomList model, int res) {
		// Find the closest residue in the model to 'res'
		int closestRes = Integer.MAX_VALUE;
		Atom closestCA = null;
		for (int c=0 ; c<model.size() ; c++) {
			if (model.atomAt(c).name().equals("CA")) {
				if (Math.abs(model.atomAt(c).residueNumber()-res) < Math.abs(closestRes-res)) {
					if (isResNumInDomain(model.atomAt(c).residueNumber())) {
						closestCA = model.atomAt(c);
						closestRes = model.atomAt(c).residueNumber();
					}
				}
			}
		}
		int MAX_SEPERATION_ANCHOR_TO_STRUCTURED_RESIDUE = 10;
		if (Math.abs(closestRes - res) > MAX_SEPERATION_ANCHOR_TO_STRUCTURED_RESIDUE) {
			throw new RuntimeException("\n\nA sepration of: " + Math.abs(closestRes - res) + " from anchor to nearest structured residue.\n\n");
		}
		if (findPosition(closestRes)==null) {
			AnchorPosition newPosition = new AnchorPosition(proteinName , domainName, closestRes, closestCA.x(), closestCA.y(), closestCA.z());
			positions.add(newPosition);
			return newPosition;
		} else {
			return findPosition(closestRes);
		}
	}
	
	public void setCenter(AtomList model) {
		double cmx, cmy, cmz; // center of mass x, y and z
		cmx = cmy = cmz = 0.0;
		int atomCounter = 0;
		for (int c=0; c<model.size() ; c++) {
			if (model.atomAt(c).name().equals("CA")) {
				if (isResNumInDomain(model.atomAt(c).residueNumber())) {
					cmx += model.atomAt(c).x();
					cmy += model.atomAt(c).y();
					cmz += model.atomAt(c).z();
					atomCounter++;
				}
			}
		}
		cmx /= atomCounter;
		cmy /= atomCounter;
		cmz /= atomCounter;
		center = new AnchorPosition(proteinName, domainName, -1 , cmx,cmy,cmz);
	}

	public void setCenter(AnchorPosition pos) {
		center = pos;
	}
	
	public void moveCenterTo(double newX, double newY, double newZ) {
		double deltaX = newX - center().x();
		double deltaY = newY - center().y();
		double deltaZ = newZ - center().z();
		center.setXYZ(newX, newY, newZ);
		for (AnchorPosition pos : positions()) {
			pos.addX(deltaX);
			pos.addY(deltaY);
			pos.addZ(deltaZ);
		}
	}
	
	public double updateRtoCenter() {
		double[] Rs = new double[positions().size()];		
		for (int c=0 ; c<positions().size() ; c++) {
			Rs[c] = Math.sqrt((positions().get(c).x()-center().x())*(positions().get(c).x()-center().x()) +
					(positions().get(c).y()-center().y())*(positions().get(c).y()-center().y()) +
					(positions().get(c).z()-center().z())*(positions().get(c).z()-center().z()));
		}
		// currently returning the median
		Arrays.sort(Rs);
		double newR = 1.5*Rs[Rs.length/2]; 
		System.out.print("Add radius: " + newR + " to " + this);
		center().setR(newR);
		return newR;
	}
			
}
