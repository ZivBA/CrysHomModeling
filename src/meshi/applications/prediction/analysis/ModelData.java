package meshi.applications.prediction.analysis;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

public class ModelData {
    public final Hashtable data;
    public final File file;
    public final boolean valid;
	private final int numberOfResidues;
    public ModelData(Model model) {
	data = model.data;
	file = model.file;
	numberOfResidues = model.numberOfResidues;
	    Exception exception = model.exception;
	valid = model.valid();
    }
    
    public double getDoubleValue(String key, boolean perResidue) {
	double factor;
	if (perResidue) factor = numberOfResidues;
	else factor = 1;
	try {
	    return new Double((String) data.get(key)) /factor;
	}
	catch (Exception ex) {throw new RuntimeException("Cannot get "+key+" or cannot parse "+
							 data.get(key)+" as double.");}
    }


    public void tabulate(String[] keys) {
	System.out.printf("%-9d ",numberOfResidues);
	    for (String key : keys) {
		    double value;
		    String element = null;
		    element = (String) data.get(key);
		    if (element == null) {
			    System.out.println("\n" + "A problem with " + file.getPath());
			    for (Enumeration dataKeys = data.keys(); dataKeys.hasMoreElements(); )
				    System.out.println(dataKeys.nextElement());
			    throw new RuntimeException("no key " + key);
		    }
		
		    try {
			    value = new Double(element);
		    } catch (Exception ex) {
			    throw new RuntimeException("cannot parse " +
					    element + " as double." +
					    "key = " + key);
		    }
		    System.out.printf("%-9.3f ", value);
	    }
	System.out.println(file.getPath());
    }

    public String getXY(String keyX, String keyY, double yShift, boolean perResidue) {
	double factor;
	if (perResidue) factor = numberOfResidues;
	else factor = 1;

	double valueX = new Double((String) data.get(keyX));
	double valueY = new Double((String) data.get(keyY)) /factor+yShift;
	return ""+valueX+"\t"+valueY+"\n";
    }
	
    public String GdtVsSolvate(double yShift) {
	double gdt = new Double((String) data.get("T2.gdt1"));
	double solvate = new Double((String) data.get("T2.Solvate")) +yShift;
	return ""+gdt+"\t"+solvate+"\n";
    }

    public String toString() {
	return "ModelData of "+file;
    }
}
	
