package ModellingTool;

import javax.swing.*;
import java.io.*;

/**
 * Created by zivben on 22/03/16.
 */
public class CustomOutputStream extends OutputStream {
	private final JTextArea textArea;
	private String buffer;
	private PrintWriter writer;
	
	CustomOutputStream(JTextArea textArea) {
		this.textArea = textArea;
		buffer = "";
		File log = new File(System.getProperty("user.dir") + "/CrysModeling.log");
		try {
			writer = new PrintWriter(log.getAbsolutePath(), "UTF-8");
			textArea.append("Writing to: "+ log.getAbsolutePath());
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}



	@Override
	public void write(int b) throws IOException {
		// redirects data to the text area
		buffer += (String.valueOf((char) b));

		if (buffer.length() >= 1024 && b == 10) {
			textArea.append(buffer);
			buffer = "";
			if (textArea.getText().length() >= 65536) {
				writeToFile();
				textArea.setText("######  Written buffer to logfile  ######\n");
			}
		}
	}

	@Override
	public void flush() {
		textArea.append(buffer);
		textArea.setCaretPosition(textArea.getText().length() - 1);
		buffer = "";
	}

	public void writeToFile() {
		textArea.append(buffer);
		textArea.setCaretPosition(textArea.getText().length() - 1);
		buffer = "";
		writer.write(textArea.getText());
		textArea.setText("\n######  Written buffer to logfile  ######\n");
	}

	public void write(String s) {
		System.out.println(s);
	}
	
	public void closeFile() {
		writer.close();
	}
}
