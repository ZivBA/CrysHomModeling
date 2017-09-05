package meshi.parameters;
public interface MeshiPotential {
    String HYDROGEN_BONDS_PAIRS_PARAMETERS_SURFACE = "meshiPotential/HydrogenBondsParameters.surface.dat";
    String HYDROGEN_BONDS_PAIRS_BETA_PARAMETERS = "meshiPotential/HydrogenBondsBetaParameters.dat";
    String HYDROGEN_BONDS_PAIRS_HELIX_PARAMETERS = "meshiPotential/HydrogenBondsHelixParameters.dat";
    String BOND_PARAMETERS = "meshiPotential/bondEnergyParameters.dat";
    String ANGLE_PARAMETERS = "meshiPotential/angleEnergyParameters.dat";
    String PLANE_PARAMETERS = "meshiPotential/planeEnergyParameters.dat";
    String OUT_OF_PLANE_PARAMETERS = "meshiPotential/outOfPlaneEnergyParameters.dat";
    String LENNARD_JONES_PARAMETERS = "meshiPotential/LennardJonesParameters.dat";
    String LENNARD_JONES_PARAMETERS_CA = "meshiPotential/LennardJonesParametersCa.dat";
    String LENNARD_JONES_PARAMETERS_BACKBONE = "meshiPotential/LennardJonesParametersBackbone.dat";
    String CONTACTS_PARAMETERS = "meshiPotential/contactsParameters.dat";
    String LJ_ENVIRONMENT_PARAMETERS = "meshiPotential/LJEnvironment.dat";
    String LJ_ENVIRONMENT_PARAMETERS_CA = "meshiPotential/LJEnvironmentCa.dat";
    String LJ_ENVIRONMENT_PARAMETERS_BACKBONE = "meshiPotential/LJEnvironmentBackbone.dat";
    String CONTACTS_ENVIRONMENT_PARAMETERS = "meshiPotential/contactsEnvironment.dat";
    String EXCLUDED_VOL_PARAMETERS = "meshiPotential/ExcludedVolumeParameters.dat";
    String EXCLUDED_VOL_PARAMETERS_IMPROVED = "meshiPotential/improvedEVparameters.dat";
    String ELECTROSTATICS_PARAMETERS = "meshiPotential/ElectrostaticParameters.dat";
    String ROT1_SOLVATE_PARAMETERS = "meshiPotential/Rot1SolvateCoarse";
    String ROT1_PAIRWISE_PARAMETERS = "meshiPotential/Rot1SolvateCoarse/Pairwise_Rot1.dat";
    String CB_PAIRWISE_PARAMETERS = "meshiPotential/Rot1SolvateCoarse/Pairwise_CB.dat";
    String[] TWO_TORSIONS_PARAMETERS = {/*"meshiPotential/parametersPhiChiCoil.dat",
						     "meshiPotential/parametersPhiChiHelix.dat",
						     "meshiPotential/parametersPhiChiStrand.dat",

						     "meshiPotential/parametersPsiChiCoil.dat",
						     "meshiPotential/parametersPsiChiHelix.dat",
						     "meshiPotential/parametersPsiChiStrand.dat",


						     "meshiPotential/parametersCHI1CHI2.dat",
						     "meshiPotential/parametersCHI2CHI3.dat",
						     "meshiPotential/parametersCHI3CHI4.dat",  */

						     "meshiPotential/parametersPhiPsiCoil.dat",
						     "meshiPotential/parametersPhiPsiHelix.dat",
						     "meshiPotential/parametersPhiPsiStrand.dat"};
								  
    String[] FLAT_RAMACH_PARAMETERS = {//"meshiPotential/flatRamachCoil.dat",
						    "meshiPotential/flatRamachHelix.dat",
						    "meshiPotential/flatRamachStrand.dat"};
						     
    String[] PROPENSITY_TORSION_PARAMETERS = {"meshiPotential/parametersPropensity.dat"};

    String[] PROPENSITY_ANGLE_PARAMETERS = {"meshiPotential/parametersPropensityAngle.dat"};
    
