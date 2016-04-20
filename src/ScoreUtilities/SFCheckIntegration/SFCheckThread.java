package ScoreUtilities.SFCheckIntegration;

import ModellingTool.RunParameters;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static ScoreUtilities.ScoringGeneralHelpers.*;

/**
 * Created by zivben on 13/04/16.
 */
public class SFCheckThread implements Callable<String[]> {
	private RunParameters params;
	private File SFCheckExe;
	private File outputFolder;
	private File output;
	private File protToProcess;
	private static File sfCheckScript = new File("/home/zivben/IdeaProjects/CrysHomModeller/paramSFcheck.btc");

	public SFCheckThread(File tempProt, RunParameters params) throws IOException {
		SFCheckExe = params.getSFChkexe();
		outputFolder = new File(params.getScwrlOutputFolder().getAbsolutePath() + params.getChainToProcess() + "sfcheck_LogFiles");
		outputFolder = makeSubFolderAt(params.getScwrlOutputFolder().getParentFile(), "sfcheck_LogFiles");
		protToProcess = tempProt;
		this.params = params;
		output = new File(outputFolder.getAbsolutePath() + File.separator + tempProt.getName().replaceFirst(PDB_EXTENSION + "+$",
				""));

	}


	@Override
	public String[] call() throws Exception {
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
			" -f " + params.getMAPsrc() +
			" -m " + protToProcess.getAbsolutePath() +
			" -po " + output.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

		List stdOutput = new ArrayList();
		String line;

		String result = null;

		try {
			while ((line = br.readLine()) != null) {
				if (line.startsWith("Correlation factor :")){
					result = line.substring(24);
				}
			}
		} catch (IOException e){
			System.err.println(e.getMessage());
		}
//		try {
//			while ((line = br.readLine()) != null) {
//				stdOutput.add(line+"\n");
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		Writer write = new FileWriter(output);
//		write.write(String.valueOf(stdOutput));
//		write.close();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(outputFolder.toPath())) {
			for (Path path : directoryStream) {
				if (!path.toString().endsWith(".log")) {
					path.toFile().delete();
				}
			}
		}
//		return new File(output.getAbsolutePath()+"sfcheck.log");
		return new String[]{protToProcess.getName(),result};
	}
}