package meshi.applications.prediction;
import meshi.energy.TotalEnergy;
import meshi.energy.distanceConstrains.TemplateDistanceConstrainsCreator;
import meshi.molecularElements.Protein;
import meshi.optimizers.*;
import meshi.util.CommandList;
import meshi.util.Key;

public abstract class PredictionMinimizationLoop extends MinimizationLoop {
    public PredictionMinimizationLoop(Protein protein,
			    TemplateDistanceConstrainsCreator distanceConstrainsCreator,
			    CommandList commands , Key iterationType) {
	super(protein, distanceConstrainsCreator, commands , iterationType);
    }

    public Minimizer getMinimizer(TotalEnergy energy, CommandList commands){
	return new LBFGS(energy, commands);
    }
    public void run() throws MinimizerException, LineSearchException {
	super.run();
    }
}
    
	    
