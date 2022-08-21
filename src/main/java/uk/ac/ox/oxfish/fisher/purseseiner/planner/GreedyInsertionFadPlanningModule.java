package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;


/**
 * picks the FAD (map discretized) that maximizes immediate
 * gain (i.e. revenue - cost of getting there and back).
 *
 * In order to make the planning more "long term", you can pick greedily by computing the revenue of
 * multiple fads from that centroid. That way you pick somewhere you are likely going to pick more fads
 *
 */
public class GreedyInsertionFadPlanningModule extends DiscretizedOwnFadPlanningModule {

    private final int additionalFadInspected;


    public GreedyInsertionFadPlanningModule(MapDiscretization discretization,
                                            double minimumValueOfFadBeforeBeingPickedUp,
                                            int additionalFadInspected) {
        super(discretization, minimumValueOfFadBeforeBeingPickedUp);
        this.additionalFadInspected = additionalFadInspected;
    }

    public GreedyInsertionFadPlanningModule(OwnFadSetDiscretizedActionGenerator optionsGenerator, int additionalFadInspected) {
        super(optionsGenerator);
        optionsGenerator.setFilterOutCurrentlyInvalidFads(true);
        this.additionalFadInspected = additionalFadInspected;
    }

    @Override
    protected PlannedAction chooseFadSet(Plan currentPlanSoFar, Fisher fisher, FishState model, NauticalMap map, OwnFadSetDiscretizedActionGenerator optionsGenerator) {
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
        double maxProfitsSoFar = 0;
        int fadGroupChosen = - 1;

        for (Pair<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer> option : options) {
            //get the fads in centroid
            PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad> fads = option.getFirst();
            //if there are none, don't bother
            if(fads.isEmpty() || fads == null)
                continue;
            //check the cost to go to the "best" fad
            Iterator<OwnFadSetDiscretizedActionGenerator.ValuedFad> iterator = fads.iterator();
            OwnFadSetDiscretizedActionGenerator.ValuedFad bestFad = iterator.next();
            PlannedAction.FadSet potentialAction = new PlannedAction.FadSet(bestFad.getFirst());
            double additionalHoursTravelled = DrawThenCheapestInsertionPlanner.cheapestInsert(
                    currentPlanSoFar,
                    potentialAction,
                    Integer.MAX_VALUE, //don't want to censor time now
                    speedInKmPerHours,
                    map,
                    false
            );
            double costHere =  fisher.getExpectedAdditionalCosts(additionalHoursTravelled,
                    potentialAction.hoursItTake(),
                    additionalHoursTravelled/speedInKmPerHours);

            //compute the revenues for harvesting the first X fads
            // (the first we will actually do, the others are there to trick us into setting in more promising areas)
            double revenuesHere = bestFad.getSecond();
            int additionalFadsInspected = 0;
            while(additionalFadsInspected< additionalFadInspected && iterator.hasNext())
            {
                revenuesHere += iterator.next().getSecond();
                additionalFadsInspected++;
            }

            double profitHere = revenuesHere - costHere;
            if(profitHere>maxProfitsSoFar){
                maxProfitsSoFar = profitHere;
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
