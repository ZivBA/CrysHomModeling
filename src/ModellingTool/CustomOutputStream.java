package ModellingTool;

import javax.swing.*;
import java.io.*;

/**
 * Created by zivben on 22/03/16.
 */
public class CustomOutputStream extends OutputStream {
	private JTextArea textArea;
	private String buffer;
	private File log;
	PrintWriter writer;

	public CustomOutputStream(JTextArea textArea) {
		this.textArea = textArea;
		buffer = "";
		log = new File(System.getProperty("user.dir")+"/CrysMod.log");
		try {
			 writer = new PrintWriter(log.getAbsolutePath(), "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void write(int b) throws IOException {
		// redirects data to the text area
		buffer += (String.valueOf((char)b));

		if (buffer.length() >= 1024 && b==10){
			textArea.append(buffer);
			buffer = "";
			if (textArea.getText().length() >= 32768) {
				writer.write(textArea.getText());
				textArea.setText("Written buffer to logfile\n");
			}
		}
		}
}
