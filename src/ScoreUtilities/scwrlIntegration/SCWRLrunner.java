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

	File scwrlExe;
	File input;
	File output;
	String[] runLog;
	boolean fakeRun = false;

	public SCWRLrunner(String pathToScwrlExe, File inputFile, File outputFile) throws IOException {

		scwrlExe = new File(pathToScwrlExe);
		this.output = outputFile;
		this.input = inputFile;
		if (!output.isFile()) {
			output.createNewFile();
			output.setWritable(true);
		} else {
			fakeRun = true;
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

			prepareNextStep();
			return runLog;
		} else {
			prepareNextStep();
			return new String[]{"Scwrl file already exists, not re-running"};
		}
	}

	private void prepareNextStep() {
		MainMenu.filesToSFcheck.add(output);
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
