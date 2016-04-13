package ModellingUtilities.molecularElements;

import ModellingTool.RunParameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

import static ScoreUtilities.ScoringGeneralHelpers.*;

/**
 * Created by Ziv_BA on 30/07/2015.
 */
public class SimpleProtein implements Iterable<SimpleProtein.ProtChain> {

	private File source;
	private String fileName;
	private char[] symmetricHomologues;
	private char[] chainsToStrip;
	private char chainToProcess;
	private boolean keepHetAtm;

	private List<Integer[]> protOriginalPositions = new ArrayList<>();
	private String crysHeader;
	private List<ProtChain> protChains; // list of the seperate AminoAcid chains
	private List<String> hetAtm;   // array of the remaining HeteroAtoms.
	private List<String> footers;   // array of the remaining footer tags.
	private int numChains;
	public int[][] acidDist;

	public void setKeepHetAtm(boolean keepHetAtm) {
		this.keepHetAtm = keepHetAtm;
	}

	/**
	 * constructor for SimpleProtein from PDB file.</br>
	 * actually uses the constHelper helper method to create the object, seperated due to need for this process elsewhere.
	 *
	 * @param file   File Object pointing to a PDB file.
	 * @param params RunParameters object holding the program arguments.
	 * @throws IOException if an error occurs while reading the file.
	 */


	public SimpleProtein(File file, RunParameters params) throws IOException {
		keepHetAtm = params.isHetAtmProcess();
		constHelper(file, params);
	}

	/**
	 * creates atoms for each ATOM line, combine atoms by resSeq as residues, all residues of a specific
	 * chain in a ProtChain object. each chain ends with a TER line. </br>
	 * hetero atoms, master tag and end tag are all injected into the hetAtmAndFooter String array without
	 * processing.
	 *
	 * @param pdbFile File Object pointing to a PDB file.
	 * @param params
	 * @throws IOException if an error occurs while reading the file.
	 */
	private void constHelper(File pdbFile, RunParameters params) throws IOException {
		source = pdbFile;
		fileName = pdbFile.getName().substring(0, pdbFile.getName().indexOf(PDB_EXTENSION));

		protChains = new ArrayList<>();
		// create a chain 2D array and the first chain list.
		List<ArrayList<String>> chains = new ArrayList<>();
		chains.add(new ArrayList<String>());

		List<String> heteroAtoms = new ArrayList<>();
		List<String> footerAtms = new ArrayList<>();
		int chainCounter = 0;

		// read the PDB file into a textual array to be processed line by line.
		List<String> PDBStrArr = Files.readAllLines(pdbFile.toPath(), Charset.defaultCharset());


		// iterate over the entire PDB file, populate the chain lists with ATOM lines, and hetAtom with
		// HETATM lines.
		// for each TER entry, add a new list for that chain.
		for (int i = 0; i < PDBStrArr.size(); i++) {
			String lineInFile = PDBStrArr.get(i);
			if (lineInFile.startsWith("ATOM")) {

				try {
					chains.get(chainCounter).add(lineInFile);
				} catch (StringIndexOutOfBoundsException e) {
					System.out.println("string index OOB exception at line:\n" + lineInFile);
					System.out.println("from file: " + pdbFile.getName());
				}
			} else if (lineInFile.startsWith("CRYST1")) {
				crysHeader = lineInFile;
			}
			else if (lineInFile.startsWith("TER")) {
				chains.get(chainCounter).add(lineInFile);
				chainCounter++;
				chains.add(new ArrayList<String>());
			} else if (lineInFile.matches("(" + HETATM + ").*")) {
				heteroAtoms.add(lineInFile);
			} else if (lineInFile.matches("(" + FOOTER_TAGS + ").*")) {
				footerAtms.add(lineInFile);
			}

		}

		for (List<String> chain : chains) {
			if (chain != null && chain.size() > 1) {
				protChains.add(new ProtChain(chain));
			}
		}

		hetAtm = heteroAtoms;
		footers = footerAtms;
		numChains = chainCounter;

	}

	public String getFileName() {
		return fileName;
	}


	public File getSource() {
		return source;
	}

	public List<Integer[]> getOriginalPositions() {
		return protOriginalPositions;
	}

	/**
	 * dump all the atom sequences and footer back to a PDB file.
	 *
	 * @param destination
	 * @throws IOException
	 */
	public void writePDB(File destination) throws IOException {
		FileWriter FW = new FileWriter(destination);


		// write Crystalographic header line
		FW.write(crysHeader);
		// write structural atoms
		for (ProtChain chain : protChains) {
			for (AminoAcid acid : chain) {
				for (SimpleAtom atom : acid) {
					FW.write(atom.getOriginalString() + "\n");
				}
			}

		}
		//write HeteroAtoms and footer tags.
		if (keepHetAtm) {
			for (String line : hetAtm) {
				FW.write(line);
			}
		}
		for (String line : footers) {
			FW.write(line);
		}
		FW.close();
	}


