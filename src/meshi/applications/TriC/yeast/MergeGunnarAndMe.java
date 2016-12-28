package meshi.applications.TriC.yeast;

import meshi.molecularElements.Atom;
import meshi.molecularElements.AtomList;
import meshi.util.file.File2StringArray;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

class MergeGunnarAndMe {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String nir =    "C:\\Users\\Nir\\TRiC\\Crystallography\\ADP\\ADP_positions\\Put_K\\refine_17_fixed_ADP_withK.pdb";
		String gunnar = "C:\\Users\\Nir\\TRiC\\Crystallography\\ADP\\ADP_positions\\Put_K\\refine_17_modified.pdb";
		String out =    "C:\\Users\\Nir\\TRiC\\Crystallography\\ADP\\ADP_positions\\Put_K\\refine_17_fixed_ADP_withK_with_SF.pdb";
		AtomList fullList = (new AtomList(nir)).filter(new AtomList.NonHydrogen()).noOXTFilter();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			String[] gFile = File2StringArray.f2a(gunnar);
			for (String aGFile : gFile) {
				if ((aGFile.length() < 5) || !aGFile.substring(0, 5).equals("ATOM ")) {
					bw.write(aGFile + "\n");
				} else if (aGFile.contains("OXT")) {
					bw.write(aGFile + "\n");
				} else {
					Atom atom = fullList.findAtomInListReturningAtom(aGFile.substring(12, 16).trim(), aGFile.substring(21, 22),
							Integer.parseInt(aGFile.substring(23, 26).trim()));
					String atomS = atom.toString();
					if (aGFile.substring(13, 16).equals("C5*") |
							aGFile.substring(13, 16).equals("C4*") |
							aGFile.substring(13, 16).equals("O4*") |
							aGFile.substring(13, 16).equals("C3*") |
							aGFile.substring(13, 16).equals("O3*") |
							aGFile.substring(13, 16).equals("C2*") |
							aGFile.substring(13, 16).equals("O2*") |
							aGFile.substring(13, 16).equals("C1*") |
							aGFile.substring(13, 16).equals("N9 ") |
							aGFile.substring(13, 16).equals("C8 ") |
							aGFile.substring(13, 16).equals("N7 ") |
							aGFile.substring(13, 16).equals("C5 ") |
							aGFile.substring(13, 16).equals("C6 ") |
							aGFile.substring(13, 16).equals("N6 ") |
							aGFile.substring(13, 16).equals("N1 ") |
							aGFile.substring(13, 16).equals("C2 ") |
							aGFile.substring(13, 16).equals("N3 ") |
							aGFile.substring(13, 16).equals("C4 ")) {
						bw.write(aGFile + "\n");
						System.out.print("kept from original: " + aGFile + "\n");
					} else {
						bw.write(aGFile.substring(0, 26) + atomS.substring(26, 54) + aGFile.substring(54) + "\n");
						System.out.print(aGFile.substring(0, 26) + atomS.substring(26, 54) + aGFile.substring(54) + "\n");
					}
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		


	}

}
