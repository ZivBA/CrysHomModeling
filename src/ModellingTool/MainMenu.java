package ModellingTool;

import ModellingUtilities.molecularElements.SimpleProtein;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by zivben on 06/03/16.
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

	public MainMenu() {
		super(new BorderLayout());

		fc = new JFileChooser();

		// add listeners to buttons
		SCWRLExecutableButton.addActionListener(this);
		SFCheckExecutableButton.addActionListener(this);
		mapButton.addActionListener(this);
		PDBButton.addActionListener(this);
		executeButton.addActionListener(this);

		//redirect standard output to onscreen log.
		PrintStream printStream = new PrintStream(new CustomOutputStream(log));
		if (debug.isSelected()) {
			System.setErr(printStream);
			System.setOut(printStream);
		} else {
			System.setOut(printStream);
		}

		updateFields();


	}


	private static void createAndShowGUI() {

		MainMenu menu = new MainMenu();
		JFrame frame = new JFrame("Crystallography Modeling Tool V0.√(π)");
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
		SpinnerNumberModel model1 = new SpinnerNumberModel(50, 0, 100, 10);
		threadLimit = new JSpinner(model1);
		updateFields();

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
			params.setHetAtmProcess(keepHetAtm.isSelected());
			params.setDebug(debug.isSelected());
			params.setThreadLimit((Integer) threadLimit.getValue());
			params.setSaveDefaults(saveDefaultsCheckBox.isSelected());

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
			for (int i=0; i< chainlist.getRowCount(); i++){
				if (chainlist.getValueAt(i,2).equals(true)){
					chainsToProcess = (Character) chainlist.getValueAt(i,0);

				}
				if (chainlist.getValueAt(i,3).equals(true)){
					chainsToStrip.add((Character) chainlist.getValueAt(i,0));
				}
				if (chainlist.getValueAt(i,4).equals(true)){
					homologues.add((Character) chainlist.getValueAt(i,0));
				}
			}
			params.setChainsToStrip(chainsToStrip.toArray(new Character[chainsToStrip.size()]));
			params.setSymmetricHomologues(homologues.toArray(new Character[homologues.size()]));
			params.setChainToProcess(chainsToProcess);

			int cores =  Runtime.getRuntime().availableProcessors();
			ExecutorService executor = Executors.newFixedThreadPool(params.getThreadLimit() / cores * 100);
			Runnable worker = new WorkerThread(params, executor);
			executor.execute(worker);

		}
		updateFields();

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
		// get number of cores
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
}
