package ModellingTool;

import ModellingUtilities.molecularElements.SimpleProtein;
import ScoreUtilities.EMCheckIntegration.EMCheckThread;
import ScoreUtilities.ScoringGeneralHelpers;
import ScoreUtilities.scwrlIntegration.SCWRLrunner;
import UtilExceptions.MissingChainID;
import alignment.RvalAlignerCluster;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by zivben on 06/03/16.
 * Main Menu GUI class - holds the user interface and execution instructions.
 */
public class MainMenu extends JPanel implements ActionListener {


	public static final List<Object[]> mapIntensityResults = new LinkedList<>();
	public static AtomicInteger scwrlProgressCounter = new AtomicInteger(0);
	public static ConcurrentLinkedDeque<File> SCWRLfilesToMapCheck;
	public static boolean doneThreading = false;
	static javax.swing.Timer sfCheckTimer;
	static javax.swing.Timer threadStartTimer;
	private static RunParameters params;
	private static AtomicInteger mapChkProgressCounter = new AtomicInteger(0);
	private static ThreadPoolExecutor executors;
	private static boolean silentRun = false;
	private final File configFile;
	private final JFileChooser fc;
	private final FASTAinput fastaDialog;
	private JTextArea EMModelingToolV0TextArea;
	private JButton SCWRLExecutableButton;
	//	private JButton SFCheckExecutableButton;
	private JButton mapButton;
	private JButton PDBButton;
	private JTable CurrentData;
	private JPanel JPanelMain;
	private JTextArea selectWhichChainsAreTextArea;
	private JScrollPane logPane;
	private JTextArea log;
	private final CustomOutputStream customOut = new CustomOutputStream(log);
	private JSpinner threadLimit;
	private JButton executeButton;
	private JTable chainListTable;
	private JCheckBox saveDefaultsCheckBox;
	private JCheckBox debug;
	private JCheckBox keepHetAtm;
	private JScrollPane chainsListContainer;
	private JScrollPane filesContainer;
	private JTextArea pleasePointToSCWRLTextArea;
	private JTextArea pleaseChooseTheSourceTextArea;
	private JTextArea limitNumberOfThreadsTextArea;
	private JTabbedPane tabbedPane1;
	private JButton FASTAButton;
	private JRadioButton fullFastaRadioButton;
	private JRadioButton inputSeqRadioButton;
	private JProgressBar threadProgress;
	private JProgressBar scwrlProgress;
	private JProgressBar mapCheck;
	private JButton SWISSPROTButton;
	private JButton exitButton;
	private int totalNumberOfFilesToProcess = 0;
	private EM_Score emScore;
	private String fastaSequence;
	private File fastaFile;
	private boolean doneWithCSVs = false;


