package meshi.molecularElements;

import meshi.PDB.PdbReader;
import meshi.molecularElements.residuesExtendedAtoms.ResidueExtendedAtoms;
import meshi.parameters.Residues;
import meshi.util.MeshiException;
import meshi.util.MeshiList;
import meshi.util.file.MeshiLineReader;
import meshi.util.filters.Filter;
import meshi.util.string.StringList;

import java.io.File;
import java.util.Iterator;
import java.util.StringTokenizer;

public class ProteinIterator implements Iterator, Residues {
    private final ResidueCreator creator;
	private Iterator fileIterator;
    private final StringList names = new StringList();
	private static final String kolDichphin = ("a b c d e f g h i j k l m n o p q r s t u v w x y z "+
					 "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z "+
					 " 1 2 3 4 5 6 7 8 9 0");
    public ProteinIterator(String dataBase) {
	this(dataBase, kolDichphin, new ResidueExtendedAtoms(DO_NOT_ADD_ATOMS));
    }

    public ProteinIterator(String dataBase, 
			   ResidueCreator creator) {
	this(dataBase, kolDichphin, creator);
    }

    public ProteinIterator(String dataBase, String files) {
	this(dataBase, files, new ResidueExtendedAtoms(DO_NOT_ADD_ATOMS));
    }

    private ProteinIterator(String dataBase, String files,
                            ResidueCreator creator) {
	this.creator = creator;
	    File dataBase1 = new File(dataBase);
	try {
	    if (! dataBase1.isDirectory()) {
		if (! dataBase1.exists()) throw new RuntimeException(dataBase+" does not exist");
		else throw new RuntimeException(dataBase+" is not a directory");
	    }
		MeshiList fileList = new MeshiList(new IsAfile());
	    if (new File(files).exists()) {
		MeshiLineReader reader = new MeshiLineReader(files);
		String line;
		File file;
		while ((line = reader.readLine()) != null) {
		    if ((line != "") & (!line.startsWith("#"))) 
			fileList.add(file = new File(dataBase,line));
		}
	    }
	    else {
		String key;
		StringTokenizer keys;
		boolean notFound = true;
		File[] fileArray = dataBase1.listFiles();
		    for (File aFileArray : fileArray) {
			    notFound = true;
			    keys = new StringTokenizer(files);
			    while (keys.hasMoreTokens() & notFound) {
				    key = keys.nextToken();
				    if (aFileArray.getName().indexOf(key) >= 0) {
					    fileList.add(aFileArray);
					    notFound = false;
				    }
			    }
		    }
	    }
	    fileIterator = fileList.iterator();
	}
	catch (Exception e) { throw new RuntimeException(" "+e); }
    }
    

    public boolean hasNext(){return fileIterator.hasNext(); }
    
    public Object next() {
	File file;
	if ((file = (File) fileIterator.next()) == null) return null;
	names.add(file.getName());
	PdbReader pdbReader;
	try {
	     pdbReader = new PdbReader(file);
	}
 	catch (MeshiException e) {
		throw new RuntimeException("Cannot open file "+file+"\n"+e);
        }	
	Protein protein = new Protein((new AtomList(pdbReader)), creator);
 	try {
	    pdbReader.close();
	}
	catch (Exception e) { 
	    throw new RuntimeException("Errot while closing "+pdbReader+"\n"+e); 
	}
	return protein;
    }

    public Protein nextProtein() {return (Protein) next();}

    public void remove() {
	throw new RuntimeException("remove not implemented");
    }

    public StringList names() { return names;}

    private static class IsAfile implements Filter {
	public boolean accept(Object obj) {
	    return (obj instanceof File);
	}
    }
}

	    
    
    
	
	    

