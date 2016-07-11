package uk.ac.ox.oxfish.fisher.heatmap.regression;

import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.HabitatRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.PortDifferenceRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RandomRegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.SpaceRegressionDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;

/**
 * 5 parameters Kernel transduction
 * Created by carrknight on 7/8/16.
 */
public class DefaultKernelTransduction extends KernelTransduction{


    final SpaceRegressionDistance space;
    final PortDifferenceRegressionDistance port;
    final HabitatRegressionDistance habitat;
    final RandomRegressionDistance random;
    final double forgettingFactor;



    public DefaultKernelTransduction(
            NauticalMap map, double forgettingFactor,
            SpaceRegressionDistance space,
            PortDifferenceRegressionDistance port,
            HabitatRegressionDistance habitat,
            RandomRegressionDistance random) {
        super(map, forgettingFactor, space,port,habitat,random);
        this.space = space;
        this.port = port;
        this.habitat = habitat;
        this.random = random;
        this.forgettingFactor = forgettingFactor;
    }


    public SpaceRegressionDistance getSpace() {
        return space;
    }

    public PortDifferenceRegressionDistance getPort() {
        return port;
    }

    public HabitatRegressionDistance getHabitat() {
        return habitat;
    }

    public RandomRegressionDistance getRandom() {
        return random;
    }

    public double getForgettingFactor() {
        return forgettingFactor;
    }
}
