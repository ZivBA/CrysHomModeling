package ModellingTool;

import ModellingUtilities.molecularElements.SimpleProtein;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;


/**
 * Created by zivben on 06/03/16.
 * Main Menu GUI class - holds the user interface and execution instructions.
 */
public class MainMenu extends JPanel implements ActionListener {


	private static RunParameters params;
	private final File configFile;
	private JTextArea crystallographyModelingToolV0TextArea;
	private JButton SCWRLExecutableButton;
	private JButton SFCheckExecutableButton;
	private JButton mapButton;
	private JButton PDBButton;
	private JTable CurrentData;
	private JPanel JPanelMain;
	private JTextArea selectWhichChainsAreTextArea;
	private JScrollPane logPane;
	JTextArea log;
	private JSpinner threadLimit;
	private JButton QueueThreadButton;
	private JTable chainListTable;
	private JCheckBox saveDefaultsCheckBox;

	private final JFileChooser fc;
	JCheckBox debug;
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
	JProgressBar threadProgress;
	private final FASTAinput fastaDialog;
	
	JProgressBar scwrlProgress;
	JProgressBar sfchkProgress;
	private JButton SWISSPROTButton;
	private JButton executeButton;
	private ConcurrentLinkedDeque<MainProgramThread> runs = new ConcurrentLinkedDeque<>();
	
	

	
	private MainMenu() {
		super(new BorderLayout());

		fc = new JFileChooser();
		fc.setCurrentDirectory(Paths.get("").toAbsolutePath().toFile());
		JFileChooser fc2 = new JFileChooser();
		fastaDialog = new FASTAinput(fc2);
		fastaDialog.pack();

		// add listeners to buttons
		SCWRLExecutableButton.addActionListener(this);
		SFCheckExecutableButton.addActionListener(this);
		mapButton.addActionListener(this);
		PDBButton.addActionListener(this);
		QueueThreadButton.addActionListener(this);
		debug.addActionListener(this);
		FASTAButton.addActionListener(this);
		SWISSPROTButton.addActionListener(this);

		// define or create config file:
		String curPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		configFile = new File(curPath.substring(0,curPath.lastIndexOf(File.separatorChar))+"/config.xml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//redirect standard output to onscreen log.
		final CustomOutputStream customOut = new CustomOutputStream(log);
		params.setCustomOutputStream(customOut);
		
		
		
	}

	private static void createAndShowGUI() {

		JFrame frame = new JFrame("Crystallography Modeling Tool V0.√(π)");
		MainMenu menu = new MainMenu();
		frame.setContentPane(menu.JPanelMain);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		

	}
	
	public void setParams(RunParameters params) {
		MainMenu.params = params;
		updateFields();
		populateChainsList(params.getProtein());
	}
	
	
	private void createUIComponents() {
		// populate the files tables
		Object rowData[][] = {
				{"SCWRL exe", "..."},
				{"SFChck exe", "..."},
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

			// handle FAST button
		} else if (e.getSource() == FASTAButton) {
			fastaDialog.setLocationRelativeTo(this.getRootPane());
			fastaDialog.setVisible(true);

			if (fastaDialog.fileChooserResult == JFileChooser.APPROVE_OPTION) {
				params.setFASTAFile(fastaDialog.fastaFile);
				params.setFASTASeq(fastaDialog.fastaSequence);
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
				System.out.println("Select SFcheck Exe cancelled by user.\n");
			}

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


		} else if (e.getSource() == QueueThreadButton) {
			queueThread();

		} else if (e.getSource() == executeButton){
			startExecution();
		}
		updateFields();

	}
	
	private void startExecution() {
		for (MainProgramThread thread : runs){
			try {
				thread.get();
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void queueThread() {
		
		
		
		params.setHetAtmProcess(keepHetAtm.isSelected());
		params.setDebug(debug.isSelected());
		params.setThreadLimit((Integer) threadLimit.getValue());
		params.setSaveDefaults(saveDefaultsCheckBox.isSelected());
		params.setThreadingSelection(fullFastaRadioButton.isSelected());
		
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
		}else{
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
				System.out.println("Saved values to "+ configFile.getAbsolutePath()+" param file.\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}


		} else {
			configFile.delete();
		}


		params.setChainsToStrip(chainsToStrip.toArray(new Character[chainsToStrip.size()]));
		params.setSymmetricHomologues(homologues.toArray(new Character[homologues.size()]));
		params.setChainToProcess(chainsToProcess);


		

		MainProgramThread mainThread = new MainProgramThread(params, this);
		runs.push(mainThread);
	}

	 

	void updateFields() {
		DefaultTableModel model1 = (DefaultTableModel) (CurrentData.getModel());

		model1.setValueAt(params.getProperty(RunParameters.SCWRLEXE), 0, 1);
		model1.setValueAt(params.getProperty(RunParameters.SFCHECK_PATH), 1, 1);
		model1.setValueAt(params.getProperty(RunParameters.MAPSRC), 2, 1);
		model1.setValueAt(params.getProperty(RunParameters.PDBSRC), 3, 1);
		model1.setValueAt(params.getProperty(RunParameters.SWISSPROTpath), 4,1);
		
		try {
//			populateChainsList(params.getProtein());
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
		try{
			TableModel chainlist = chainListTable.getModel();
			LinkedList<Character> chainsToStrip = new LinkedList<>(java.util.Arrays.asList(params.getChainsToStrip()));
			LinkedList<Character> homologues = new LinkedList<>(java.util.Arrays.asList(params.getSymmetricHomologues()));
			Character chainToProcess = params.getChainToProcess();
			for (int i = 0; i < chainlist.getRowCount(); i++) {
				if (chainlist.getValueAt(i, 0).equals(chainToProcess)) {
					chainlist.setValueAt(true, i, 2);

				}
				if (chainsToStrip.contains(chainlist.getValueAt(i, 0))) {
					chainlist.setValueAt(true,i,3);
				}
				if (homologues.contains(chainlist.getValueAt(i,0))) {
					chainlist.setValueAt(true,i,4);
				}
			}

		} catch (Exception ignored){

		}
		System.out.flush();

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
		model1.fireTableDataChanged();
	}

	public static void main(String[] args) throws FileNotFoundException {
		params = new RunParameters();
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		SwingUtilities.invokeLater(() -> {
			//Turn off metal's use of bold fonts
			UIManager.put("swing.boldMetal", Boolean.FALSE);
			createAndShowGUI();
		});


	}




}
