package uk.ac.ox.oxfish.biology;

import fr.ird.osmose.OsmoseSimulation;
import uk.ac.ox.oxfish.geography.osmose.OsmoseStepper;

/**
 * Global Biology with additionally a link to the OSMOSE simulation
 * Created by carrknight on 11/5/15.
 */
public class OsmoseGlobalBiology extends GlobalBiology {

    private final OsmoseSimulation simulation;

    private final OsmoseStepper stepper;


    public OsmoseGlobalBiology(OsmoseSimulation simulation,
                               OsmoseStepper stepper,
                               Species... species) {
        super(species);
        this.simulation = simulation;
        this.stepper = stepper;
    }


    public OsmoseSimulation getSimulation()
    {
        return simulation;
    }

    public OsmoseStepper getStepper() {
        return stepper;
    }
}
