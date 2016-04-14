package ScoreUtilities.scwrlIntegration;

/**
 * Created by zivben on 09/04/16.
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static ScoreUtilities.ScoringGeneralHelpers.*;

/**
 * Created by zivben on 06/08/15.
 */
public class SCWRLactions {
	private static boolean debugFlag;
	/**
	 * iterate all files in folder and scwrl them.
	 * @param tempFolder
	 * @param debug
	 * @param executor
	 */
	public static void genSCWRLforFolder(File tempFolder, boolean debug, ExecutorService executor) throws IOException {
		SCWRLactions.debugFlag = debug;
		if (debug) {
			System.out.println("******************************************************************");
			System.out.println("Processing SCWRL input folder: \n" + tempFolder.getAbsolutePath());
			System.out.println("******************************************************************");
		}
		List<File> fileNames = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		float tempTime;
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempFolder.toPath())) {
			for (Path path : directoryStream) {
				if (!path.getFileName().toString().endsWith("_SCWRLed.pdb")) {
					fileNames.add(path.toFile());
				}
			}
		} catch (IOException ex) {
			throw ex;
		}

		int numOfFiles = fileNames.size();
		int blockSize = (int) (numOfFiles / (Math.ceil(Math.log10(numOfFiles))));
		int filesScwrled = 0;


		for (File fileName : fileNames) {
			File SCWRLFile = new File(fileName.getAbsolutePath().replace(".pdb", "_SCWRLed.pdb"));
			if (!SCWRLFile.exists()) {
				SCWRLrunner oneRun = new SCWRLrunner(SCWRL_PATH,fileName,SCWRLFile);
				executor.execute(oneRun);
				if (debugFlag){
					System.out.println(Arrays.toString(oneRun.runLog));
				}

			}
			fileName.delete();
			filesScwrled++;
			if (debugFlag) {
				if (filesScwrled % blockSize == 0) {
					tempTime = System.currentTimeMillis();
					float elapsed = (tempTime - startTime) / 1000f;
					System.out.println("Processed " + filesScwrled + " files out of " + numOfFiles + "\nthis batch took: "
							+ String.valueOf(elapsed));
					System.out.println("Should probably take about " + ((numOfFiles - filesScwrled) / blockSize *
							elapsed) +
							" Seconds");
				}
			}
		}
		if (debugFlag) {
			long stopTime = System.currentTimeMillis();
			float elapsedTime = (stopTime - startTime) / 1000f;
			System.out.println("******************************************************************");
			System.out.println("Generated: " + fileNames.size() + " Files in: " + elapsedTime + " seconds");
			System.out.println("******************************************************************");
			System.out.println("SCWRL execution terminated!");
		}


	}


}
