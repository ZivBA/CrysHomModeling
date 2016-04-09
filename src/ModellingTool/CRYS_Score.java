package ModellingTool;

import ModellingUtilities.molecularElements.ProteinActions;
import ModellingUtilities.molecularElements.SimpleProtein;
import ScoreUtilities.MRC_Map_New;
import ScoreUtilities.SCWRLactions;
import ScoreUtilities.ScoringGeneralHelpers;
import UtilExceptions.MissingChainID;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by zivben on 09/04/16.
 */
public class CRYS_Score {
	private final RunParameters params;
	private MRC_Map_New myMap;
	private SimpleProtein myProt;
	public char requestedChain;
	private double[][] proteinIntensityValueMatrix;
	public ArrayList<String> logFile = new ArrayList<>();
	public ArrayList<File> toDelete = new ArrayList<>();
	private List<Integer[]> originalPos;

	public CRYS_Score(RunParameters params) {
		this.params = params;
		try {

			this.myProt = new SimpleProtein(params.getPDBsrc(), params);
			//TODO new MAP object
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println("Problem processing PDB file");
		}
	}

	public int[][] getAcidDist() throws InvalidPropertiesFormatException {
		return (myProt.acidDist != null ? myProt.acidDist : myProt.calcAcidDist());
	}

	public double[][] scoreProtein() throws IOException, MissingChainID {
		// if cannot read existing results, generate new files.
		if (!checkExistingCSVs()) {

			System.out.println("Starting protein scoring, saving original positions.");
			myProt.saveOriginalPositions();
			originalPos = myProt.getOriginalPositions();


			System.out.println("Stripping all amino acid resiudes and setting to ALA.");
			ProteinActions.stripAndAllALAToObject(myProt,params);
			System.out.println("Iterating all acid permutations and creating SCWRL input files");
			File scwrlOutput = ProteinActions.iterateAcids(myProt,params);

			File chainSubFolders[] = scwrlOutput.listFiles();
			for (File chain : chainSubFolders) {
				if (chain.isDirectory()) {
					if (chain.getAbsolutePath().endsWith(File.separator + requestedChain) || requestedChain == '\0') {
						SCWRLactions.genSCWRLforFolder(chain);
						if (!params.isDebug()) {
							toDelete.add(chain);
						}
					}
				}
			}

			processSCWRLfolder(scwrlOutput, requestedChain, params.isDebug());


		}
		return proteinIntensityValueMatrix;
	}


	private boolean checkExistingCSVs() {

		//try reading CSVs
		try {
			File tempCSVfolder = ScoringGeneralHelpers.makeFolder(new File(myProt.getSource().getParent() + File
					.separator + "tempCSVs"));


			for (SimpleProtein.ProtChain chain : myProt) {
				if (chain.getChainID() == requestedChain || requestedChain == '\0') {
					File resultCSV = new File(tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
							+ "_" + chain.getChainID() + "_resultMatrix.csv");

					File backBoneIntMatrix = new File(
							tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
									+ "_" + chain.getChainID() + "_BBresultMatrix.csv");

					File correctPositions = new File(
							tempCSVfolder.getAbsolutePath() + File.separator + myProt.getFileName()
									+ "_" + chain.getChainID() + "_originalPositions.csv");

					double[][] temp;
					temp = ScoringGeneralHelpers.csvToMatrix(resultCSV);
					if (temp != null) {
						chain.resIntensityValueMatrix = temp;
					} else {
						return false;
					}
					temp = csvToMatrix(backBoneIntMatrix);
					if (temp != null) {
						chain.backBoneIntensityValueMatrix = temp;
					} else {
						return false;
					}
					double[][] tempPositions = csvToMatrix(correctPositions);
					if (tempPositions == null)
						return false;
					chain.originalPositions = new Integer[tempPositions[0].length];
					for (int i = 0; i < tempPositions.length; i++) {
						for (int j = 0; j < tempPositions[i].length; j++) {
							chain.originalPositions[j] = (int) tempPositions[i][j];
						}
					}
				}
				return true;
			}

		} catch (Exception e) {
			System.out.println("Existing CSVs not found or malformed, processing from scratch.");
			;
			return false;
		}


		return false;
	}


	private void processSCWRLfolder(File processingFolder, char requestedChain, boolean debug) throws IOException, MissingChainID {


		SimpleProtein tempProt;

		/**
		 * get input File object, read and convert to List of Files (PDB Iterations) for processing.
		 */
		List<File> chainFolders = new ArrayList<>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(processingFolder.toPath())) {
			for (Path path : directoryStream) {
				if (path.toString().endsWith(File.separator + requestedChain)) {
					chainFolders.add(path.toFile());
				} else if (requestedChain == '\0') {
					chainFolders.add(path.toFile());
				}
			}
		} catch (IOException ex) {
			System.out.println("There was a problem processing the SCWRL input folder.");
			System.err.println(ex.getLocalizedMessage());
			System.err.println(ex.getMessage());
			throw ex;
		}

		/**
		 * read each chain File object
		 */
		for (File chain : chainFolders) {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(chain.toPath())) {
				for (Path path : directoryStream) {
					tempProt = new SimpleProtein(path.toFile(),params);
					scoreSingleScwrl(tempProt);
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				System.out.println("array index OOB exception for file: " + chain.getAbsolutePath());
			} catch (IOException   ex) {
				System.out.println("array index OOB exception for file: " + chain.getAbsolutePath());
				System.err.println(ex.getMessage());
				throw ex;
			}


		}
	}

	private void scoreSingleScwrl(SimpleProtein tempProt) {
		//TODO process SCWRL file with SFCHECK
	}


}