    String ALPHA_ANGLE_PARAMETERS = "meshiPotential/alphaRangeParameters.dat";

    String ALPHA_TORSION_PARAMETERS = "meshiPotential/alphaTorsionRangeParameters.dat";

    String HELIX = "HELIX"; //Effects both TwoTiorsions and HydrogenBondsPairs
    String SHEET = "SHEET"; //Effects both TwoTiorsions and HydrogenBondsPairs
    String COIL = "COIL";  //Effects both TwoTiorsions and HydrogenBondsPairs
    String HELIX_OR_COIL = "HELIX_OR_COIL"; // Effects only HydrogenBondsPairs
    String SHEET_OR_COIL = "SHEET_OR_COIL"; // Effects only HydrogenBondsPairs
    
    String ACCESSIBLE = "ACCESSIBLE";
    String BURIED = "BURIED";



    String[] SOLVATE_WIDE_ATTRACTION_PARAMETERS = {"meshiPotential/SolvateMESHI2Tsai.dat",
						"meshiPotential/SolvateExtResParameters.dat",
						"meshiPotential/SolvateExtResCend.dat",
						"meshiPotential/SolvateExtResCp1.dat",
						"meshiPotential/SolvateExtResCp2.dat",
						"meshiPotential/SolvateExtResCvalAtp1.dat",
						"meshiPotential/SolvateExtResCvalAtp2.dat",
						"meshiPotential/SolvateWideAttHBend.dat",
						"meshiPotential/SolvateWideAttHBp1.dat",
						"meshiPotential/SolvateWideAttHBp2.dat",
						"meshiPotential/SolvateWideAttHBvalAtp1.dat",
						"meshiPotential/SolvateWideAttHBvalAtp2.dat"};


    String[] SOLVATE_NOHB_PARAMETERS = {"meshiPotential/SolvateMESHI2Tsai.dat",
    "meshiPotential/SolvateNoHydrogenBondParameters.dat",
	"meshiPotential/SolvateCend.dat",
	"meshiPotential/SolvateCp1.dat",
	"meshiPotential/SolvateCp2.dat",
	"meshiPotential/SolvateCvalAtp1.dat",
	"meshiPotential/SolvateCvalAtp2.dat"};

    /* parameters file name for the compositeTorsionsEnergy term */
    String COMPOSITE_TORSIONS_PARAMETERS = "meshiPotential/CompositeTorsionsParameters.dat";
    String COMPOSITE_PROPENSITY_2D_PARAMETERS = "meshiPotential/CompositePropensity2D_withPP.dat";
    String COMPOSITE_PROPENSITY_2D_WITH_PP_PARAMETERS = "meshiPotential/CompositePropensity2D_withPP.dat";
    String[] COMPOSITE_PROPENSITY_3D_PARAMETERS = {"meshiPotential/CompositePropensity2D.dat",
    														"meshiPotential/CompositePropensityParameters.dat"};


    String[] SOLVATE_PARAMETERS = {"meshiPotential/SolvateCend.dat",
						"meshiPotential/SolvateCp1.dat",
						"meshiPotential/SolvateCp2.dat",
						"meshiPotential/SolvateCvalAtp1.dat",
						"meshiPotential/SolvateCvalAtp2.dat",
						"meshiPotential/SolvateRegularHBend.dat",
						"meshiPotential/SolvateRegularHBp1.dat",
						"meshiPotential/SolvateRegularHBp2.dat",
						"meshiPotential/SolvateRegularHBvalAtp1.dat",
						"meshiPotential/SolvateRegularHBvalAtp2.dat",
						"meshiPotential/SolvateMESHI2Tsai.dat",
						"meshiPotential/SolvatePolarSideChainSplines.dat",
						"meshiPotential/SolvateCarbonSideChainSplines.dat",
						"meshiPotential/SolvatePolarBackboneSplines.dat"};

