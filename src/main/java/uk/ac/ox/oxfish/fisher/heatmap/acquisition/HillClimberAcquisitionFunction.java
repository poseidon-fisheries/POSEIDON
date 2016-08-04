package uk.ac.ox.oxfish.fisher.heatmap.acquisition;

import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * hill climbs at one random direction until it can't climb further and pick that as its spot
 * Created by carrknight on 6/28/16.
 */
public class HillClimberAcquisitionFunction implements  AcquisitionFunction
{


    private final int stepSize;


    public HillClimberAcquisitionFunction(int stepSize) {
        this.stepSize = stepSize;
    }

    /**
     * The acquisition function main task: to pick a tile from the map given geographical regression
     * @param map        the map to pick from
     * @param regression the geographical regression
     * @param state  @return a choice
     * @param fisher
     * @param current
     */
    @Override
    public SeaTile pick(
            NauticalMap map, GeographicalRegression regression,
            FishState state, Fisher fisher, SeaTile current) {

        double time = state.getHoursSinceStart();

        //start at a random location
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        //start at current best if you have it
        SeaTile location = current == null ?tiles.get(state.getRandom().nextInt(tiles.size())): current;
        Bag mooreNeighbors =  new Bag(map.getMooreNeighbors(location, stepSize)) ;
        mooreNeighbors.shuffle(state.getRandom());
        Set<SeaTile> checkedAlready = new HashSet<>();
        //as long as there are neighbors you aren't done
        while(!mooreNeighbors.isEmpty())
        {
            //remove a neighbor
            SeaTile option = (SeaTile) mooreNeighbors.remove(0);
            //if it is better, restart search at that neighbor!
            if(option.getAltitude()<0 && !checkedAlready.contains(option) &&
                    regression.predict(location, time, state,fisher )
                            < regression.predict(option, time, state,fisher )) {
                location = option;
                mooreNeighbors = new Bag(map.getMooreNeighbors(location, stepSize));
                mooreNeighbors.shuffle(state.getRandom());
            }
            checkedAlready.add(option);
        }

        return location;
    }

    /**
     * Getter for property 'stepSize'.
     *
     * @return Value for property 'stepSize'.
     */
    public int getStepSize() {
        return stepSize;
    }
}
