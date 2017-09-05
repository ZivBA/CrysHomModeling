package meshi.optimizers;

public class LineSearchException extends Exception {
	
	public final int code;
	
	public LineSearchException(int code, String msg) {
		super(msg);		
		this.code = code;
	}
}



