package meshi.util;

import meshi.util.string.StringList;

import java.util.Formatter;


public class MeshiLog extends StringList {
	private final int FIELD_LENGTH = 10;
    private final int FIELDS_LENGTH = 6;

    public MeshiLog() { 
	super(); 
    } 

    protected void add(String log, String key) {
	    String KEY = "MESHILOG";
	    String formatString = KEY +" "+key+" %s";
	    int LINE_LENGTH = 240;
	    int effectiveLineLength = LINE_LENGTH - KEY.length() - key.length()-7;
	String line;
	while (log.length() > 0) { 
	    if (log.length() < effectiveLineLength) {
		Formatter frmt = new Formatter();
		frmt.format(formatString,log);
		line = frmt.out().toString();
		log = ""; 
	    } 
	    else { 
		int end = effectiveLineLength;
		while ((log.charAt(end) != ' ') & (end > 0))end--; 
		if (end == 0) throw new RuntimeException("bad formated log "+log);
		Formatter frmt = new Formatter();
		frmt.format(formatString,log.substring(0,end+1));
		line = frmt.out().toString();
		log = log.substring(end+1);
	    } 
	    add(line);
	} 
    }



    protected String field(String s) {
	    return field(s,FIELD_LENGTH);
    }
    protected String field(double d) {
	    return field(d,FIELD_LENGTH);
    }
    protected String fields(String s) {
	    return field(s,FIELDS_LENGTH);
    }
    protected String fields(double d) {
            return field(d,FIELDS_LENGTH);

    }

    private String field(String s, int length) {
	String out;
	if (s.length() <= length-1) {
	    out = s;
	    for (int i = 0; i <  (length-s.length());i++)
		out +=" ";
	}
	else out = s.substring(0,length-1)+" ";
	return out;
    }

    private String field(double d, int length) {
	String fmt;
	if (length == FIELD_LENGTH)  
	      fmt = "%-"+(length-1)+".2f";
	else 
   	      fmt = "%-"+(length-1)+".3f";
	String dString = (new Formatter()).format(fmt, d).toString();
	if ((!dString.contains(".")) &
	    (dString.length() >= length)) {
	    String dummy = "";
	    for (int i = 0; i < (length-1); i++) dummy += "*";
	    dummy += " ";
	    return dummy;
	}
	else return dString+" ";
    }	    
}
	



			
