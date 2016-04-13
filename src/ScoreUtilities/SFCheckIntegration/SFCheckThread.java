package ScoreUtilities.SFCheckIntegration;

import ModellingTool.RunParameters;
import ModellingUtilities.molecularElements.SimpleProtein;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import static ScoreUtilities.ScoringGeneralHelpers.*;

/**
 * Created by zivben on 13/04/16.
 */
public class SFCheckThread implements Runnable {
	RunParameters params;
	File SFCheckExe;
	File outputFolder;
	File output;
	File protToProcess;

	public SFCheckThread(File tempProt, RunParameters params) {
		SFCheckExe = params.getSFChkexe();
		outputFolder = params.getScwrlOutputFolder();
		protToProcess = tempProt;
		this.params = params;
		output = new File(outputFolder.getAbsolutePath() + File.separator + tempProt.getName().replaceFirst(PDB_EXTENSION + "+$",
				"_SFCheck.log" ));

	}

	@Override
	public void run() {
		if (!output.isFile()) {
			try {
				output.createNewFile();
			} catch (IOException e) {
				System.err.println("cannot create temporary log file for SFCHECK");
			}
			output.setWritable(true);
		}


		Process process = null;
		try {
			process = Runtime.getRuntime().exec(SFCheckExe.getAbsolutePath() +
					"_DOC Y>" + outputFolder.getAbsolutePath() + File.separatorChar + protToProcess.getName() + "\n" +
					"_FILE_C " + protToProcess.getAbsolutePath() + "\n" +
					"_FILE_F" + params.getMAPsrc() + "\n" + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

		List stdOutput = new ArrayList();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				stdOutput.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

}