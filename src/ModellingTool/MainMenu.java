package ModellingTool;

import ModellingUtilities.molecularElements.SimpleProtein;
import ScoreUtilities.SFCheckIntegration.SFCheckThread;
import ScoreUtilities.scwrlIntegration.SCWRLrunner;
import UtilExceptions.MissingChainID;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;


/**
 * Created by zivben on 06/03/16.
 * Main Menu GUI class - holds the user interface and execution instructions.
 */
public class MainMenu extends JPanel implements ActionListener {


	public static RunParameters params;
	static private final String newline = "\n";
	private JTextPane crystallographyModelingToolV0TextPane;
	private JButton SCWRLExecutableButton;
	private JButton SFCheckExecutableButton;
	private JTextPane pleasePointToSCWRLTextPane;
	private JTextPane pleaseChooseTheSourceTextPane;
	private JButton mapButton;
	private JButton PDBButton;
	private JTable CurrentData;
	private JPanel JPanelMain;
	private JTextPane selectWhichChainsAreTextPane;
	private JScrollPane logPane;
	private JTextArea log;
	private JSpinner threadLimit;
	private JTextPane limitNumberOfThreadsTextPane;
	private JButton executeButton;
	private JTable chainListTable;
	private JCheckBox saveDefaultsCheckBox;

	private JFileChooser fc;
	private JCheckBox debug;
	private JCheckBox keepHetAtm;
	private JScrollPane chainsListContainer;

	private JScrollPane filesContainer;
	private JProgressBar scwrlProgress;
	private JProgressBar sfchkProgress;

	protected static Integer scwrlProgressCounter = 0;
	protected static int sfckProgressCounter = 0;
	int filesScwrled = 0;
	int filesSfchecked = 0;
	int totalNumberOfFilesToProcess = 0;
	public static ConcurrentLinkedQueue<File> filesToSFcheck;


	static protected CRYS_Score crysScore;
	protected List<List<SCWRLrunner>> SCWRLruns;
	protected final List<List<Future<String[]>>> SCWRLexecutionList = new LinkedList<>();
	public static List<String[]> SFCheckResultSet = new LinkedList<>();
	//	protected static SwingWorkerExecutors executors;
	private ExecutorService executorSC;
	private static ExecutorService executorSF;

	public MainMenu() {
		super(new BorderLayout());

		fc = new JFileChooser();

		// add listeners to buttons
		SCWRLExecutableButton.addActionListener(this);
		SFCheckExecutableButton.addActionListener(this);
		mapButton.addActionListener(this);
		PDBButton.addActionListener(this);
		executeButton.addActionListener(this);
		debug.addActionListener(this);


		//redirect standard output to onscreen log.
		PrintStream printStream = new PrintStream(new CustomOutputStream(log));


		Runtime.getRuntime().addShutdownHook(new ShutDownHook());
		updateFields();
		//		if (debug.isSelected()) {
		//			System.setErr(printStream);
		//			System.setOut(printStream);
		//		} else {
		//			System.setOut(printStream);
		//		}
		filesToSFcheck = new ConcurrentLinkedQueue<>();

	}


