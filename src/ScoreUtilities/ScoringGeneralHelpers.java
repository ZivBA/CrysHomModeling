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
	//path to SCWRL
	public static String SCWRL_PATH = "./utils/scwrlIntegration/Scwrl4";
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


	public static double[] latestVector = {-1.0, 1.0, -1.0, -1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, -1.0, 1.0, 1.0, 1.0, 1.0,};


	public static final double[] normalVector = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,};
	
	/**
	 * weight factors according to SVM, last index in every line is the Bias for that AA.
	 */
	public static final double[][] crysSVMmatrix = {
	/* A */	    {0.0000, 0.0339, 0.1271, -0.2553, -0.1344, 0.4699, -0.1735, -0.1619, 0.0296, -0.2215,
			0.0594, -0.3630, -0.1499, -0.0459, 0.0371, -0.2111, -0.3423, -0.4768, 0.0704, 0.1308, -0.9782
	},
	/* C */		{0.0000, 0.2610, -0.0034, -0.0632, 0.0101, -0.4098, 0.1842, 0.3067, -0.1896, 0.1381,
			0.2236, 0.0860, -0.0543, 0.2389, -0.2565, 0.3582, 0.1729, -0.0465, 0.2471, -0.4149, -0.3731
	},
	/* D */		{0.0000, -0.0179, 0.4069, 0.1621, -0.1659, 0.4429, -0.2269, -0.0009, -0.0813, -0.2792,
			-0.0008, 0.4214, -0.0615, 0.0588, 0.2254, 0.1031, 0.2502, 0.0262, 0.3648, 0.0738, -0.1826
	},
	/* E */		{0.0000, -0.1721, -0.0611, 0.5636, -0.0278, 0.3825, -0.3497, 0.2668, 0.1646, -0.0756,
			0.2395, -0.0988, -0.1297, 0.1118, 0.1947, 0.0527, -0.0923, -0.1225, 0.3001, -0.1582, -0.0392
	},
	/* F */		{0.0000, 0.1492, -0.1194, -0.0175, 0.8039, -0.0608, 0.3322, 0.0052, 0.0155, 0.0544,
			0.0020, 0.1449, 0.0399, -0.0335, -0.1139, -0.1209, -0.1498, 0.1901, 0.0268, -0.2984, -0.6241
	},
	/* G */		{0.0000, -0.0724, 0.0195, 0.1531, 0.0200, 0.9141, -0.0299, 0.0402, 0.0041, -0.0084,
			0.0509, -0.0415, -0.2437, -0.0051, 0.0088, -0.0181, -0.0234, -0.2571, 0.0369, 0.0175, -0.6734
	},
	/* H */		{0.0000, 0.1313, 0.1996, -0.0577, 0.2540, 0.3297, 0.6851, 0.1488, -0.0030, -0.0603,
			-0.2066, -0.0146, -0.0720, 0.2765, -0.1117, -0.0039, -0.0052, 0.2561, 0.2567, 0.0516, -0.6857
	},
	/* I */		{0.0000, 0.1646, -0.0954, -0.1168, -0.3646, -0.5692, -0.2748, 0.1827, -0.1052, 0.1370,
			-0.2323, -0.0776, 0.0826, -0.2700, -0.1346, 0.1085, 0.3779, 0.1327, -0.1557, -0.0138, -0.6703
	},
	/* K */		{0.0000, -0.1357, 0.2895, 0.0764, -0.1277, 0.4219, -0.1554, 0.0994, 0.3558, -0.0988,
			0.3627, -0.2844, -0.2476, -0.0125, 0.1689, -0.0384, -0.2186, -0.3066, 0.2161, -0.1865, 0.0738
	},
	/* L */		{0.0000, 0.3670, -0.0279, -0.2303, -0.1492, -0.1642, 0.1608, -0.0365, -0.1906, 0.4582,
			-0.0320, -0.0142, -0.0462, -0.1953, -0.3023, -0.1007, 0.0641, 0.4687, -0.3264, -0.1243, -0.3583
	},
	/* M */		{0.0000, -0.2813, 0.1651, 0.1897, 0.1293, -0.0807, -0.3098, 0.1783, 0.1540, 0.3345,
			0.3410, -0.0002, -0.0055, 0.3797, -0.1540, -0.0226, -0.3348, 0.3098, -0.0180, -0.2789, -0.4447
	},
	/* N */		{0.0000, 0.0156, 0.2014, -0.1101, -0.0481, 0.2704, -0.0762, 0.1499, -0.2807, 0.0416,
			0.0337, 0.6672, -0.0108, 0.2069, 0.2800, -0.1206, 0.3710, 0.0810, 0.1215, 0.1475, -0.1141
	},
	/* P */		{0.0000, -0.2200, 0.1668, -0.0308, 0.0114, 0.1709, 0.1329, -0.3555, 0.1544, -0.2711,
			-0.1011, -0.2079, 0.7340, 0.0056, 0.0457, 0.0600, -0.0335, -0.1701, 0.1245, -0.0931, -0.4383
	},
	/* Q */		{0.0000, -0.1896, -0.1741, 0.4371, -0.0988, 0.2418, -0.2825, 0.3778, -0.2222, 0.2407,
			0.2934, 0.1448, 0.0516, 0.2622, 0.1263, -0.1871, -0.2120, 0.2371, 0.1139, 0.0328, -0.1610
	},
	/* R */		{0.0000, -0.1028, -0.0822, 0.3921, 0.1107, 0.0833, -0.4597, 0.1515, 0.2602, -0.1471,
			0.1890, -0.0455, -0.1663, -0.0396, 0.5023, -0.1844, 0.0019, -0.2966, 0.1562, 0.1429, -0.1357
	},
	/* S */		{0.0000, -0.3162, 0.1641, -0.0024, -0.1565, 0.1045, -0.1228, 0.1183, 0.1679, -0.3815,
			0.0490, -0.2916, 0.1724, 0.0519, 0.2056, 0.3749, 0.0829, -0.5394, 0.1176, 0.1433, 0.0440
	},
	/* T */		{0.0000, -0.1224, -0.0711, 0.0728, -0.1817, -0.2423, -0.2187, 0.1339, -0.0480, -0.3489,
			-0.2452, -0.1341, 0.2826, -0.2219, 0.1821, 0.2405, 0.6190, 0.0802, 0.0682, -0.0251, -0.3579
	},
	/* V */		{0.0000, 0.1609, 0.0973, -0.0858, -0.1194, -0.6159, -0.2011, -0.3569, -0.0443, -0.2583,
			-0.2033, -0.4090, -0.0982, -0.0498, -0.0044, 0.1311, 0.1530, 0.1877, -0.1724, -0.0298, -0.4780
	},
	/* W */		{0.0000, -0.2704, -0.0622, 0.2201, 0.3870, -0.1115, 0.1444, 0.1866, 0.0405, -0.0801,
			0.0242, 0.1737, -0.0423, 0.0387, 0.0109, -0.1805, -0.0815, -0.0123, 0.7573, -0.0412, -0.9736
	},
	/* Y */		{0.0000, -0.0183, 0.0539, -0.1110, 0.0889, -0.0092, 0.4079, -0.1970, 0.0746, -0.1290,
			-0.0341, -0.1217, -0.0385, 0.0220, 0.0520, -0.0357, -0.0664, 0.3119, 0.0487, 0.7894, -0.8156
	},
	};

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
	
	public File getSource() {
		return source;
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


}