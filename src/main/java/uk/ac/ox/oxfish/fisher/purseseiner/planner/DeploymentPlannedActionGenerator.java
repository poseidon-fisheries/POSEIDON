package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.MTFApache;

import java.util.stream.Collectors;

/**
 * this object exists to generate a new deployment action (probably to add to a plan)
 */
public class DeploymentPlannedActionGenerator extends DrawFromLocationValuePlannedActionGenerator<PlannedAction.Deploy>{


    /**
     * the time it takes for the boat to "recover" after a deployment; 0 means you can drop another immediately
     */
    private final double delayInHoursAfterADeployment;


    public DeploymentPlannedActionGenerator(DeploymentLocationValues originalLocationValues,
                                            NauticalMap map, MersenneTwisterFast random) {
       this(originalLocationValues,map,random,0);
    }

    public DeploymentPlannedActionGenerator(DeploymentLocationValues originalLocationValues,
                                            NauticalMap map, MersenneTwisterFast random,
                                            double delayInHoursAfterADeployment) {
        super(originalLocationValues,map,random);
        this.delayInHoursAfterADeployment = delayInHoursAfterADeployment;

    }


    public PlannedAction.Deploy drawNewPlannedAction(){
        Preconditions.checkState(isReady(),"Did not start the deploy generator yet!");
        return new PlannedAction.Deploy(
                drawNewLocation(),
                delayInHoursAfterADeployment
        );
    }



}
