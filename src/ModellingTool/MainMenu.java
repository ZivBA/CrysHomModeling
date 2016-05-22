package ModellingTool;

import ModellingUtilities.molecularElements.SimpleProtein;
import ScoreUtilities.SFCheckIntegration.SFCheckThread;
import ScoreUtilities.ScoringGeneralHelpers;
import ScoreUtilities.scwrlIntegration.SCWRLrunner;
import UtilExceptions.MissingChainID;
import UtilExceptions.SfCheckResultError;

import javax.jnlp.BasicService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by zivben on 06/03/16.
 * Main Menu GUI class - holds the user interface and execution instructions.
 */
public class MainMenu extends JPanel implements ActionListener {


	public static RunParameters params;
	static private final String newline = "\n";
	private JTextArea crystallographyModelingToolV0TextArea;
	private JButton SCWRLExecutableButton;
	private JButton SFCheckExecutableButton;
	private JButton mapButton;
	private JButton PDBButton;
	private JTable CurrentData;
	private JPanel JPanelMain;
	private JTextArea selectWhichChainsAreTextArea;
	private JScrollPane logPane;
	private JTextArea log;
	private JSpinner threadLimit;
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
	private JTextArea pleasePointToSCWRLTextArea;
	private JTextArea pleaseChooseTheSourceTextArea;
	private JTextArea limitNumberOfThreadsTextArea;
	private JTabbedPane tabbedPane1;
	private JTextArea pleaseEnterTheRequestedTextArea;
	private JButton FASTAButton;
	private JRadioButton fullFastaRadioButton;
	private JRadioButton inputSeqRadioButton;
	FASTAinput fastaDialog;

	protected static Integer scwrlProgressCounter = 0;
	protected static int sfckProgressCounter = 0;

	int totalNumberOfFilesToProcess = 0;
	public static ConcurrentLinkedQueue<File> filesToSFcheck;


	static protected CRYS_Score crysScore;
	protected List<List<SCWRLrunner>> SCWRLruns;
	public static List<String[]> SFCheckResultSet = new LinkedList<>();
	private ExecutorService executorSC;
	private static ExecutorService executorSF;

	static BasicService basicService = null;
	private String fastaSequence;
	private File fastaFile;

	public MainMenu() {
		super(new BorderLayout());

		fc = new JFileChooser();
		fastaDialog = new FASTAinput(fc);
		fastaDialog.pack();

		// add listeners to buttons
		SCWRLExecutableButton.addActionListener(this);
		SFCheckExecutableButton.addActionListener(this);
		mapButton.addActionListener(this);
		PDBButton.addActionListener(this);
		executeButton.addActionListener(this);
		debug.addActionListener(this);
		FASTAButton.addActionListener(this);


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

		//		try {
		//			basicService = (BasicService)
		//					ServiceManager.lookup("javax.jnlp.BasicService");
		//		} catch (UnavailableServiceException e) {
		//			System.err.println("Lookup failed: " + e);
		//		}

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

			// handle FAST button
		} else if (e.getSource() == FASTAButton){
			fastaDialog.setLocationRelativeTo(this.getRootPane());
			fastaDialog.setVisible(true);

			if (fastaDialog.fileChooserResult == JFileChooser.APPROVE_OPTION){
				this.fastaFile = fastaDialog.fastaFile;
				this.fastaSequence = fastaDialog.fastaSequence;
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
		log.setText("");
		log.append("Starting run!\n");
		params.setHetAtmProcess(keepHetAtm.isSelected());
		params.setDebug(debug.isSelected());
		params.setThreadLimit((Integer) threadLimit.getValue());
		params.setSaveDefaults(saveDefaultsCheckBox.isSelected());
		params.setThreadingSelection(fullFastaRadioButton.isSelected());
		scwrlProgress.setValue(0);
		sfchkProgress.setValue(0);
		scwrlProgressCounter = 0;
		sfckProgressCounter = 0;
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


		params.setChainsToStrip(chainsToStrip.toArray(new Character[chainsToStrip.size()]));
		params.setSymmetricHomologues(homologues.toArray(new Character[homologues.size()]));
		params.setChainToProcess(chainsToProcess);



		int threads = params.getThreadLimit();
		executorSC = Executors.newFixedThreadPool(threads - 1);
		executorSF = Executors.newFixedThreadPool(1);

		ScoringGeneralHelpers.debug = debug.isSelected();
		crysScore = new CRYS_Score(params);

		if (fastaFile != null && fastaSequence == null){
			crysScore.setFastaFile(fastaFile);
		} else if (fastaSequence != null && fastaFile == null){
			crysScore.setFastaSequence(fastaSequence);
		}


		totalNumberOfFilesToProcess = crysScore.getExpectedTotalNumberOfFiles();
		scwrlProgress.setMaximum(totalNumberOfFilesToProcess);
		sfchkProgress.setMaximum(totalNumberOfFilesToProcess);


		try {

			SCWRLruns = crysScore.iterateAndGenScwrl();
			if (SCWRLruns == null) {
				log.append("Finished processing existing files\n");
			} else {
				for (List<SCWRLrunner> SCWRLTasks : SCWRLruns) {
					for (SCWRLrunner singleRun : SCWRLTasks) {
						singleRun.addPropertyChangeListener(new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent e) {
								if (e.getPropertyName().equals("progress")) {
									scwrlProgressCounter++;
									scwrlProgress.setValue(scwrlProgressCounter);
									checkSCWRLfile();
								}

							}
						});
						executorSC.submit(singleRun);
					}
				}
			}
		} catch (IOException | MissingChainID e) {
			System.err.println(e.getMessage());
			System.out.println("There was a problem processing one of the files.");
		}


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
		try {
			fullFastaRadioButton.setSelected(Boolean.parseBoolean(params.getProperty(RunParameters.FULL_FASTA)));
		} catch (Exception e){
			System.out.println("There was an issue parsing FASTA threading preference. no worries.");
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
		if (sfckProgressCounter == totalNumberOfFilesToProcess) {
			log.append("Processing Results\n");
			try {
				crysScore.processSFCheckResults(SFCheckResultSet);
			} catch (SfCheckResultError | IOException e) {
				log.append(e.getMessage() + "\n");

			}
			log.append("Finished Processing Results, You may exit!\n");

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

}
