package uk.ac.ox.oxfish.gui;

import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Just a simple component to inspect in its own tab
 * Created by carrknight on 7/20/15.
 */
public class RegulationProxy {
    
    
    
    private AlgorithmFactory<? extends  Regulation> globalRegulations = new AnarchyFactory();


    public AlgorithmFactory<? extends Regulation> getGlobalRegulations() {
        return globalRegulations;
    }

    public void setGlobalRegulations(
            AlgorithmFactory<? extends Regulation> globalRegulations) {
        this.globalRegulations = globalRegulations;
    }
}
