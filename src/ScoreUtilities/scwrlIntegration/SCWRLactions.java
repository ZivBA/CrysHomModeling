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
import java.util.LinkedList;
import java.util.List;

import static ScoreUtilities.ScoringGeneralHelpers.*;

/**
 * Created by zivben on 06/08/15.
 */
public class SCWRLactions {
	/**
	 * iterate all files in folder and scwrl them.
	 * @param tempFolder
	 */
	public static List<SCWRLrunner> genSCWRLforFolder(File tempFolder) throws IOException {
		List<SCWRLrunner> SCWRLtasks = new LinkedList<>();

		List<File> fileNames = new ArrayList<>();

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempFolder.toPath())) {
			for (Path path : directoryStream) {
				if (!path.getFileName().toString().endsWith("_SCWRLed.pdb")) {
					fileNames.add(path.toFile());
				}
			}
		} catch (IOException ex) {
			throw ex;
		}

		for (File fileName : fileNames) {
			File SCWRLFile = new File(fileName.getAbsolutePath().replace(".pdb", "_SCWRLed.pdb"));
			if (!SCWRLFile.exists()) {
				SCWRLrunner oneRun = new SCWRLrunner(SCWRL_PATH,fileName,SCWRLFile);
				SCWRLtasks.add(oneRun);
			}
		}

		return SCWRLtasks;

	}


}
