package meshi.applications.prediction;

import meshi.molecularElements.AtomList;
import meshi.sequences.AtomAlignment;
import meshi.sequences.AtomAlignmentColumn;
import meshi.util.file.MeshiWriter;

import java.util.Iterator;

// --Commented out by Inspection START (16:38 31/10/16):
//class PredictionAtomList extends AtomList {
//	public PredictionAtomList(AtomAlignment alignment, int index) {
//		super();
//		for (Iterator columns = alignment.iterator(); columns.hasNext();) {
//			AtomAlignmentColumn column = (AtomAlignmentColumn) columns.next();
//			add(column.cell(index).object());
//		}
//	}
//
//	public void print(String fileName) {
//		try {
//			MeshiWriter writer = new MeshiWriter(fileName);
//			print(writer);
//		}
//		catch (Exception ex) {throw new RuntimeException(ex);}
//	}
//}
// --Commented out by Inspection STOP (16:38 31/10/16)
