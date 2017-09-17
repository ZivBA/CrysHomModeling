package ScoreUtilities.EMCheckIntegration;

import ModellingTool.MainMenu;
import ModellingTool.RunParameters;
import ModellingUtilities.molecularElements.AminoAcid;
import ModellingUtilities.molecularElements.SimpleAtom;
import ModellingUtilities.molecularElements.SimpleProtein;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static ModellingUtilities.molecularElements.ProteinActions.acidToIndex;
import static org.apache.commons.lang3.StringUtils.substringBetween;

/**
 * SFCheck worker thread instance.
 * create these when you want to run SFCheck against some PDB file, then pass the worker to an execution pool for processing.
 */
public class EMCheckThread extends SwingWorker<Object[], Void> {
	private static AtomicLong tempFolderCounter = new AtomicLong(0);
	private static MRC_Map_New myMap;
	int testResidueIndex;
	char requestedChain;
	private Integer testResiduePosition;
	private RunParameters params;
	private SimpleProtein protToProcess;
	private boolean fakeRun = false;
	private String[] res;
	private boolean cifNotSF = false;

	/**
	 * constructor - gets the PDB file to check and the run parameters object.
	 */
	public EMCheckThread(File tempProt, RunParameters params) throws IOException {
		try {
			testResiduePosition = Integer.valueOf(substringBetween(tempProt.getAbsolutePath(), "_res_", "_to"));
		} catch (NumberFormatException e) {
			System.err.println("Problem with some SCWRL file?");
			System.out.println("Problematic file is:");
			System.out.println(tempProt.getAbsolutePath());
			System.out.println("also this is the filename analized: " + tempProt.getName());
			throw e;
		}
		requestedChain = params.getChainToProcess();
		testResidueIndex = acidToIndex(
				substringBetween(tempProt.getName(), "_to_", "_SCWRLed"));
		protToProcess = new SimpleProtein(tempProt, params);
		this.params = params;

	}


	public static void setMRCMap(Path MapPath, JButton executeButton) throws IOException {
		SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {

				// mimic some long-running process here...
				myMap = new MRC_Map_New(MapPath.toString());
				System.out.flush();
				return null;
			}
		};

		Window win = SwingUtilities.getWindowAncestor(executeButton);
		final JDialog dialog = new JDialog(win, "Dialog", Dialog.ModalityType.APPLICATION_MODAL);
		mySwingWorker.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("state")) {
					if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
						dialog.dispose();
					}
				}
			}
		});
		mySwingWorker.execute();
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(progressBar, BorderLayout.CENTER);
		panel.add(new JLabel("Reading map file......."), BorderLayout.PAGE_START);
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(win);
		dialog.setVisible(true);
	}

	public static MRC_Map_New getMRCMap() {

		return myMap;
	}

	private double scoreSingleAtom(SimpleAtom atom) {
		float[] coords = atom.getAtomCoords();

		try {
			atom.setAtomScore(myMap.val(coords[0], coords[1], coords[2]));
		} catch (RuntimeException e) {
			System.out.println("Warning, PDB contains coordinate value outside MRC map scope.");
			System.out.println("Atom " + atom.getName() + " from residue " + atom.getaAcidName() + " at coordinates " +
					Arrays.toString(atom.getAtomCoords()) + " is the problem.");
			System.out.println("problematic line from PDB is: \n" + atom.getOriginalString() + "\n");
			atom.setAtomScore(0.0);
		}
		return atom.getAtomScore();
	}

	@Override
	/**
	 * execution method - returns a String array of the results - first line is the protein name (which position was changed to which residue)
	 * second line is the result - Correlation Coefficient.
	 */
	protected Object[] doInBackground() throws Exception {

		Object[] results = new Object[0];
		for (SimpleProtein.ProtChain chain : protToProcess) {
			if (chain.getChainID() == requestedChain || requestedChain == '\0') {
				AminoAcid residue = chain.getAminoAcidAt(testResiduePosition);
//				for (AminoAcid residue : chain) {

				if (acidToIndex(residue.getName()) == testResidueIndex) {

					double resSum = 0;
					double backBoneSum = 0;
					for (SimpleAtom atom : residue) {
						if (atom.isBackbone()) {
							backBoneSum += scoreSingleAtom(atom);
						} else {
							resSum += scoreSingleAtom(atom);
						}
					}

					SimpleProtein.ProtChain originalChain = protToProcess.getChain(residue.getChainID());
					try {
						results = new Object[]{residue.getChainID(), acidToIndex(residue.getName()), residue.getPosition(),
								resSum, backBoneSum};
//							originalChain.resIntensityValueMatrix[acidToIndex(residue.getName())][residue.getPosition()] = resSum;
//							originalChain.backBoneIntensityValueMatrix[acidToIndex(residue.getName())][residue.getPosition()] = backBoneSum;
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("OOB exception at residue: " + residue.getName() + " in position: " + residue.getPosition());
					}
				}
//				}
			}
		}
		return results;
	}

	@Override
	/**
	 * when done, add the result to the SFCheck resultset collection and advance the progress counter.
	 */
	protected void done() {
		try {

			MainMenu.mapIntensityResults.add(get());
			setProgress(100);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}