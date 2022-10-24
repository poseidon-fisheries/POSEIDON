package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.List;
import java.util.PriorityQueue;
import java.util.function.ToDoubleFunction;

/**
 * simply picks the best fad in the area where there are most fads, weighted by the age of the fad:
 *
 * (best area has max SUM(FAD_AGE^AGE_WEIGHT)
 */
public class WhereFadsArePlanningModule extends PickBestPilePlanningModule {


    private final double ageWeight;


    public WhereFadsArePlanningModule(OwnFadSetDiscretizedActionGenerator optionsGenerator,
                                double ageWeight) {

        super(optionsGenerator);
        this.ageWeight = ageWeight;


    }


    @Override
    protected double weighFad(int currentModelStep,
                            OwnFadSetDiscretizedActionGenerator.ValuedFad valuedFad) {
        //get the age of the fad
        int age = valuedFad.getFirst().isActive() ?
                currentModelStep - valuedFad.getFirst().getStepDeployed() :
                0;
        //age ^ weight
        return Math.pow(age, ageWeight);
    }


}
