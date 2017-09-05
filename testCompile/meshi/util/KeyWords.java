package meshi.util;
public interface KeyWords {
    //---------------------------- protein -------------------------------
    Key SEQUENCE = new Key("sequence");
    Key SECONDARY_STRUCTURE = new Key("secondary");
    Key MODEL_NUMBER = new Key("modelNumber");
    Key CLASH_DISTANCE = new Key("clashDistance");
    Key MAX_CLASHES = new Key("maxNumberOfClashes");
    Key N_TRYS = new Key("nTrays");

    //---------------------------- minimization -------------------------------
    Key MINIMIZE = new Key("minimize"); // First word for full minimization
    Key RELAX = new Key("relax"); // First word for short relaxation minimization with SteepestDecent
    Key TOLERANCE = new Key("tolerance");
    Key MAX_STEPS = new Key("maxSteps");
    Key REPORT_EVERY = new Key("reportEvery");
    
    //--------------------------------- energy  -----------------------------
    Key PARAMETERS_DIRECTORY = new Key("parameters");

    //--------------------------------- energy terms -----------------------------
    Key LENNARD_JONES_CA = new Key("LennardJonesCa");
    Key LENNARD_JONES = new Key("LennardJones");
    Key ANGLE_ENERGY = new Key("angleEnergy");
    Key BOND_ENERGY = new Key("bondEnergy");
    Key CONSENSUS_ENERGY = new Key("consensusEnergy");
    Key PLANE_ENERGY = new Key("planeEnergy");
    Key OUT_OFPLANE_ENERGY = new Key("outOfPlaneEnergy");
    Key TEMPLATE_DISTANCE_CONSTRAINS = new Key("templateDistanceConstrains");
    Key DISTANCE_CONSTRAINS_ENERGY = new Key("distanceConstrainsEnergy");
    Key INFLATE_ENERGY = new Key("inflateEnergy");
    Key VOLUME_CONSTRAIN = new Key("volumeConstrainWeight");
    Key HYDROGEN_BONDS = new Key("hydrogenBonds");
    Key HYDROGEN_BONDS_PAIRS = new Key("hydrogenBondsPairs");
    Key TWO_TORSIONS_ENERGY = new Key("twoTorsionsEnergy");
    Key FLAT_RAMACH_ENERGY = new Key("flatRamachEnergy");
    Key PROPENSITY_TORSION_ENERGY = new Key("propensityTorsionEnergy");
    Key ALPHA_ANGLE_ENERGY = new Key("alphaAngleEnergy");
    Key ALPHA_TORSION_ENERGY = new Key("alphaTorsionEnergy");
    Key SOLVATE_ENERGY = new Key("solvateEnergy");
    Key EXCLUDED_VOL = new Key("excludedVolume");
    Key ELECTROSTATICS = new Key("electrostatics");
    Key DIELECTRIC_CONSTANT = new Key("dielectricConstant");
    //final Key EVALUATED_LOCATION_ENERGY = new Key("tetherEnergy");
    Key TETHER_ENERGY = new Key("tetherEnergy");
    Key CALPHA_HYDROGEN_BONDS = new Key("cAlphaHydrogenBonds");
    Key CALPHA_HYDROGEN_BONDS_PLANE = new Key("cAlphaPlane");
    Key HYDROGEN_BONDS_ANGLES = new Key("hydrogenBondsAngles");
    Key HYDROGEN_BONDS_PLANE = new Key("hydrogenBondsPlane");
    //------------------------ inflate --------------------------   
    Key RMS_TARGET = new Key("RmsTarget");
    //------------------------ template based distance constrains --------------------------   
    Key INTRA_SEGMENT_FACTOR = new Key("intraSegmentFactor");
    Key INTRA_SEGMENT_TOLERANCE = new Key("intraSegmentTolerance");
    Key INTER_SEGMENT_FACTOR = new Key("interSegmentFactor");
    Key INTER_SEGMENT_TOLERANCE = new Key("interSegmentTolerance");
    Key SATURATION = new Key("saturation");
    Key UNSATISFIED_CUTTOF = new Key("unsatisfiedCutoff");
    Key UP_TO_CUTOFF = new Key("upToCutoff");
    Key DISTANCE_CONSTRAINS_MASK = new Key("constrain");

    //--------------------------------- Superimpose -------------------------------------
    Key SUPERIMPOSE = new Key("superimpose");
    Key REFERENCE = new Key("reference");
    Key MODE = new Key("mode");
    Key ALL_CA = new Key("allCa");
    

    //--------------------------------- minimization loop -------------------------------------
    Key MINIMIZATION_LOOP = new Key("minimizationLoop");
    Key ITERATIONS_CA = new Key("nIterationsCA");
    Key ITERATIONS_BACKBONE = new Key("nIterationsBackbone");
    Key ITERATIONS_ALLATOM = new Key("nIterationsAllAtoms");
    //--------------------------------- MCM -------------------------------------
    Key MCM = new Key("MCM");
    Key N_MCM_STEPS = new Key("numberOfMCMsteps");
    Key INITIAL_TEMPERATURE = new Key("initialTemperature");
    Key FINAL_TEMPERATURE = new Key("finalTemperature");

    //--------------------------------- Homology Modeling -------------------------------------
    Key TARGET_NAME = new Key("targetName");
    Key TEMPLATE_NAME = new Key("templatetName");
    Key TARGET_FILE_PATH = new Key("targetFilePath");
    Key ALINMENT_FILE_PATH = new Key("alinmentFilePath");

Key TEMPLATE = new Key("template");

    Key TEMPLATE_FILE_PATH = new Key("templateFilePath");
    Key TEMPLATE_STRUCTURE = new Key("templateStructure");
    Key TEMPLATE_DSSP = new Key("templateDssp");
    Key TEMPLATE_TARGET_ALIGNMENT = new Key("templateTargetAlignment");
    Key OUTPUT_FILE_PATH = new Key("outputFilePath");
    Key OUTPUT_FILE_NAME = new Key("outputFileName");
    Key SS_NAME = new Key("ssName");
    Key LOOSEN_EDGE_LENGTH = new Key("loosenEdgeLength");
    Key NON_FROZEN_BOND_DEPTH = new Key("nonFrozenBondDepth");
    Key NON_FROZEN_RADIUS = new Key("nonFrozenRadius");
    Key NUMBER_OF_MODELS = new Key("numberOfModels");

    //--------------------------------- analysis ---------------------------------
    Key DICTIONARY_KEY = new Key("COMMENT");
    Key MESHILOG_KEY = new Key("MESHILOG");
    Key KEY_KEY = new Key("T1");
    Key VALUE_KEY = new Key("V1");

    //--------------------------------- Sequencea -------------------------------------
    Key AA_SEQUENCE = new Key("aa sequence");
    Key SS_SEQUENCE = new Key("ss sequence");
    Key ACCESIBILITY_SEQUENCE = new Key("accesibility sequence");
    //
    //--------------------------------- Misc -------------------------------------
    Key ON = new Key("on");
    Key OFF = new Key("off");
    Key END = new Key("end");
    Key WEIGHT = new Key("weight");
    Key INPUT_FILE = new Key("inputFile");
    Key CUTOFF = new Key("cutoff");
    Key NONE = new Key("none");
    Key USE_FAST_ARCCOS = new Key("useFastArcCos");
    Key DRESSER_FRAGMENTS = new Key("dresserFragments");
    Key ROTAMER_LIBRARY = new Key("rotamerLibrary");
    Key FIX_N_TERMINAL = new Key("fixNterminal");
    Key FIX_C_TERMINAL = new Key("fixCterminal");
}
