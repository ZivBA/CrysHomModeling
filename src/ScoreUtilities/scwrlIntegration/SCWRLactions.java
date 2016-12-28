package ScoreUtilities.scwrlIntegration;


import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static ScoreUtilities.ScoringGeneralHelpers.SCWRL_PATH;

/**
 * Probably could be incorporated into another class, a helper class currently with one method.
 */
public class SCWRLactions {
	/**
	 * iterate all files in folder and create a SCWRLrunner instance for each file..
	 * @param tempFolder the folder to process
	 * @return a list of SCWRLrunner instances for execution later
	 * @throws IOException if some file cannot be read.
	 */
	public static List<SCWRLrunner> genSCWRLforFolder(File tempFolder) throws IOException {
		List<SCWRLrunner> SCWRLtasks = new LinkedList<>();

//		List<File> fileNames = new ArrayList<>();
		System.out.println("Creating SCWRL tasks for processing");
		System.out.flush();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempFolder.toPath())) {
			for (Path path : directoryStream) {
				File SCWRLFile;
				if (!path.getFileName().toString().endsWith("_SCWRLed.pdb")) {
					 SCWRLFile = new File(path.toFile().getAbsolutePath().replace(".pdb", "_SCWRLed.pdb"));
				}else {
					 SCWRLFile = path.toFile();
				}
				SCWRLrunner oneRun = new SCWRLrunner(SCWRL_PATH, path.toFile(), SCWRLFile);
				SCWRLtasks.add(oneRun);
				
//				if (!path.getFileName().toString().endsWith("_SCWRLed.pdb")) {
//					fileNames.add(path.toFile());
//				}
			}
		} catch (IOException ex) {
			throw ex;
		}

//		for (File fileName : fileNames) {
//			File SCWRLFile = new File(fileName.getAbsolutePath().replace(".pdb", "_SCWRLed.pdb"));
//
//			SCWRLrunner oneRun = new SCWRLrunner(SCWRL_PATH, fileName, SCWRLFile);
//			SCWRLtasks.add(oneRun);
//
//		}
		System.out.println("Done creating SCWRL Tasks");
		System.out.flush();
		return SCWRLtasks;

	}


}
