package ModellingTool;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Paths;

class FASTAinput extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTabbedPane tabbedPane1;
	private JTextPane inputFASTAfield;
	private JPanel fileChooserPane;
	private final JFileChooser fc;
	public int fileChooserResult;
	public String fastaSequence;
	File fastaFile;

	public FASTAinput(JFileChooser fc) {
		this.fc =fc;
		fc.setCurrentDirectory(Paths.get("").toAbsolutePath().toFile());
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);


		buttonOK.addActionListener(e -> onOK());

		buttonCancel.addActionListener(e -> onCancel());

		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void onOK() {
		fileChooserResult = JFileChooser.APPROVE_OPTION;
		fastaSequence = inputFASTAfield.getText();
		fastaFile = null;
		dispose();
	}

	private void onCancel() {
		fileChooserResult = JFileChooser.CANCEL_OPTION;
		dispose();
	}


	private void createUIComponents() {
		// TODO: place custom component creation code here
		fileChooserPane = new JPanel();
		fileChooserPane.add(fc);
		fc.addActionListener(e -> {
			if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
				fastaFile = fc.getSelectedFile();
				fastaSequence = null;
				fileChooserResult = JFileChooser.APPROVE_OPTION;
				dispose();

			}else{
				onCancel();
			}
		});

	}

	//	public static void main(String[] args) {
//		FASTAinput dialog = new FASTAinput();
//		dialog.pack();
//		dialog.setVisible(true);
//		System.exit(0);
//	}
}
