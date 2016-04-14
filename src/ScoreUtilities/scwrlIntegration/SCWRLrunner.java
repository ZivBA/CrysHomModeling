package ScoreUtilities.scwrlIntegration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zivben on 06/08/15.
 */
public class SCWRLrunner implements Runnable{

	File scwrlExe;
	File input;
	File output;
	String[] runLog;

	public SCWRLrunner(String pathToScwrlExe, File inputFile, File outputFile) throws IOException {

		scwrlExe = new File(pathToScwrlExe);
		this.output = outputFile;
		this.input = inputFile;
		if (!output.isFile()) {
			output.createNewFile();
			output.setWritable(true);
		}

		if (!scwrlExe.isFile()) {
			throw new FileNotFoundException(pathToScwrlExe);
		}

	}

	@Override
	public void run() {
		try {
			Process process = Runtime.getRuntime().exec(scwrlExe.getAbsolutePath() +
					" -i " + input.getAbsolutePath() +
					" -o " + output.getAbsolutePath() +
					" -h");

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

			List stdOutput = new ArrayList();
			String line;
			while ((line = br.readLine()) != null) {
				stdOutput.add(line);
			}
			runLog = (String[]) stdOutput.toArray(new String[stdOutput.size()]);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}