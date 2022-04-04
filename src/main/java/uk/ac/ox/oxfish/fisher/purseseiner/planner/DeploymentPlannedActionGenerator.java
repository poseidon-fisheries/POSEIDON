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
public class DeploymentPlannedActionGenerator{

    /**
     * here I use Nic's object on location values to use the whole reading toolchain;
     * in practice however all we need here is a mapping coords --> weight
     */
    private final DeploymentLocationValues originalLocationValues;


    private final NauticalMap map;

    /**
     * the rng to use (compatible with Apache)
     */
    private final MTFApache localRng;

    //todo
    //we can avoid ton of waste by not instantiating this every step and only
    //when there is a change in the location value deployment
    //but unfortunately it requires a bit of work with a specialized listener
    private EnumeratedDistribution<SeaTile> seatilePicker;


    public DeploymentPlannedActionGenerator(DeploymentLocationValues originalLocationValues,
                                            NauticalMap map, MersenneTwisterFast random) {
        this.originalLocationValues = originalLocationValues;
        this.map = map;
        localRng = new MTFApache(random);
    }


    private void preparePicker(){
        seatilePicker = new EnumeratedDistribution<>(localRng,
                originalLocationValues.getValues().stream().map(
                        entry -> new Pair<>(
                                map.getSeaTile(entry.getKey()),
                                entry.getValue()
                        )
                ).collect(Collectors.toList()));
    }

    public void start(){
        Preconditions.checkState(originalLocationValues.hasStarted(),"need to start the location values first!");
        preparePicker();
    }

    public PlannedAction.PlannedDeploy drawNewDeployment(){
        Preconditions.checkNotNull(seatilePicker,"Did not start the deploy generator yet!");
        return new PlannedAction.PlannedDeploy(
                seatilePicker.sample()
        );
    }


}
