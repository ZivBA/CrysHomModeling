package ModellingTool;

import ScoreUtilities.SFCheckIntegration.SFCheckThread;
import ScoreUtilities.ScoringGeneralHelpers;
import ScoreUtilities.scwrlIntegration.SCWRLrunner;
import UtilExceptions.MissingChainID;
import UtilExceptions.SfCheckResultError;
import alignment.RvalAlignerCluster;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zivben on 22/01/17.
 */
public class MainProgramThread extends SwingWorker<Void,String[]> {
	static Integer scwrlProgressCounter = 0;
	public static int sfckProgressCounter = 0;
	public static ConcurrentLinkedDeque<File> SCWRLfilesToSFcheck;
	
	CustomOutputStream customOut;
	private boolean doneWithCSVs = false;
	private boolean notStartedSFCheck = true;
	
	public static final List<String[]> SFCheckResultSet = new LinkedList<>();
	private static ExecutorService executorSF;
	private static ExecutorService executorSC;
	private final RunParameters params;
	private final File fastaFile;
	private final String fastaSequence;
	private final MainMenu menuObject;
	private CRYS_Score crysScore;
	private int totalNumberOfFilesToProcess;
	
	public MainProgramThread(RunParameters params, MainMenu mainMenu){
		this.params = params;
		this.fastaFile = params.getFastaFile();
		this.fastaSequence = params.getFastaSeq();
		this.menuObject = mainMenu;
		
		PrintStream printStream = new PrintStream(customOut);
		try {
			mainMenu.updateFields();
		} catch (Exception e) {
			System.out.println("Problem with current config.xml file, continue without defaults.");
		}
		if (mainMenu.debug.isSelected()) {
			System.setErr(printStream);
			System.setOut(printStream);
		} else {
			System.setOut(printStream);
		}
		SCWRLfilesToSFcheck = new ConcurrentLinkedDeque<>();
		
		Runtime.getRuntime().addShutdownHook(new ShutDownHook());
		
		
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		menuObject.setParams(this.params);
		System.out.println("");
		System.out.println("Starting run!\n");
		menuObject.scwrlProgress.setValue(0);
		menuObject.scwrlProgress.setStringPainted(true);
		menuObject.sfchkProgress.setValue(0);
		menuObject.sfchkProgress.setStringPainted(true);
		scwrlProgressCounter = 0;
		sfckProgressCounter = 0;
		crysScore = new CRYS_Score(params);
		int threads = params.getThreadLimit();
		executorSC = Executors.newFixedThreadPool(threads - 1);
		executorSF = Executors.newFixedThreadPool(1);
		ScoringGeneralHelpers.debug = menuObject.debug.isSelected();
		if (fastaFile != null && fastaSequence == null) {
			crysScore.setFastaFile(fastaFile);
		} else if (fastaSequence != null && fastaFile == null) {
			crysScore.setFastaSequence(fastaSequence);
		}
		
		
		totalNumberOfFilesToProcess = crysScore.getExpectedTotalNumberOfFiles();
		menuObject.scwrlProgress.setMaximum(totalNumberOfFilesToProcess);
		menuObject.sfchkProgress.setMaximum(totalNumberOfFilesToProcess);
		
		
		try {
			
			List<List<SCWRLrunner>> SCWRLruns = crysScore.iterateAndGenScwrl(this);
			if (SCWRLruns == null) {
				customOut.writeToFile();
				menuObject.scwrlProgress.setValue(menuObject.scwrlProgress.getMaximum());
				menuObject.scwrlProgress.setString("Done");
				menuObject.sfchkProgress.setValue(menuObject.sfchkProgress.getMaximum());
				menuObject.sfchkProgress.setString("Done");
				
				System.out.println("Finished processing existing files.\n");
				System.out.flush();
				RvalAlignerCluster results = crysScore.getRvalThread();
				runThreadingThread(results);
			} else {
				System.out.println("Starting SCWRL and SFCheck runs.");
				System.out.flush();
				for (List<SCWRLrunner> SCWRLTasks : SCWRLruns) {
					for (SCWRLrunner singleRun : SCWRLTasks) {
						singleRun.addPropertyChangeListener(e -> {
							if (e.getPropertyName().equals("progress")) {
								
								SwingUtilities.invokeLater(() -> {
									scwrlProgressCounter++;
									menuObject.scwrlProgress.setValue(scwrlProgressCounter);
									menuObject.scwrlProgress.setString(scwrlProgressCounter+" done");
									if (notStartedSFCheck){
										checkSCWRLfile();
									}
								});
								
							}
							
						});
						executorSC.submit(singleRun);
					}
				}
			}
		} catch (IOException | MissingChainID e) {
			System.err.println(e.getMessage());
			System.out.println("There was a problem processing one of the files.");
			System.out.flush();
		}
		return null;
	}
	
	@Override
	public void done(){
		
	}
	
	void runThreadingThread(RvalAlignerCluster finalThreadingRuns) {
		finalThreadingRuns.setExecutor(executorSC);
		finalThreadingRuns.setProgressBar(menuObject.threadProgress);
		finalThreadingRuns.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals("progress")) {
				menuObject.threadProgress.setValue(finalThreadingRuns.getProgress());
				menuObject.threadProgress.setString(finalThreadingRuns.getProgress()+"%");
			}
			
		});
		finalThreadingRuns.execute();
	}
	
	
	private void checkSCWRLfile() {
		
		// old SFCHECK thread
		try {
			while (SCWRLfilesToSFcheck.size() <5 && sfckProgressCounter < 5 ){
				// wait for queue to buffer a bit before polling. maybe prevents bad SFCheck runs...
			}
			
			SFCheckThread sfThread = new SFCheckThread(SCWRLfilesToSFcheck.pollLast(), params, this);
			sfThread.addPropertyChangeListener(e -> {
				if (e.getPropertyName().equals("progress")) {
					
					SwingUtilities.invokeLater(() -> {
						menuObject.sfchkProgress.setValue(sfckProgressCounter);
						menuObject.sfchkProgress.setString(sfckProgressCounter + " done");
						checkIfLastSFCHECK();
					});
					
				}
				
			});
			executorSF.submit(sfThread);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void checkIfLastSFCHECK() {
		
		if (sfckProgressCounter == totalNumberOfFilesToProcess) {
			
			if (!doneWithCSVs) {
				customOut.writeToFile();
				System.out.println("Processing Results\n");
				try {
					RvalAlignerCluster results = crysScore.processSFCheckResults(SFCheckResultSet);
					runThreadingThread(results);
					doneWithCSVs = true;
					
				} catch (SfCheckResultError e) {
					System.out.println("Problem with one of the SFCheck log files:");
					System.out.println(e.getMessage() + "\n");
					
				}
				System.out.println("Finished Processing Results!\n");
				System.out.println("Please wait for threading results to appear, then you may exit!");
			}
			
			
		}
		
	}
	
	private class ShutDownHook extends Thread {
		
		
		public void run() {
			
			customOut.writeToFile();
			customOut.closeFile();
			crysScore.deleteChains();
			
		}
	}
}