	@Override
	public Iterator<ProtChain> iterator() {
		return protChains.iterator();
	}


	public int getLegnth() {
		int protLength = 0;
		for (ProtChain chain : protChains) {
			protLength += chain.residues.size();
		}
		return protLength;

	}

	/**
	 * save the current structure of the protein before processing begins.
	 *
	 * @throws InvalidPropertiesFormatException
	 */
	public void saveOriginalPositions() throws InvalidPropertiesFormatException {

		for (ProtChain chain : protChains) {
			List<Integer> posList = new LinkedList<>();
			for (AminoAcid res : chain.residues) {
				posList.add(res.getAcidGlobalIndex());
			}
			chain.originalPositions = posList.toArray(new Integer[posList.size()]);
			protOriginalPositions.add(chain.originalPositions);
		}


	}

	public ProtChain getChain(char requestedChainID) {
		for (ProtChain chain : this) {
			if (chain.getChainID() == requestedChainID) {
				return chain;
			}
		}
		return null;
	}

	public int getNumChains() {
		return numChains;
	}

	public int[][] calcAcidDist() throws InvalidPropertiesFormatException {
		acidDist = new int[protOriginalPositions.size()][20];
		Arrays.fill(acidDist, 0);
		int i = 0;
		for (ProtChain chain : protChains) {
			for (AminoAcid acid : chain) {
				acidDist[i][acid.getAcidGlobalIndex()] += 1;
			}
		}
		return acidDist;
	}

	/**
	 * helper class to bulk residues together in respective chains.
	 * also performs the actual processing from string array to molecular elements.
	 */
	public class ProtChain implements Iterable<AminoAcid> {
		private char chainID;
		public double[][] resIntensityValueMatrix;  // Residue intensity values from map
		public double[][] backBoneIntensityValueMatrix; // BB intensity values from map

		public double[][] allZvalueMatrix;          // ZScore values for all iterations
		public double[] trueZvalues;                // zvalues of the residues in the original protein
		public double[][] backBoneZvalueMatrix;     // ZValues for BB atoms
		public double[] backBoneZvalue;             // zvalues of the BB atoms in the original protein

		private int sequenceBias;                   // diff between seq ID of first atom and '0'
		public Integer[] originalPositions;         // acid ID of the original AAcids in the protein

		public double[] medianTrue = new double[20];
		public double[] medianFalse = new double[20];

		public double[] signalMaybe = new double[20];


		private List<AminoAcid> residues = new ArrayList<>();

		/**
		 * constructor creating a chain from a list of strings (assume all strings are for a single chain)
		 *
		 * @param sourceList
		 */
		protected ProtChain(List<String> sourceList) throws InvalidPropertiesFormatException {
			List<String> tempAtomList = new ArrayList<>();
			int workingResSeq = Integer.valueOf(sourceList.get(0).substring(RES_SEQ_START, RES_SEQ_END + 1)
					.trim());

			for (int i = 0; i < sourceList.size(); i++) {

				String tempCurrLine = sourceList.get(i);
				if (!tempCurrLine.startsWith("TER")) {
					int tempCurrResSeq = Integer.valueOf(
							tempCurrLine.substring(RES_SEQ_START, RES_SEQ_END + 1).trim());

					if (tempCurrResSeq == workingResSeq) {
						tempAtomList.add(tempCurrLine);
					} else {
						workingResSeq = tempCurrResSeq;
						residues.add(new AminoAcid(tempAtomList));
						tempAtomList.clear();
						tempAtomList.add(tempCurrLine);
					}
				}

			}
			// add last bulk of atoms.
			residues.add(new AminoAcid(tempAtomList));

			chainID = residues.get(0).getChainID();
			try {
				this.sequenceBias = residues.get(0).getSeqNum();
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Possible empty PDB file at:\n" + source.toString());
			}
			//intensity values and z values for residues
			resIntensityValueMatrix = new double[20][residues.size()];
			trueZvalues = new double[residues.size()];
			//intensity values and z values for backbone
			backBoneIntensityValueMatrix = new double[20][residues.size()];
			backBoneZvalue = new double[residues.size()];



		}

		public int getAcidSequenceID(int position) {
			return residues.get(position).getSeqNum();
		}

		public char getChainID() {
			return chainID;
		}

		protected void addRes(AminoAcid res) {
			residues.add(res);
		}

		@Override
		public Iterator<AminoAcid> iterator() {
			return residues.iterator();
		}

		public int getSequenceBias() {
			return sequenceBias;
		}

		public Integer[] getOriginalPositions() {
			return originalPositions;
		}

		public char getSingleLetter(int j) throws InvalidPropertiesFormatException {
			return residues.get(j).getSingleLetter();
		}

		public int getLength() {
			return residues.size();
		}

		public AminoAcid getAminoAcidAt(int seqNum) {
			return residues.get(seqNum);
		}
	}
}
