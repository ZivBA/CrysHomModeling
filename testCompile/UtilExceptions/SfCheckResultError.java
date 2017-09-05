package UtilExceptions;

/**
 * Created by zivben on 28/04/16.
 */
public class SfCheckResultError extends Throwable {

	public SfCheckResultError(String s) {
		super("There was a problem parsing the SFCheck result for the file: "+s+"\n");
	}
}
