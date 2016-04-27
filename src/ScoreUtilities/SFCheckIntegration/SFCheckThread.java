package ScoreUtilities.SFCheckIntegration;

import ModellingTool.MainMenu;
import ModellingTool.RunParameters;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ScoreUtilities.ScoringGeneralHelpers.PDB_EXTENSION;
import static ScoreUtilities.ScoringGeneralHelpers.makeSubFolderAt;

/**
 * Created by zivben on 13/04/16.
 */
public class SFCheckThread extends SwingWorker<String[],Void>  {
	private RunParameters params;
	private File SFCheckExe;
	private File outputFolder;
	private File output;
	private File trueOutput;
	private File protToProcess;
	private boolean fakeRun = false;
	private static File sfCheckScript = new File("/home/zivben/IdeaProjects/CrysHomModeller/paramSFcheck.btc");

	public SFCheckThread(File tempProt, RunParameters params) throws IOException {
		SFCheckExe = params.getSFChkexe();
		outputFolder = new File(params.getScwrlOutputFolder().getAbsolutePath() + params.getChainToProcess() + "sfcheck_LogFiles");
		outputFolder = makeSubFolderAt(params.getScwrlOutputFolder().getParentFile(), "sfcheck_LogFiles");
		protToProcess = tempProt;
		this.params = params;
		output = new File(outputFolder.getAbsolutePath() + File.separator + tempProt.getName().replaceFirst(PDB_EXTENSION + "+$",""));
		trueOutput = new File(output.getAbsolutePath() + "sfcheck.log");
		if (trueOutput.exists()){
			fakeRun = true;
		}

	}



	@Override
	protected String[] doInBackground() throws Exception {
//		if (!output.isFile()) {
//			try {
//				output.createNewFile();
//			} catch (IOException e) {
//				System.err.println("cannot create temporary log file for SFCHECK");
//			}
//			output.setWritable(true);
//		}
		String result = null;
		String re1="(\\s+)";	// White Space 1
		String re2="(Correlation)";	// Word 1
		String re3="( )";	// White Space 2
		String re4="(factor)";	// Word 2
		String re5="(\\s+)";	// White Space 3
		String re6="(:)";	// Any Single Character 1

		Pattern p = Pattern.compile(re1+re2+re3+re4+re5+re6,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher("");

		if (fakeRun){

			try (
					BufferedReader reader = Files.newBufferedReader(trueOutput.toPath(), Charset.defaultCharset());
					LineNumberReader lineReader = new LineNumberReader(reader)
			){
				String line = null;
				while ((line = lineReader.readLine()) != null) {
					m.reset(line); //reset the input
					if (m.find()) {
						result = line.substring(24);
						break;
					}
				}
				reader.close();
			}
			catch (IOException ex){
				ex.printStackTrace();
			}

			return new String[]{protToProcess.getName(),result};
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

		assert process != null;
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;


		try {
			while ((line = br.readLine()) != null) {
				m.reset(line);
				if (m.find()){
					result = line.substring(24);
				}
			}
		} catch (IOException e){
			System.err.println(e.getMessage());
		}

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(outputFolder.toPath())) {
			for (Path path : directoryStream) {
				if (!path.toString().endsWith(".log")) {
					path.toFile().delete();
				}
			}
		}
		setProgress(100);
		return new String[]{protToProcess.getName(),result};

	}

	@Override
	protected void done(){
		try{
			if (get().length!=0){
				MainMenu.SFCheckResultSet.add(get());
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}