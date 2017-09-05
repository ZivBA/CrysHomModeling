package meshi.applications.prediction.analysis;

// --Commented out by Inspection START (16:34 31/10/16):
//public class Exp extends MeshiList {
//    public Exp(File directory) {
//	super(new isExpTarget());
//	setComment("results from "+directory);
//	if (! directory.isDirectory()) throw new RuntimeException(directory+" not a directory");
//	String[] fileNames = directory.list();
//	for (int i = 0; i < fileNames.length; i++) {
//	    String name = fileNames[i];
//	    if (name.startsWith("1")) {
//		ExpTarget temp = new ExpTarget(new File(directory,name));
//		add((Object) temp);
//	    }
//	}
//    }
//
//    public String toString() {
//	String out =  "Exp "+comment()+"\n";
//	for (Iterator targets = iterator();targets.hasNext();) {
//	    StringTokenizer lines = new StringTokenizer(targets.next().toString(),"\n");
//	    while (lines.hasMoreTokens())
//		out += "\t"+lines.nextToken()+"\n";
//	}
//	return out;
//    }
//
//    public static class isExpTarget implements Filter {
//	public boolean accept(Object obj) {
//	    return (obj instanceof ExpTarget);
//	}
//    }
//}
// --Commented out by Inspection STOP (16:34 31/10/16)
