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
	/* A */	    {0.0000, 0.0527, 0.1114, -0.2430, -0.1421, 0.5098, -0.1455, -0.1554, 0.0150, -0.1954,
					0.0127, -0.3766, -0.1646, -0.0523, 0.0370, -0.2226, -0.2443, -0.5130, 0.0876, 0.0930, -0.9717},
	/* C */		{0.0000, 0.2390, 0.0742, 0.0271, 0.0370, -0.4777, 0.2232, 0.2759, -0.1789, 0.0998,
					0.2365, 0.0583, -0.0428, 0.2361, -0.3012, 0.3811, 0.1694, -0.0814, 0.0978, -0.3760, -0.4309},
	/* D */		{0.0000, -0.0196, 0.3969, 0.1521, -0.1716, 0.4639, -0.2207, 0.0115, -0.0771, -0.2550,
					-0.0320, 0.4318, -0.0627, 0.0811, 0.2012, 0.0921, 0.2515, 0.0250, 0.3733, 0.0655, -0.1815},
	/* E */		{0.0000, -0.2040, -0.0434, 0.5848, -0.0261, 0.3857, -0.3373, 0.2583, 0.1565, -0.0777,
					0.2384, -0.1049, -0.1038, 0.1145, 0.1802, 0.0456, -0.0857, -0.1205, 0.2921, -0.1435, -0.0449},
	/* F */		{0.0000, 0.1238, -0.1316, -0.0041, 0.7921, -0.0569, 0.3430, 0.0114, 0.0200, 0.0790,
					-0.0272, 0.1585, 0.0348, -0.0350, -0.1293, -0.0816, -0.1707, 0.1922, 0.0469, -0.3039, -0.6256},
	/* G */		{0.0000, -0.0942, 0.0354, 0.1486, 0.0373, 0.9239, -0.0443, 0.0033, 0.0034, 0.0027,
					0.0584, -0.0652, -0.2403, -0.0091, 0.0041, 0.0087, -0.0242, -0.2053, 0.0506, -0.0027, -0.6669},
	/* H */		{0.0000, 0.1216, 0.1837, -0.0464, 0.2522, 0.3042, 0.6625, 0.1086, -0.0051, -0.1122,
					-0.1727, -0.0305, -0.0390, 0.2711, -0.1254, -0.0251, -0.0953, 0.3832, 0.2150, 0.0745, -0.6527},
	/* I */		{0.0000, 0.1594, -0.0861, -0.1265, -0.3837, -0.5514, -0.2697, 0.1132, -0.1174, 0.0993,
					-0.2168, -0.0844, 0.0796, -0.2691, -0.1063, 0.1143, 0.3499, 0.2656, -0.2005, 0.0155, -0.6697},
	/* K */		{0.0000, -0.1283, 0.2909, 0.0923, -0.1256, 0.3902, -0.1654, 0.0841, 0.3737, -0.0929,
					0.3577, -0.2727, -0.2630, -0.0464, 0.1563, -0.0475, -0.2287, -0.3188, 0.2253, -0.1863, 0.0692},
	/* L */		{0.0000, 0.3631, -0.0532, -0.2038, -0.1517, -0.1948, 0.1360, -0.0298, -0.1927, 0.4718,
					-0.0551, 0.0187, -0.0557, -0.2212, -0.3185, -0.1000, 0.0773, 0.4492, -0.3101, -0.1139, -0.3645},
	/* M */		{0.0000, -0.2677, 0.1870, 0.2505, 0.1357, -0.1318, -0.2571, 0.1902, 0.1440, 0.2770,
					0.3336, -0.0186, -0.0286, 0.3747, -0.1899, -0.0656, -0.3433, 0.2939, -0.0083, -0.3125, -0.4410},
	/* N */		{0.0000, -0.0233, 0.2385, -0.0420, -0.0457, 0.2182, -0.0697, 0.0585, -0.2514, 0.0598,
					-0.0639, 0.7047, -0.0261, 0.2049, 0.2601, -0.0783, 0.3815, 0.0550, 0.1948, 0.1116, -0.1356},
	/* P */		{0.0000, -0.2323, 0.1896, -0.0361, -0.0435, 0.1706, 0.0971, -0.3628, 0.1595, -0.2394,
					-0.1122, -0.2052, 0.7406, 0.0053, 0.0543, 0.0560, -0.0687, -0.1312, 0.1455, -0.0409, -0.4344},
	/* Q */		{0.0000, -0.1842, -0.1318, 0.4482, -0.0523, 0.2407, -0.2889, 0.3778, -0.2402, 0.2213,
					0.2646, 0.1429, 0.0374, 0.3187, 0.1243, -0.1216, -0.2652, 0.2370, 0.0615, 0.0190, -0.1748},
	/* R */		{0.0000, -0.0756, -0.0621, 0.3935, 0.0810, 0.0877, -0.4908, 0.0729, 0.2715, -0.1857,
					0.2271, -0.0871, -0.1633, -0.0281, 0.4948, -0.1916, 0.0017, -0.1965, 0.1792, 0.1674, -0.1338},
	/* S */		{0.0000, -0.3110, 0.1330, 0.0252, -0.1538, 0.0848, -0.1165, 0.0822, 0.1506, -0.4225,
					0.0666, -0.2982, 0.1842, 0.0527, 0.2096, 0.4053, 0.0019, -0.5043, 0.1010, 0.1726, 0.0499},
	/* T */		{0.0000, -0.1206, -0.0812, 0.1168, -0.1730, -0.2612, -0.1877, 0.1896, -0.0947, -0.3312,
					-0.2274, -0.1423, 0.2858, -0.2487, 0.1751, 0.2468, 0.6020, 0.0400, 0.0635, -0.0455, -0.3510},
	/* V */		{0.0000, 0.0894, 0.1153, -0.1168, -0.1356, -0.5989, -0.2289, -0.3803, -0.0056, -0.2526,
					-0.1955, -0.3900, -0.1001, -0.0474, -0.0161, 0.1293, 0.1457, 0.2578, -0.1375, -0.0315, -0.4737},
	/* W */		{0.0000, -0.2609, -0.0490, 0.2095, 0.4140, -0.1220, 0.0919, 0.1662, 0.0118, -0.0273,
					0.0289, 0.1640, -0.0385, 0.0378, 0.0193, -0.1754, -0.1147, 0.0072, 0.7643, -0.0576, -0.9772},
	/* Y */		{0.0000, 0.0368, 0.0615, -0.1198, 0.0491, 0.0024, 0.4380, -0.1714, 0.0772, -0.1492, -0.0238,
					-0.1743, -0.0462, 0.0111, 0.0562, -0.0288, -0.0576, 0.2840, 0.0608, 0.7770, -0.8022},
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