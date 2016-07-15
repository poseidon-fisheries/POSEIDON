package uk.ac.ox.oxfish.fisher.heatmap.acquisition;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
        Stream<SeaTile> tileStream = seaTiles.stream();
        MersenneTwisterFast random = state.getRandom();
        if(proportionSearched<=1d)
            tileStream = tileStream.filter(tile -> random.nextDouble()<=proportionSearched);


        SeaTile possibleBest = tileStream.
                max(
                        (o1, o2) -> Double.compare(
                                regression.predict(o1, state.getHoursSinceStart(), state, fisher),
                                regression.predict(o2, state.getHoursSinceStart(), state, fisher))
                ).orElse(seaTiles.get(random.nextInt(seaTiles.size())));
        if(current==null || current == possibleBest ||
                regression.predict(possibleBest,state.getHoursSinceStart(),state,fisher) >
                regression.predict(current,state.getHoursSinceStart(),state,fisher))
            return possibleBest;
        else
            return current;

    }
}