	private static void createAndShowGUI() {

		JFrame frame = new JFrame("Crystallography Modeling Tool V0.√(π)");
		MainMenu menu = new MainMenu();
		frame.setContentPane(menu.JPanelMain);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);


	}

	private void createUIComponents() {
		// populate the files tables
		Object rowData[][] = {
				{"SCWRL exe", "..."},
				{"SFChck exe", "..."},
				{"Map source", "..."},
				{"PDB source", "..."},

		};
		Object columnNames[] = {"Object", "Path"};
		TableModel model = new DefaultTableModel(rowData, columnNames);
		CurrentData = new JTable(model);
		// update values from config.xml

		chainListTable = new JTable(new ChainListTableModel());

		//thread count spinner
		SpinnerNumberModel model1 = new SpinnerNumberModel(Runtime.getRuntime().availableProcessors() / 2, 0, Runtime.getRuntime()
				.availableProcessors
						(), 1);
		threadLimit = new JSpinner(model1);


	}


	@Override
	public void actionPerformed(ActionEvent e) {


		//Handle SCWRL EXE button action.
		if (e.getSource() == SCWRLExecutableButton) {
			int returnVal = fc.showOpenDialog(MainMenu.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				params.setSCWRLexe(file);
				System.out.println("SCWRL executable: " + file.getName() + " selected.");
			} else {
				System.out.println("Select SCWRL Exe cancelled by user.");
			}

			//handle DEBUG button action.
		} else if (e.getSource() == debug) {
			params.setDebug(!params.isDebug());


			//Handle PDB src button action.
		} else if (e.getSource() == PDBButton) {
			int returnVal = fc.showSaveDialog(MainMenu.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				System.out.println("Opening PDB file: " + file.getName() + ".");
				try {
					System.out.println(params.setPDBsrc(file));
				} catch (Exception e1) {
					System.err.println(e1.getMessage());
					System.out.println("PDB file bad / missing");
				}
				populateChainsList(params.getProtein());

			} else {
				System.out.println("PDB selection canceled by user.");
			}

			//Handle SFCheck exe button action.
		} else if (e.getSource() == SFCheckExecutableButton) {
			int returnVal = fc.showSaveDialog(MainMenu.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				params.setSFChkexe(file);
				System.out.println("SFcheck Exe: " + file.getName() + " selected.");

			} else {
				System.out.println("Select SFcheck Exe cancelled by user.");
			}

			// handle Map file section button
		} else if (e.getSource() == mapButton) {
			int returnVal = fc.showSaveDialog(MainMenu.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				params.setMAPsrc(file);
				System.out.println("Opening MAP file: " + file.getName() + ".");

			} else {
				System.out.println("PDB selection canceled by user.");
			}


		} else if (e.getSource() == executeButton) {
			startProgramMainThread();

		}
		updateFields();

	}

	public void startProgramMainThread() {

		params.setHetAtmProcess(keepHetAtm.isSelected());
		params.setDebug(debug.isSelected());
		params.setThreadLimit((Integer) threadLimit.getValue());
		params.setSaveDefaults(saveDefaultsCheckBox.isSelected());
		scwrlProgress.setValue(0);
		sfchkProgress.setValue(0);
		scwrlProgressCounter = 0;
		sfckProgressCounter = 0;

		if (saveDefaultsCheckBox.isSelected()) {
			params.setProperty("Save Defaults", String.valueOf(true));
			File configFile = new File("config.xml");
			OutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(configFile);
				params.storeToXML(outputStream, "Modeling Tool default settings");
				outputStream.close();
				System.out.println("Saved values to \"config.xml\" param file.");
			} catch (IOException e1) {
				e1.printStackTrace();
			}


		} else {
			File configFile = new File("config.xml");
			configFile.delete();
		}

		TableModel chainlist = chainListTable.getModel();
		LinkedList<Character> chainsToStrip = new LinkedList<>();
		LinkedList<Character> homologues = new LinkedList<>();
		Character chainsToProcess = null;
		for (int i = 0; i < chainlist.getRowCount(); i++) {
			if (chainlist.getValueAt(i, 2).equals(true)) {
				chainsToProcess = (Character) chainlist.getValueAt(i, 0);

			}
			if (chainlist.getValueAt(i, 3).equals(true)) {
				chainsToStrip.add((Character) chainlist.getValueAt(i, 0));
			}
			if (chainlist.getValueAt(i, 4).equals(true)) {
				homologues.add((Character) chainlist.getValueAt(i, 0));
			}
		}
		params.setChainsToStrip(chainsToStrip.toArray(new Character[chainsToStrip.size()]));
		params.setSymmetricHomologues(homologues.toArray(new Character[homologues.size()]));
		params.setChainToProcess(chainsToProcess);
		params.setDebug(debug.isSelected());


		int threads = params.getThreadLimit();
		executorSC = Executors.newFixedThreadPool(threads - 1);
		executorSF = Executors.newFixedThreadPool(1);


		//		ExecutorService executor = Executors.newFixedThreadPool(threads-1);
		//		ExecutorService executorSF = Executors.newFixedThreadPool(1);


		crysScore = new CRYS_Score(params);
		totalNumberOfFilesToProcess = crysScore.getExpectedTotalNumberOfFiles();
		scwrlProgress.setMaximum(totalNumberOfFilesToProcess);
		sfchkProgress.setMaximum(totalNumberOfFilesToProcess);


		try {

			SCWRLruns = crysScore.iterateAndGenScwrl();
			for (List<SCWRLrunner> SCWRLTasks : SCWRLruns) {
				for (SCWRLrunner singleRun : SCWRLTasks) {
					singleRun.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent e) {
							if (e.getPropertyName().equals("progress")) {
								scwrlProgressCounter++;
								if (scwrlProgressCounter%100 == 0){
									System.out.println("Processed another 100 SCWRLS");
								}
								scwrlProgress.setValue(scwrlProgressCounter);
								checkSCWRLfile();
							}

						}
					});
					executorSC.submit(singleRun);
				}
			}


			//			SFCheckRuns = crysScore.processSCWRLfolder();
			//			System.out.println("Running SFCheck on files");
			//
			//			for (SFCheckThread sfThread : SFCheckRuns){
			//
			//				Future<File> sfExec = executor.submit(sfThread);
			//				SFCheckResultSet.add(sfExec.get());
			//			}


			//						executors.shutdown();
		} catch (IOException | MissingChainID e) {
			System.err.println(e.getMessage());
			System.out.println("There was a problem processing one of the files.");
		}


	}


	private void printStatus(List<List<Future<String[]>>> SCWRLexecutions) throws InterruptedException, TimeoutException, ExecutionException {
		long startTime = System.currentTimeMillis();
		float tempTime = 0;

		System.out.println("******************************************************************");
		System.out.println("            Processing SCWRL input folder \n");
		System.out.println("******************************************************************");


		int numOfFiles = 0;
		for (List<Future<String[]>> list : SCWRLexecutions) {
			numOfFiles += list.size();
		}

		int blockSize = (int) (numOfFiles / (Math.ceil(Math.log10(numOfFiles))));
		int filesScwrled = 0;

		while (filesScwrled != numOfFiles) {
			for (List<Future<String[]>> execution : SCWRLexecutions)
				for (Future<String[]> SCWRLtask : execution) {
					if (SCWRLtask.isDone() && !SCWRLtask.isCancelled()) {
						filesScwrled++;
						SCWRLtask.cancel(true);
						System.err.println(Arrays.toString(SCWRLtask.get()));
					}
					if (filesScwrled % blockSize == 0) {
						tempTime = System.currentTimeMillis();
						float elapsed = (tempTime - startTime) / 1000f;
						System.out.println("Processed " + filesScwrled + " files out of " + numOfFiles + "\nthis batch took: " +
								String.valueOf(elapsed));
						System.out.println("Should probably take about " + ((numOfFiles - filesScwrled) / blockSize * elapsed) + " Seconds");
					}
				}

			if ((tempTime - startTime) / 1000f > TimeUnit.MINUTES.toSeconds(120)) {
				System.err.println("SCWRL execution is taking way too long for some reason");
				throw new TimeoutException("SCWRL execution is taking way too long for some reason");
			}
		}


		long stopTime = System.currentTimeMillis();
		float elapsedTime = (stopTime - startTime) / 1000f;
		System.out.println("******************************************************************");
		System.out.println("Generated: " + numOfFiles + " Files in: " + elapsedTime + " seconds");
		System.out.println("******************************************************************");
		System.out.println("SCWRL execution terminated!");

	}

	private void updateFields() {
		DefaultTableModel model1 = (DefaultTableModel) (CurrentData.getModel());

		model1.setValueAt(params.getProperty(RunParameters.SCWRLEXE), 0, 1);
		model1.setValueAt(params.getProperty(RunParameters.SFCHECK_PATH), 1, 1);
		model1.setValueAt(params.getProperty(RunParameters.MAPSRC), 2, 1);
		model1.setValueAt(params.getProperty(RunParameters.PDBSRC), 3, 1);

		try {
			keepHetAtm.getModel().setSelected(Boolean.parseBoolean(params.getProperty(RunParameters.HETATM)));
		} catch (Exception e) {
			System.out.println("There was an issue with the HetAtm default value. no worries.");
		}
		try {
			saveDefaultsCheckBox.getModel().setSelected(Boolean.parseBoolean(params.getProperty(RunParameters.DEFAULT)));
		} catch (Exception e) {
			System.out.println("There was an issue with the saveDefault values. no worries.");
		}

		try {
			debug.setSelected(Boolean.parseBoolean(params.getProperty(RunParameters.DEBUG)));
		} catch (Exception e) {
			System.out.println("There was an issue with the Debug Flag values. no worries.");
		}


	}

	private void populateChainsList(SimpleProtein protein) {
		ChainListTableModel model1 = (ChainListTableModel) chainListTable.getModel();
		model1.setRowCount(0);
		for (SimpleProtein.ProtChain chain : protein) {
			Vector<Object> row = new Vector<>();
			row.add(chain.getChainID());
			row.add(chain.getLength());
			row.add(false); // process
			row.add(true);  // strip
			row.add(false); // homologue
			model1.addRow(row);
		}
	}

	//	public void propertyChange(PropertyChangeEvent evt) {
	//		if ("progress" == evt.getPropertyName()) {
	//			int progress = (Integer) evt.getNewValue();
	//			sfchkProgress.setValue(progress);
	//		}
	//		if ("scwrlProgressCounter" == evt.getPropertyName()) {
	//			int progress = (Integer) evt.getNewValue();
	//			scwrlProgress.setValue(progress);
	//		}
	//	}


	public static void main(String[] args) throws FileNotFoundException {
		// get number of threads
		int cores = Runtime.getRuntime().availableProcessors();
		params = new RunParameters();
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
			}
		});


	}

	public void checkSCWRLfile() {
		try {
			SFCheckThread sfThread = new SFCheckThread(filesToSFcheck.poll(), params);
			sfThread.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					if (e.getPropertyName().equals("progress")) {
						sfckProgressCounter++;
						if (sfckProgressCounter%100 == 0){
							System.out.println("Processed another 100 SFCHECKS");
						}
						sfchkProgress.setValue(scwrlProgressCounter);
						checkIfLastSFCHECK();

					}

				}
			});
			executorSF.submit(sfThread);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkIfLastSFCHECK() {
		if (sfckProgressCounter == scwrlProgressCounter && sfckProgressCounter != 1) {
			crysScore.processSFCheckResults(SFCheckResultSet);
			log.append("Finished Processing Results, You may exit!");

		}

	}


	private class ShutDownHook extends Thread {
		public void run() {
			File logFile = new File(System.getProperty("user.dir") + "/CrysModeling.log");
			try (BufferedWriter fileOut = new BufferedWriter(new FileWriter(logFile))) {
				log.write(fileOut);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			crysScore.deleteChains();
		}
	}

	//	class processingTask extends SwingWorker<List<String[]>, Void> {
	//
	//		List<String> filesToSf = new ArrayList<>();
	//		public processingTask() {
	//			super();
	//			setProgress(0);
	//		}
	//
	//		@Override
	//		protected List<String[]> doInBackground() throws Exception {
	//
	//			if (SCWRLexecutionList.size() == 0) {
	//				filesToSf = crysScore.getScwrledFiles();
	//			} else {
	//				for (List<Future<String[]>> list : SCWRLexecutionList) {
	//					filesScwrled += list.size();
	//				}
	//			}
	//
	//			int blockSize = (int) (filesScwrled / (Math.ceil(Math.log10(filesScwrled))));
	//			int origNumFiles = filesScwrled;
	//			while (filesScwrled < origNumFiles) {
	//				for (List<Future<String[]>> list : SCWRLexecutionList) {
	//					for (Future<String[]> SCWRLthread : list) {
	//						if (SCWRLthread.isDone() && !SCWRLthread.isCancelled()) {
	//							filesScwrled++;
	//							SCWRLthread.cancel(true);
	//							try {
	//								filesToSf.add(SCWRLthread.get()[0]);
	//							} catch (InterruptedException e) {
	//								e.printStackTrace();
	//							} catch (ExecutionException e) {
	//								e.printStackTrace();
	//							}
	//							if (filesScwrled % blockSize == 0) {
	//								scwrlProgressCounter = (int) (100 - ((float) filesScwrled / origNumFiles * 100f));
	//								setProgress(Math.min(scwrlProgressCounter, 100));
	//
	//							}
	//
	//						}
	//					}
	//				}
	//				for (String fileToSf : filesToSf) {
	//					try {
	//						SFCheckResultSet.add(executors.submitSF(new SFCheckThread(new File(fileToSf), params)));
	//						filesSfchecked++;
	//					} catch (ExecutionException e) {
	//						e.printStackTrace();
	//					} catch (InterruptedException e) {
	//						e.printStackTrace();
	//					} catch (IOException e) {
	//						e.printStackTrace();
	//					}
	//					filesToSf.remove(fileToSf);
	//
	//				}
	//
	//
	//			}
	//			while (filesSfchecked != origNumFiles) {
	//				if (filesSfchecked % blockSize == 0) {
	//					sfckProgressCounter = (int) (100 - ((float) filesSfchecked / origNumFiles * 100));
	//					setProgress(Math.min(sfckProgressCounter, 100));
	//
	//				}
	//			}
	//			return SFCheckResultSet;
	//		}
	//
	//		@Override
	//		protected void done() {
	//			scwrlProgress.setValue(100);
	//			sfchkProgress.setValue(100);
	//			try {
	//				crysScore.processSFCheckResults(get());
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//
	//
	//		}
	//	}
}
