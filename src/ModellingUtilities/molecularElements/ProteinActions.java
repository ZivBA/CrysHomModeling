package ModellingUtilities.molecularElements;

import ModellingTool.RunParameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ScoreUtilities.ScoringGeneralHelpers.*;

/**
 * Created by zivben on 04/08/15.
 */
public class ProteinActions {

	public static void stripAndAllALAToObject(SimpleProtein sourceProtein, RunParameters params) {
		TreeSet<Character> toStrip = new TreeSet<>(Arrays.asList(params.getChainsToStrip()));
		for (SimpleProtein.ProtChain chain : sourceProtein) {
			if (toStrip.contains(chain.getChainID())) {
				for (AminoAcid acid : chain) {
					acid.substituteWith("ALA");
					acid.strip();
				}
			}

		}

	}

	/**
	 * strips a PDB file from all residue atoms except basic backbone (Ca, C, N, O) and replaces all AA to
	 * ALA. saves to file for all to enjoy.
	 *
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	public static void stripAndAllALAToFile(File source, File dest) throws IOException {


		List<String> PDBStrArr = Files.readAllLines(source.toPath(), Charset.defaultCharset());
		Iterator<String> PDBIter = PDBStrArr.iterator();
		Pattern acceptableNames = Pattern.compile("\\s*(" + ALLOWED_ATOMS + ")\\s*");
		Matcher matchNames = acceptableNames.matcher("");
		FileWriter writer = new FileWriter(dest);
		while (PDBIter.hasNext()) {
			String line = PDBIter.next() + "\n";
			if (line.startsWith("ATOM")) {
				if (matchNames.reset(line.substring(ATOM_NAME_START, ATOM_NAME_END + 1)).matches()) {
					line = line.substring(0, RES_NAME_START) + aAcids[0] + line.substring(RES_NAME_END + 1);
					writer.write(line);
				}
			} else {
				writer.write(line);
			}


		}
		writer.close();
	}

	public static File iterateAcids(SimpleProtein sourceProtein, RunParameters params) throws IOException {


		File outputFolder = makeSubFolderAt(sourceProtein.getSource(), sourceProtein.getFileName());
		outputFolder = makeSubFolderAt(outputFolder, "_scwrlFiles");

		SimpleProtein.ProtChain chainToProcess = sourceProtein.getChain(params.getChainToProcess());
		Character[] homologChains = params.getSymmetricHomologues();

		File ChainFolder = makeSubFolderAt(outputFolder, String.valueOf(params.getChainToProcess()));


		for (AminoAcid aminoAcid : chainToProcess) {
			String originalAcidType = aminoAcid.getName();

			for (String newAcid : aAcids) {
				aminoAcid.substituteWith(newAcid);
				for (Character homologChain1 : homologChains) {
					sourceProtein.getChain(homologChain1).getAminoAcidAt(aminoAcid.getPosition()).substituteWith(newAcid);
				}

				// write new processed file
				File fileWithNewRes = new File(ChainFolder.getAbsolutePath() + File.separator + sourceProtein
						.getFileName() + "_res_" + aminoAcid.getPosition() + "_to_" + newAcid + PDB_EXTENSION);
				//				File scwrledWithNewRes = new File(fileWithNewRes.getAbsolutePath().replace(".pdb", "_SCWRLed.pdb"));
				if (params.isDebug()) {
					System.out.println("Generating permutation: " + fileWithNewRes.getName());
				}
				sourceProtein.writePDB(fileWithNewRes);
				

				//reset to original type
				aminoAcid.substituteWith(originalAcidType);
				aminoAcid.strip();
				for (Character homologChain : homologChains) {
					sourceProtein.getChain(homologChain).getAminoAcidAt(aminoAcid.getPosition()).substituteWith(originalAcidType);
					sourceProtein.getChain(homologChain).getAminoAcidAt(aminoAcid.getPosition()).strip();
					
				}


			}


		}


		return outputFolder;

	}


	/**
	 * take a protein and start iterating all over it's residues. at each position create a new file with
	 * that residue replaced to each of the 20 AA.
	 *
	 * @param prot         input protein
	 * @param outputFolder output folder
	 * @throws IOException
	 */
	public static void iterateAllAcidsToFile(SimpleProtein prot, File outputFolder) throws IOException {

		// go over all AA in the protein
		for (SimpleProtein.ProtChain chain : prot) {
			for (AminoAcid aminoAcid : chain) {

				// for each Amino acid, replace with every possible AA
				for (String acid : aAcids) {
					aminoAcid.substituteWith(acid);
					// write new processed file
					File fileWithNewRes = new File(outputFolder.getAbsolutePath() + File.separator + prot
							.getFileName() + "_res_" + aminoAcid.getPosition() + "_to_" + acid +PDB_EXTENSION);

					prot.writePDB(fileWithNewRes);

					//reset to ALA
					aminoAcid.substituteWith("ALA");

				}
			}

		}
	}

	public static char resToSingleLetter(String name) throws InvalidPropertiesFormatException {

		if (name.trim().length() > 1) {
			for (int i = 0; i < aAcids.length; i++) {
				if (aAcids[i].equalsIgnoreCase(name)) {
					return singleLetters[i];
				}

			}
		} else {
			
			for (char singleLetter : singleLetters) {
				if (singleLetter == name.trim().toUpperCase().charAt(0)) {
					return singleLetter;
				}
				
			}
		}

		if (name.equals("SEC") || name.trim().equals("U")) {
			return 1;
			//TODO - fix this 21st amino acid thing.
		} else
			throw new InvalidPropertiesFormatException("Bad AminoAcid Name: " + name);


	}

	public static int acidToIndex(String name) throws InvalidPropertiesFormatException {

		for (int i = 0; i < aAcids.length; i++) {
			if (name.trim().equalsIgnoreCase(aAcids[i]) || name.trim().equalsIgnoreCase(
					String.valueOf(singleLetters[i]))) {
				return i;
			}
		}
		// failsafe for noncommon acids, add cases if necessary.
		if (name.equals("SEC") || name.trim().equals("U")) {
			return 1;
			//TODO - fix this 21st amino acid thing.
		} else
			throw new InvalidPropertiesFormatException("Bad AminoAcid Name: " + name);


	}
}