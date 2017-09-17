package ScoreUtilities.scwrlIntegration;

import ModellingTool.MainMenu;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by zivben on 06/08/15.
 */
public class SCWRLrunner extends SwingWorker<String[], Void> {

	private final File scwrlExe;
	private final File input;
	private final File output;
	private String[] runLog;
	private boolean fakeRun = false;

	public SCWRLrunner(String pathToScwrlExe, File inputFile, File outputFile) throws IOException {

		scwrlExe = new File(pathToScwrlExe);
		this.output = outputFile;
		this.input = inputFile;
		long inputSize = inputFile.length();
		if (output.isFile() && output.length() > inputSize * 0.5) {
			fakeRun = true;
		} else {
			output.createNewFile();
			output.setWritable(true);
		}

		
		if (!scwrlExe.isFile()) {
			throw new FileNotFoundException(pathToScwrlExe);
		}

	}

	@Override
	protected String[] doInBackground() throws Exception {
		if (!fakeRun) {
			try {
				Process process = Runtime.getRuntime().exec(scwrlExe.getAbsolutePath() +
						" -i " + input.getAbsolutePath() +
						" -o " + output.getAbsolutePath() +
						" -h");

				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

				List<String> stdOutput = new ArrayList<>();
				stdOutput.add(output.getAbsolutePath());
				String line;
				while ((line = br.readLine()) != null) {
					if (!line.equals("")) {
						stdOutput.add(line);
					}
				}
				runLog = stdOutput.toArray(new String[stdOutput.size()]);

			} catch (IOException e) {
				e.printStackTrace();
			}
			try{
				input.delete();
			} catch (Exception e){
				System.err.println("Error deleting input PDB:");
				e.printStackTrace();
			}
			prepareNextStep();
			return runLog;
		} else {
			prepareNextStep();
			return new String[]{"Scwrl file already exists, not re-running"};
		}
	}

	private void prepareNextStep() {
		MainMenu.SCWRLfilesToMapCheck.add(output);
		MainMenu.scwrlProgressCounter.incrementAndGet();
		setProgress(100);
	}


	@Override
	protected void done() {
		try {
			if (get().length != 0) {
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
