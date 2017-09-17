package ScoreUtilities;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Ziv_BA on 30/07/2015.
 */
public class ScoringGeneralHelpers {
	/**
	 * constatns from PDB format. (positions are "-1" since format starts at '1' and computers start at '0'
	 */
	public static final int ATOM_NUM_START = 6, ATOM_NUM_END = 10, ATOM_NAME_START = 12, ATOM_NAME_END = 15,
			RES_NAME_START = 17, RES_NAME_END = 19, CHAIN_ID = 21, RES_SEQ_START = 22, RES_SEQ_END = 25,
			RES_TEMP_START = 60, RES_TEMP_END = 65;
	public static final String FOOTER_TAGS = "MASTER|END", ALLOWED_ATOMS = "N|CA|C|O", PDB_EXTENSION = ".pdb", HETATM = "HETATM";
	//consts
	// keep ALA as first item, as this is being called explicitly by stripAllRes method.
	public static final String[] aAcids = {"ALA", "CYS", "ASP", "GLU", "PHE", "GLY", "HIS", "ILE",
			"LYS", "LEU", "MET", "ASN", "PRO", "GLN", "ARG", "SER", "THR", "VAL", "TRP", "TYR"};
	public static final char[] singleLetters = {'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q',
			'R', 'S', 'T', 'V', 'W', 'Y'};
	public static final double[] normalVector = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,};

	// vector designating which amino acids have negative signal. those are to be multiplied by "-1" to normalize the
	// zvalues for further processing. vector is 20 positions in order of AA same as seen above.
	//	public static final double[] vector2 = {1.0, -1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, -1.0, 1.0,
	//			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, -1.0, 1.0, 1.0, 1.0,};
	//
	//	public static final double[] vector3 = {1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0,
	//			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0,};
	//
	//	public static final double[] vector4 = {-1.0, 1.0, -1.0, -1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
	//			1.0, 1.0, 1.0, 1.0, 1.0, -1.0, 1.0, 1.0, 1.0, 1.0,};
	/**
	 * weight factors according to SVM, last index in every line is the Bias for that AA.
	 */
	public static final double[][] emNormalMatrix = {
	/* A */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* C */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* D */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* E */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* F */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* G */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* H */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* I */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* K */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* L */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* M */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* N */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* P */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* Q */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* R */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* S */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* T */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* V */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* W */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	/* Y */        {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0
	},
	};
	//path to SCWRL
	public static String SCWRL_PATH = "./utils/scwrlIntegration/Scwrl4";
	public static double[] latestVector = {-1.0, 1.0, -1.0, -1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, -1.0, 1.0, 1.0, 1.0, 1.0,};
	public static boolean debug = false;
	private final File source;
	
	/**
	 * Constructor - gets File obj, sets destination.
	 *
	 * @param toProcess
	 */
	public ScoringGeneralHelpers(File toProcess, boolean debug) {
		ScoringGeneralHelpers.debug = debug;
		source = toProcess;
		File dest = new File(source.getAbsolutePath().replaceFirst("[.][pdb]+$", "_stripped" + PDB_EXTENSION));
	}
	
	/*
	Helper routines for creating folders and subfolders.
	 */
	
	public static File makeFolder(File requestedFolder) throws IOException {

		if (requestedFolder.isDirectory()) {
			if (debug)
				System.out.println("Requested folder already exists at:\n" + requestedFolder.getAbsolutePath());
		} else {
			if (requestedFolder.mkdir()) {
				if (debug)
					System.out.println("Created requested folder \n" + requestedFolder.getAbsolutePath());
			} else {
				if (debug)
					System.out.println("Requested Folder not created");
				throw new IOException();
			}
		}

		return requestedFolder;
	}
	
	public static File makeSubFolderAt(File sourceFile, String targetSubFolder) throws IOException {
		File requestedFolder;
		if (sourceFile.isFile()) {
			requestedFolder = new File(sourceFile.getParent() + File.separator + targetSubFolder);
		} else if (sourceFile.isDirectory()) {
			requestedFolder = new File(sourceFile.getAbsolutePath() + File.separator + targetSubFolder);
		} else {
			throw new FileNotFoundException("source folder is not a proper path.");
		}
		
		requestedFolder = makeFolder(requestedFolder);
		
		return requestedFolder;
	}

	//*
	//*
	//*
	
	public static void multiplyMatrixByVector(double[][] allZvalueMatrix, double[] vector) {
		for (int i = 0; i < allZvalueMatrix.length; i++) {
			for (int j = 0; j < allZvalueMatrix[i].length; j++) {
				allZvalueMatrix[i][j] *= vector[i];
			}
		}
	}
	
	/**
	 * helper method to read CSV file to array .
	 * @param resultCSV the file to read from.
	 * @return the matrix read from the csv.
	 */
	public static double[][] csvToMatrix(File resultCSV) {
		double[][] resultMatrix;
		try {
			List<String> csvList = Files.readAllLines(resultCSV.toPath(), Charset.defaultCharset());
			resultMatrix = new double[csvList.get(0).split(",").length][csvList.size()];
			for (int i = 0; i < csvList.size(); i++) {
				String[] tempLine = csvList.get(i).split(",");
				for (int j = 0; j < csvList.get(0).split(",").length; j++) {
					resultMatrix[j][i] = Double.parseDouble(tempLine[j]);
				}
			}

		} catch (IOException e) {
			if (debug)
				System.out.println("Cannot read requested CSV");
			return null;
		}
		return resultMatrix;
	}

	public File getSource() {
		return source;
	}


}