	/**
	 * the main menu GUI form configuration and setup.
	 */
	private MainMenu() {
		super(new BorderLayout());

		// file chooser for dialogs
		fc = new JFileChooser();
		fc.setCurrentDirectory(Paths.get("").toAbsolutePath().toFile());
		JFileChooser fc2 = new JFileChooser();
		fc2.setCurrentDirectory(Paths.get("").toAbsolutePath().toFile());

		// text input dialog for FASTA string input.
		fastaDialog = new FASTAinput(fc2);
		fastaDialog.pack();

		// add listeners to buttons
		SCWRLExecutableButton.addActionListener(this);
//		SFCheckExecutableButton.addActionListener(this); // for CRYS project.
		mapButton.addActionListener(this);
		PDBButton.addActionListener(this);
		executeButton.addActionListener(this);
		debug.addActionListener(this);
		FASTAButton.addActionListener(this);
		SWISSPROTButton.addActionListener(this);

		// define or create config file:
		String curPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		configFile = new File(curPath.substring(0, curPath.lastIndexOf(File.separatorChar)) + "/config.xml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//redirect standard output to onscreen log.
		PrintStream printStream = new PrintStream(customOut);

		Runtime.getRuntime().addShutdownHook(new ShutDownHook());
		try {
			// populate GUI fields from XML file.
			updateFields();
		} catch (Exception e) {
			System.out.println("Problem with current config.xml file, continue without defaults.");
		}
		if (debug.isSelected()) {
			System.setErr(printStream);
		}
		System.setOut(printStream);

		// thread safe container for processed SCWRL files.
		SCWRLfilesToMapCheck = new ConcurrentLinkedDeque<>();

		// add listener to exit button.
		exitButton.addActionListener(e -> MainMenu.exitProgram(exitButton));
	}

	/**
	 * Main method.
	 *
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		/**
		 * If you get an argument file, run the program in "silent" mode. otherwise - open GUI.
		 */
		if (args.length > 0) {
			//try to parse the XML configuration file for silent run.
			params = new RunParameters(args[0]);
			if (args.length == 2) {
				silentRun = args[1].trim().toLowerCase().equals("silent");
			} else {
				silentRun = false;
			}
			params.setSilent(silentRun);
			//Schedule a job for the event dispatch thread:
			//creating and showing this application's GUI.
			SwingUtilities.invokeLater(() -> {
				//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				try {
					// try to run the "silent" GUI version - basically runs the program the same way as with GUI, but
					// hides it.
					createAndShowGUISilent();
				} catch (IOException e) {
					e.printStackTrace();
				}

			});

		} else {
			// create a new "empty" params object for interactive GUI execution.
			params = new RunParameters(null);

			//Schedule a job for the event dispatch thread:
			//creating and showing this application's GUI.
			SwingUtilities.invokeLater(() -> {
				//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();

			});
		}


	}

	/**
	 * Exit notification and main window destruction.
	 * @param exitButton need to get some entity from the main panel, so the exit button made sense.
	 */
	static void exitProgram(JButton exitButton) {
		System.err.println("Closing program");
		System.err.flush();
		JFrame j = new JFrame();
		// open exit notification.
		final JDialog dialog = new JDialog(j, "Shut-Down", true);
		JLabel label = new JLabel("<html><p align=center>"
				+ "Program is now closing.<br>"
				+ "So long and thanks for all the protein<br>");
		label.setHorizontalAlignment(JLabel.CENTER);
		Font font = label.getFont();
		label.setFont(label.getFont().deriveFont(Font.PLAIN,
				14.0f));
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(label, BorderLayout.CENTER);
		contentPane.setOpaque(true);
		dialog.setContentPane(contentPane);

		// wait for 10 seconds before stopping.
		Timer timer = new Timer(10000, e -> {
			dialog.setVisible(false);
			dialog.dispose();
		});
		timer.setRepeats(false);
		timer.start();
		dialog.setSize(new Dimension(300, 150));
		dialog.setLocationRelativeTo(j);
		dialog.setVisible(true); // if modal, application will pause here

		// climb up frame hierarchy until we get the JFrame parent and the dispose of it.
		Container frame = exitButton.getParent();
		do
			frame = frame.getParent();
		while (!(frame instanceof JFrame));
		frame.setVisible(false);
		((JFrame) frame).dispose();
		frame.dispatchEvent(new WindowEvent((Window) frame, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * setup the GUI form and start program.
	 */
	private static void createAndShowGUI() {

		JFrame frame = new JFrame("Crystallography Modeling Tool V0.1");
		MainMenu menu = new MainMenu();
		frame.setContentPane(menu.JPanelMain);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);


	}

	/**
	 * setup the GUI form but dont display. under assumption that XML config file was properly loaded - start the
	 * main program thread (like hitting "execute" button).
	 * @throws IOException
	 */
	private static void createAndShowGUISilent() throws IOException {

		JFrame frame = new JFrame("Crystallography Modeling Tool V0.√(π)");
		MainMenu menu = new MainMenu();
		frame.setContentPane(menu.JPanelMain);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(!silentRun);
//		frame.setVisible(true);
		menu.startProgramMainThread();


	}

	private void createUIComponents() {
		// populate the files tables
		Object rowData[][] = {
				{"SCWRL exe", "..."},
//				{"SFChck exe", "..."},
				{"Map source", "..."},
				{"PDB source", "..."},
				{"SWISSPROT source", "..."}
			
			
		};
		Object columnNames[] = {"Object", "Path"};
		TableModel model = new DefaultTableModel(rowData, columnNames);
		CurrentData = new JTable(model);
		
		chainListTable = new JTable(new ChainListTableModel());
		
		//thread count spinner
		SpinnerNumberModel model1 = new SpinnerNumberModel(Runtime.getRuntime().availableProcessors() / 2, 0, Runtime.getRuntime()
				.availableProcessors
						(), 1);
		threadLimit = new JSpinner(model1);
		logPane = new JScrollPane(log);
		UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
		UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
		
		
	}
	
	@Override
	/**
	 * button interactions
	 */
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

			// handle FASTA button
		} else if (e.getSource() == FASTAButton) {
			fastaDialog.setLocationRelativeTo(this.getRootPane());
			fastaDialog.setVisible(true);
			
			if (fastaDialog.fileChooserResult == JFileChooser.APPROVE_OPTION) {
				this.fastaFile = fastaDialog.fastaFile;
				this.fastaSequence = fastaDialog.fastaSequence;
			}
			if (!(fastaSequence == null))
				params.setFASTASeq(this.fastaSequence);
			if (!(fastaFile == null))
				params.setFASTAFile(this.fastaFile);
			
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
//		} else if (e.getSource() == SFCheckExecutableButton) {
//			int returnVal = fc.showSaveDialog(MainMenu.this);
//			if (returnVal == JFileChooser.APPROVE_OPTION) {
//				File file = fc.getSelectedFile();
//				params.setSFChkexe(file);
//				System.out.println("SFcheck Exe: " + file.getName() + " selected.");
//
//			} else {
//				System.out.println("Select SFcheck Exe cancelled by user.\n");
//			}
//
			// handle Map file section button
		} else if (e.getSource() == mapButton) {
			int returnVal = fc.showSaveDialog(MainMenu.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				params.setMAPsrc(file);
				System.out.println("Opening MAP file: " + file.getName() + ".\n");
				
			} else {
				System.out.println("PDB selection canceled by user.\n");
			}
			
			// handle SWISSPROT file section button
		} else if (e.getSource() == SWISSPROTButton) {
			int returnVal = fc.showSaveDialog(MainMenu.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				params.setSWISSProt(file);
				System.out.println("Opening MAP file: " + file.getName() + ".\n");
				
			} else {
				System.out.println("PDB selection canceled by user.\n");
			}
			
			
		} else if (e.getSource() == executeButton) {
			try {
				startProgramMainThread();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(-1);
			}
			
		}
		updateFields();
		
	}
	
	private void startProgramMainThread() throws IOException {
		
		ActionListener uiUpdateListener = e -> {
			
			scwrlProgress.setValue(scwrlProgressCounter.get());
			scwrlProgress.setString(scwrlProgressCounter + " done");
			mapCheck.setValue(mapChkProgressCounter.get());
			mapCheck.setString(mapChkProgressCounter + " done");


		};

		ActionListener mapCheckEventListener = e -> {
			if (!SCWRLfilesToMapCheck.isEmpty())
				checkSCWRLfile();
		};
		
		ActionListener threadStartEvent = e -> {
			if (!doneWithCSVs)
				checkIfLastMapCheck();
			if (doneThreading)
				exitProgram(exitButton);
		};
		
		
		Timer uiTimer = new Timer(250, uiUpdateListener);
		sfCheckTimer = new Timer(1000, mapCheckEventListener);
		sfCheckTimer.setInitialDelay(5000);
		threadStartTimer = new Timer(10000, threadStartEvent);
		threadStartTimer.setInitialDelay(5000);


//		SFCheckThread.setSfcheckProgram(params.getSFChkexe().toPath());
		customOut.setLogName(params.getProtein().getFileName(), params.getChainToProcess());
		params.setCustomOutputStream(customOut);

		System.out.println("");
		System.out.println("Starting run!\n");
		params.setHetAtmProcess(keepHetAtm.isSelected());
		params.setDebug(debug.isSelected());
		params.setThreadLimit((Integer) threadLimit.getValue());
		params.setSaveDefaults(saveDefaultsCheckBox.isSelected());
		params.setThreadingSelection(fullFastaRadioButton.isSelected());
		scwrlProgress.setValue(0);
		scwrlProgress.setStringPainted(true);
		mapCheck.setValue(0);
		mapCheck.setStringPainted(true);
		scwrlProgressCounter.set(0);
		mapChkProgressCounter.set(0);
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
		
		if (chainsToProcess == null) {
			JOptionPane.showMessageDialog(this, "Please choose one chain to process.");
		} else {
			params.setChainToProcess(chainsToProcess);
		}
		int homChainLength = 0;
		for (Character chain : homologues) {
			if (homChainLength == 0)
				homChainLength = params.getProtein().getChain(chain).getLength();
			else if (homChainLength != params.getProtein().getChain(chain).getLength()) {
				JOptionPane.showMessageDialog(this, "Homologous chains must have the same length.");
				return;
			}
		}
		
		if (saveDefaultsCheckBox.isSelected()) {
			params.setProperty("Save Defaults", String.valueOf(true));
			
			OutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(configFile);
				params.storeToXML(outputStream, "Modeling Tool default settings");
				outputStream.close();
				System.out.println("Saved values to " + configFile.getAbsolutePath() + " param file.\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			
		} else {
			configFile.delete();
		}
		
		
		params.setChainsToStrip(chainsToStrip.toArray(new Character[chainsToStrip.size()]));
		params.setSymmetricHomologues(homologues.toArray(new Character[homologues.size()]));
		params.setChainToProcess(chainsToProcess);
		
		
		int threads = params.getThreadLimit();
//		int ScwrlThreads = (threads / 3 * 2) == 0 ? 1 : threads / 3 * 2;
		//		int SFCheckThreads = (threads / 3) == 0 ? 1 : threads / 3;
		executors = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
		//		executorSF = Executors.newFixedThreadPool(SFCheckThreads);
		ScoringGeneralHelpers.debug = debug.isSelected();
		
		SwingWorker<Void, Void> mainThread = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {

				emScore = new EM_Score(params);
				emScore.exMax();
				
				if (fastaFile != null && fastaSequence == null) {
					emScore.setFastaFile(fastaFile);
				} else if (fastaSequence != null && fastaFile == null) {
					emScore.setFastaSequence(fastaSequence);
				}


				totalNumberOfFilesToProcess = emScore.getExpectedTotalNumberOfFiles();
				scwrlProgress.setMaximum(totalNumberOfFilesToProcess);
				mapCheck.setMaximum(totalNumberOfFilesToProcess);


				try {

					List<List<SCWRLrunner>> SCWRLruns = emScore.iterateAndGenScwrl();
					
					if (SCWRLruns == null) {
						customOut.writeToFile();
						scwrlProgress.setValue(scwrlProgress.getMaximum());
						scwrlProgress.setString("Done");
						mapCheck.setValue(mapCheck.getMaximum());
						mapCheck.setString("Done");
						
						System.out.println("Finished processing existing files.\n");
						System.out.flush();
						RvalAlignerCluster results = emScore.getRvalThread();
						runThreadingThread(results);
						threadStartTimer.start();
					} else {
						EMCheckThread.setMRCMap(params.getMAPsrc().toPath(), executeButton);
						System.out.println("Starting SCWRL and SFCheck runs.");
						System.out.flush();
						for (List<SCWRLrunner> SCWRLTasks : SCWRLruns) {
							for (SCWRLrunner singleRun : SCWRLTasks) {
								executors.submit(singleRun);
							}
						}
						sfCheckTimer.start();
						threadStartTimer.start();
						
					}
				} catch (IOException | MissingChainID e) {
					System.err.println(e.getMessage());
					System.out.println("There was a problem processing one of the files.");
					System.out.flush();
				}
				return null;
			}
			
			@Override
			public void done() {
				try {
					get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				
			}
		};
		
		mainThread.execute();
		
		uiTimer.start();
		
	}
	
	private void runThreadingThread(RvalAlignerCluster finalThreadingRuns) {
		finalThreadingRuns.setExecutor(executors);
		finalThreadingRuns.setProgressBar(threadProgress);
		finalThreadingRuns.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals("progress")) {
				threadProgress.setValue(finalThreadingRuns.getProgress());
				threadProgress.setString(finalThreadingRuns.getProgress() + "%");
			}
			
		});
		try {
			finalThreadingRuns.execute();
		} catch (Exception e) {
			System.out.println("Error: ");
			System.out.println(e.getMessage());
			exitProgram(exitButton);
		}
		emScore.deleteChains();
		System.out.println("Done!");
		
		
	}
	
	private void updateFields() {
		// get the table model for the file paths table.
		DefaultTableModel model1 = (DefaultTableModel) (CurrentData.getModel());
		
		model1.setValueAt(params.getProperty(RunParameters.SCWRLEXE), 0, 1);
//		model1.setValueAt(params.getProperty(RunParameters.SFCHECK_PATH), 1, 1);
		model1.setValueAt(params.getProperty(RunParameters.MAPSRC), 1, 1);
		model1.setValueAt(params.getProperty(RunParameters.PDBSRC), 2, 1);
		model1.setValueAt(params.getProperty(RunParameters.SWISSPROTpath), 3, 1);

		// try to fill GUI with details from XML file
		try {
			// try to fill the protein chains detail table.
			populateChainsList(params.getProtein());
		} catch (Exception e) {
			System.out.println("cant load protein from config file.");
		}
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
		try {
			fullFastaRadioButton.setSelected(Boolean.parseBoolean(params.getProperty(RunParameters.FULL_FASTA)));
		} catch (Exception e) {
			System.out.println("There was an issue parsing FASTA threading preference. no worries.");
		}
		try {
			TableModel chainlist = chainListTable.getModel();
			LinkedList<Character> chainsToStrip = new LinkedList<>(java.util.Arrays.asList(params.getChainsToStrip()));
			LinkedList<Character> homologues = new LinkedList<>(java.util.Arrays.asList(params.getSymmetricHomologues()));
			Character chainToProcess = params.getChainToProcess();
			for (int i = 0; i < chainlist.getRowCount(); i++) {
				if (chainlist.getValueAt(i, 0).equals(chainToProcess)) {
					chainlist.setValueAt(true, i, 2);
				}
				if (chainsToStrip.contains(chainlist.getValueAt(i, 0))) {
					chainlist.setValueAt(true, i, 3);
				}
				if (homologues.contains(chainlist.getValueAt(i, 0))) {
					chainlist.setValueAt(true, i, 4);
				}
			}
			
		} catch (Exception ignored) {
			
		}
		System.out.flush();

	}

	/**
	 * populate the chain detail table from input Protein object.
	 * @param protein the protein object according to which we fill the table.
	 */
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
		model1.fireTableDataChanged();
	}

	/**
	 * event executed at intervals once SCWRL execution has started.
	 * periodically checks the queue of available files, polls at most 100 entries and creates new "check" threads for
	 * each one.
	 */
	private void checkSCWRLfile() {
		
	
		try {
			
			for (int k = 0; k < 100; k++) {
				if (!SCWRLfilesToMapCheck.isEmpty()) {
					EMCheckThread emThread = new EMCheckThread(SCWRLfilesToMapCheck.pollLast(), params);
					emThread.addPropertyChangeListener(e -> {
						if (e.getPropertyName().equals("progress")) {
							
							SwingUtilities.invokeLater(() -> {
								mapChkProgressCounter.incrementAndGet();
								
							});

						}

					});
					executors.submit(emThread);

				} else {
					threadStartTimer.restart();
					return;
				}
			}
			threadStartTimer.restart();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * event executed at intervals to check if all files were processed, and if so start processing results.
	 */
	private void checkIfLastMapCheck() {

		if (debug.isSelected()) {
			System.err.println(executors.getActiveCount() + "active, completed:" + executors.getCompletedTaskCount());
		}
		if ((mapChkProgressCounter.get() == totalNumberOfFilesToProcess) && (mapChkProgressCounter.get() > 0)) {
				
				customOut.writeToFile();
				System.out.println("Processing Results\n");
			try {
				RvalAlignerCluster results = emScore.processMapCheckResults(mapIntensityResults);
					doneWithCSVs = true;
					runThreadingThread(results);

			} catch (MissingChainID e) {
				System.out.println("Problem with one of the MapCheck process:");
					System.out.println(e.getMessage() + "\n");
					executors.shutdownNow();
					exitProgram(exitButton);

			}
			System.out.println("Finished Processing Results!\n");
				System.out.println("Please wait for threading results to appear, then you may exit!");
				System.out.flush();
			
			
		}
			
		
	}
	
	private class ShutDownHook extends Thread {
		
		public void run() {
			
			customOut.writeToFile();
			customOut.closeFile();
			//			System.exit(0);
			
			
		}
		
		
	}
	
	
}
