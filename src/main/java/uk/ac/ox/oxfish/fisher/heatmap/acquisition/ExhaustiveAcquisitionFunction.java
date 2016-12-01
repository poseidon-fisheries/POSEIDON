package uk.ac.ox.oxfish.fisher.heatmap.acquisition;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Collections;
import java.util.List;

/**
 * Goes through all the possible seatiles and picks the highest one
 * Created by carrknight on 6/28/16.
 */
public class ExhaustiveAcquisitionFunction  implements AcquisitionFunction
{

    private double proportionSearched;

    public ExhaustiveAcquisitionFunction() {
        this(1d);
    }


    public ExhaustiveAcquisitionFunction(double proportionSearched) {
        this.proportionSearched = proportionSearched;
    }

    /**
     * Goes through all the possible seatiles and picks the highest one
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

        List<SeaTile> seaTiles = map.getAllSeaTilesExcludingLandAsList();
        Collections.shuffle(seaTiles);
        MersenneTwisterFast random = state.getRandom();

        Pair<SeaTile,Double> best;
        if(current!=null)
            best = new Pair<>(current, regression.predict(current, state.getHoursSinceStart(), fisher,state ));
        else
            best = new Pair<>(null,-Double.MAX_VALUE);
        assert Double.isFinite(best.getSecond());
        for(SeaTile tile : seaTiles)
        {
            if(random.nextBoolean(proportionSearched))
            {
                double predicted = regression.predict(tile, state.getHoursSinceStart(), fisher,state );
                if(Double.isFinite(predicted) && predicted > best.getSecond())
                    best=new Pair<>(tile,predicted);
            }
        }



       return best.getFirst();

    }
}
