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
public class WhereFadsArePlanningModule extends DiscretizedOwnFadPlanningModule {


    private final double ageWeight;


    public WhereFadsArePlanningModule(OwnFadSetDiscretizedActionGenerator optionsGenerator,
                                double ageWeight) {

        super(optionsGenerator);
        this.ageWeight = ageWeight;


    }


    @Override
    protected PlannedAction chooseFadSet(Plan currentPlanSoFar, Fisher fisher,
                                         FishState model, NauticalMap map,
                                         OwnFadSetDiscretizedActionGenerator optionsGenerator) {

        final int now = model.getStep();

        List<Pair<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer>> options =
                optionsGenerator.peekAllFads();

        //if there are no options, don't bother
        if(options == null || options.isEmpty())
            return null;
        //if there is only one option, also don't bother
        if(options.size()==1)
        {
            if(options.get(0).getSecond()>0)
                return optionsGenerator.chooseFad(options.get(0).getSecond());
            else return null;
        }


        //go through every valid discretized list of fads
        double bestWeight = -1;
        int fadGroupChosen = -1;
        for (Pair<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer> option : options) {
            double weightHere =
                    option.getFirst().stream().mapToDouble(
                            valuedFad -> {
                                //get the age of the fad
                                int age = valuedFad.getFirst().isActive() ?
                                        now - valuedFad.getFirst().getStepDeployed() :
                                        0;
                                //age ^ weight
                                return Math.pow(age,ageWeight);
                            }
                    ).sum();
            if(weightHere>bestWeight){
                bestWeight = weightHere;
                fadGroupChosen = option.getSecond();
            }
        }


        //all fads are empty, don't bother setting on any!
        if(fadGroupChosen<0 ||
                fadGroupChosen >= optionsGenerator.getNumberOfGroups() )
            return null;
        return optionsGenerator.chooseFad(fadGroupChosen);
    }




}