    String[] SOLVATE_PARAMETERS_ALT2 = {"meshiPotential/SolvateCend.dat",
						"meshiPotential/SolvateCp1.dat",
						"meshiPotential/SolvateCp2.dat",
						"meshiPotential/SolvateCvalAtp1.dat",
						"meshiPotential/SolvateCvalAtp2.dat",
						"meshiPotential/SolvateRegularHBend.dat",
						"meshiPotential/SolvateRegularHBp1.dat",
						"meshiPotential/SolvateRegularHBp2.dat",
						"meshiPotential/SolvateRegularHBvalAtp1.dat",
						"meshiPotential/SolvateRegularHBvalAtp2.dat",
						"meshiPotential/SolvateMESHI2Tsai.dat",
                        "meshiPotential/SolvatePolarSideChainSplines_alt2.dat",
                        "meshiPotential/SolvateCarbonSideChainSplines.dat",
                        "meshiPotential/SolvatePolarBackboneSplines_alt2.dat"};

    String[] SOLVATE_LONG_HB_PARAMETERS = {"meshiPotential/SolvateCend.dat",
						"meshiPotential/SolvateCp1.dat",
						"meshiPotential/SolvateCp2.dat",
						"meshiPotential/SolvateCvalAtp1.dat",
						"meshiPotential/SolvateCvalAtp2.dat",
						"meshiPotential/SolvateLongHBend.dat",
						"meshiPotential/SolvateLongHBp1.dat",
						"meshiPotential/SolvateLongHBp2.dat",
						"meshiPotential/SolvateLongHBvalAtp1.dat",
						"meshiPotential/SolvateLongHBvalAtp2.dat",
						"meshiPotential/SolvateMESHI2Tsai.dat",
						"meshiPotential/SolvatePolarSideChainSplines.dat",
						"meshiPotential/SolvateCarbonSideChainSplines.dat",
						"meshiPotential/SolvatePolarBackboneSplines.dat"};

    String[] SOLVATE_MINIMIZE_HB_PARAMETERS = {"meshiPotential/SolvateCend.dat",
						"meshiPotential/SolvateCp1.dat",
						"meshiPotential/SolvateCp2.dat",
						"meshiPotential/SolvateCvalAtp1.dat",
						"meshiPotential/SolvateCvalAtp2.dat",
						"meshiPotential/SolvateForMinimizeHBend.dat",
						"meshiPotential/SolvateForMinimizeHBp1.dat",
						"meshiPotential/SolvateForMinimizeHBp2.dat",
						"meshiPotential/SolvateForMinimizeHBvalAtp1.dat",
						"meshiPotential/SolvateForMinimizeHBvalAtp2.dat",
						"meshiPotential/SolvateMESHI2Tsai.dat",
						"meshiPotential/SolvatePolarSideChainSplines.dat",
						"meshiPotential/SolvateCarbonSideChainSplines.dat",
						"meshiPotential/SolvatePolarBackboneSplines.dat"};
    
    
    String[] NEW_SOLVATE_PARAMETERS = {"meshiPotential/SolvateCend.dat",
    		"meshiPotential/SolvateCp1.dat",
    		"meshiPotential/SolvateCp2.dat",
    		"meshiPotential/SolvateCvalAtp1.dat",
    		"meshiPotential/SolvateCvalAtp2.dat",
    		"meshiPotential/SolvateRegularHBend.dat",
    		"meshiPotential/SolvateRegularHBp1.dat",
    		"meshiPotential/SolvateRegularHBp2.dat",
    		"meshiPotential/SolvateRegularHBvalAtp1.dat",
    		"meshiPotential/SolvateRegularHBvalAtp2.dat",
    		"meshiPotential/SolvateMESHI2Tsai.dat",
    		"meshiPotential/NewSolvatePolarSideChainSplines_30_4_2008_CNC_5.3.dat",
    		"meshiPotential/NewSolvateCarbonSideChainSplines_30_4_2008_CNC_5.3.dat",
    		"meshiPotential/NewSolvatePolarBackboneSplines_30_4_2008_CNC_5.3.dat"};
    
}
