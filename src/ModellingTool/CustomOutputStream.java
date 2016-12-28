package ModellingTool;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by zivben on 22/03/16.
 */
public class CustomOutputStream extends OutputStream {
	private final JTextArea textArea;
	private String buffer;
	private PrintWriter writer;
	String logFile;
	
	CustomOutputStream(JTextArea textArea) {
		this.textArea = textArea;
		buffer = "";
		String curPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		
		logFile = curPath.substring(0, curPath.lastIndexOf(File.separatorChar)) + "/CrysModeling.log";
		File log = new File(logFile);
		if (log.exists()) {
			File backupLog = new File(logFile + ".backup");
			try {
				Files.copy(log.toPath(), backupLog.toPath(), REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			writer = new PrintWriter(log.getAbsolutePath(), "UTF-8");
			textArea.append("Writing to: " + log.getAbsolutePath() + "\n");
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
			if (textArea.getText().length() >= 131072) {
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
		textArea.setText("\n######  Written buffer to logfile: ######\n ######  " + logFile + "  ######\n");
	}
	
	public void write(String s) {
		System.out.println(s);
	}
	
	public void closeFile() {
		writer.close();
	}
}
