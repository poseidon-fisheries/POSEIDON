package uk.ac.ox.oxfish.fisher.heatmap.acquisition;

import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

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
     *  @param map        the map to pick from
     * @param regression the geographical regression
     * @param state  @return a choice
     */
    @Override
    public SeaTile pick(
            NauticalMap map, GeographicalRegression regression,
            FishState state) {

        double time = state.getHoursSinceStart();

        //start at a random location
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        SeaTile location = tiles.get(state.getRandom().nextInt(tiles.size()));
        Bag mooreNeighbors =new Bag(map.getMooreNeighbors(location, stepSize));
        mooreNeighbors.shuffle(state.getRandom());

        //as long as there are neighbors you aren't done
        while(!mooreNeighbors.isEmpty())
        {
            //remove a neighbor
            SeaTile option = (SeaTile) mooreNeighbors.remove(0);
            //if it is better, restart search at that neighbor!
            if(option.getAltitude()<0 &&
                    regression.predict(location, time, state ) < regression.predict(option, time, state )) {
                location = option;
                mooreNeighbors = new Bag(map.getMooreNeighbors(location, stepSize));
                mooreNeighbors.shuffle(state.getRandom());
            }
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
