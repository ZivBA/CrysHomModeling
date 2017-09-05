//package ScoreUtilities.EMCheckIntegration;
//
//import ModellingTool.MainMenu;
//import ModellingTool.RunParameters;
//import ModellingUtilities.molecularElements.SimpleProtein;
//
//import javax.swing.*;
//import java.io.*;
//import java.nio.file.Path;
//import java.util.concurrent.atomic.AtomicLong;
//
//import static ScoreUtilities.ScoringGeneralHelpers.PDB_EXTENSION;
//import static ScoreUtilities.ScoringGeneralHelpers.makeSubFolderAt;
//
///**
// * SFCheck worker thread instance.
// * create these when you want to run SFCheck against some PDB file, then pass the worker to an execution pool for processing.
// */
//public class EMCheckThread extends SwingWorker<String[],Void>  {
//	private static AtomicLong tempFolderCounter = new AtomicLong(0);
//	private static MRC_Map_New EM_map;
//	private RunParameters params;
//	private File outputFolder;
//	private File output;
//	private File trueOutput;
//	private File protToProcess;
//	private boolean fakeRun = false;
//	private String[] res;
//	private boolean cifNotSF = false;
//
//	/**
//	 * constructor - gets the PDB file to check and the run parameters object.
//	 */
//	public EMCheckThread(File tempProt, RunParameters params) throws IOException {
//		outputFolder = makeSubFolderAt(params.getScwrlOutputFolder().getParentFile(), "sfcheck_LogFiles");
//		outputFolder = makeSubFolderAt(outputFolder, String.valueOf(params.getChainToProcess()));
//
//		protToProcess = tempProt;
//		this.params = params;
//		output = new File(outputFolder.getAbsolutePath() + File.separator + tempProt.getName().replaceFirst(PDB_EXTENSION + "+$","_"));
//		trueOutput = new File(output.getAbsolutePath() + "sfcheck.log");
//		if (trueOutput.exists()){
//			fakeRun = true;
//			return;
//		}
//
//		SimpleProtein srcProt = new SimpleProtein(params.getPDBsrc(),params);
//		SimpleProtein newProt = new SimpleProtein(tempProt,params);
//		newProt.replaceTempValue(srcProt);
//		newProt.writePDB(tempProt);
//
//
//	}
//
//	public static void setMRCMap(Path MapPath) throws IOException {
//		EM_map = new MRC_Map_New(MapPath.toString());
//	}
//
//	@Override
//	/**
//	 * execution method - returns a String array of the results - first line is the protein name (which position was changed to which residue)
//	 * second line is the result - Correlation Coefficient.
//	 */
//	protected String[] doInBackground() throws Exception {
//
//
//	}
//
//	@Override
//	/**
//	 * when done, add the result to the SFCheck resultset collection and advance the progress counter.
//	 */
//	protected void done(){
//		try{
//
//			MainMenu.MapCorrResultSet.add(get());
//				setProgress(100);
//
//		} catch (Exception e){
//			e.printStackTrace();
//		}
//	}
//}