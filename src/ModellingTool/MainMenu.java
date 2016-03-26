package ModellingTool;

import ModellingUtilities.molecularElements.SimpleProtein;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;
import java.util.Vector;


/**
 * Created by zivben on 06/03/16.
 */
public class MainMenu extends JPanel implements ActionListener {


		public static RunParameters params;
//	public static Properties params;

	static private final String newline = "\n";
	private JTextArea crystallographyModelingToolV0TextArea;
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
		Vector<String> rowOne = new Vector<>(2);
		rowOne.addElement("Map File");
		rowOne.addElement("None yet");

		Vector<Vector> rowData = new Vector<>();

		Vector<String> titlesList = new Vector<>(2);
		titlesList.addElement("Property");
		titlesList.addElement("File Path");

		CurrentData = new JTable(rowData, titlesList);
		chainListTable = new JTable(new ChainListTableModel());

		SpinnerNumberModel model1 = new SpinnerNumberModel(50.0, 0.0, 100.0, 10.0);
		threadLimit = new JSpinner(model1);


	}


	@Override
	public void actionPerformed(ActionEvent e) {

		DefaultTableModel model1 = (DefaultTableModel)(CurrentData.getModel());

		//Handle SCWRL EXE button action.
		if (e.getSource() == SCWRLExecutableButton) {
			int returnVal = fc.showOpenDialog(MainMenu.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				params.setSCWRLexe(file);
				System.out.println("SCWRL executable: " + file.getName() + " selected.");

				Vector<String> newRow = new Vector<>(2);
				newRow.addElement("SCWRL exe File");
				newRow.addElement(file.getPath());
				model1.addRow(newRow);

			} else {
				System.out.println("Select SCWRL Exe cancelled by user.");
			}


			//Handle PDB src button action.
		} else if (e.getSource() == PDBButton) {
			int returnVal = fc.showSaveDialog(MainMenu.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				System.out.println("Opening PDB file: " + file.getName() + ".");
				System.out.println(params.setPDBsrc(file,params));

				Vector<String> newRow = new Vector<>(2);
				newRow.addElement("PDB File");
				newRow.addElement(file.getPath());
				model1.addRow(newRow);

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

				Vector<String> newRow = new Vector<>(2);
				newRow.addElement("SFCheck exe File");
				newRow.addElement(file.getPath());
				model1.addRow(newRow);

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

				Vector<String> newRow =  new Vector<>(2);
				newRow.addElement("Map File");
				newRow.addElement(file.getPath());
				model1.addRow(newRow);

			} else {
				System.out.println("PDB selection canceled by user.");
			}


		} else if (e.getSource() == executeButton) {
			params.setHetAtmProcess(keepHetAtm.isSelected());
			params.setDebug(debug.isSelected());
			params.setThreadLimit((Double) threadLimit.getValue());

			if (saveDefaultsCheckBox.isSelected()){
				params.setProperty("Save Defaults",String.valueOf(true));
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


			} else{
				File configFile = new File("config.xml");
				configFile.delete();
			}
		}


	}

	private void populateChainsList(SimpleProtein protein) {
		ChainListTableModel model1 = (ChainListTableModel) chainListTable.getModel();

		for (SimpleProtein.ProtChain chain : protein){
			Vector row = new Vector();
			row.add(chain.getChainID());
			row.add(chain.getLength());
			row.add(false);
			row.add(true);
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
