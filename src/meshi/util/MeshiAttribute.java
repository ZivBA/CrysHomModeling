package meshi.util;
/*
 * All the additional attributes of Distance are type of DistanceAtribute
 */
public interface MeshiAttribute{
    int LENNARD_JONES_ELEMENT_ATTRIBUTE = 0;
    int EXCLUDED_VOLUME_ELEMENT_ATTRIBUTE = 1;
    int HYDROGEN_BONDS_ATTRIBUTE = 2;
    int CN_ATTRIBUTE = 3;
    int SEQUENCE_ALIGNMENT_COLUMN_ATTRIBUTE = 4;
    int SOLVATE_ALL_ATOM_ATTRIBUTE = 5;
    int SOLVATE_CA_ATTRIBUTE = 6;
    int SOLVATE_ROT1_ATTRIBUTE = 7;
    int EV_ROT1_ATTRIBUTE = 8;
    int SOLVATE_EXTRACTION_ATTRIBUTE = 9;
    int key();
}
