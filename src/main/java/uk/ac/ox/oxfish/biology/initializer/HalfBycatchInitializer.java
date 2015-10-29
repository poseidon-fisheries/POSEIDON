package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.LogisticLocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A diffusing logistic initializer where the bycatch specie only exists on the upper half of the sea.
 * Created by carrknight on 7/30/15.
 */
public class HalfBycatchInitializer implements BiologyInitializer {



    private final  DiffusingLogisticInitializer delegate;


    public HalfBycatchInitializer(DoubleParameter carryingCapacity, DoubleParameter steepness,
                                  double percentageLimitOnDailyMovement,
                                  double differentialPercentageToMove) {
        delegate = new DiffusingLogisticInitializer(carryingCapacity, steepness,
                                                    percentageLimitOnDailyMovement,
                                                    differentialPercentageToMove);
    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     */
    @Override
    public LocalBiology generate(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells) {

        if(seaTile.getAltitude() < 0) {
            LogisticLocalBiology generated = (LogisticLocalBiology) delegate.generate(biology,
                                                                                                            seaTile,
                                                                                                            random,
                                                                                                            mapHeightInCells,
                                                                                                            mapWidthInCells);

            //if you are at the top, make carrying capacity of the second specie = 0
            if (seaTile.getGridY() <= mapHeightInCells / 2)
                generated.setCarryingCapacity(biology.getSpecie(1), 0d);

            return generated;
        }
        else
            return delegate.generate(biology,
                                     seaTile,
                                     random,
                                     mapHeightInCells,
                                     mapWidthInCells);

    }

    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model)
    {

        delegate.processMap(biology, map, random, model);

        int width = map.getHeight();
        int heightLow = map.getHeight() / 2;
        int heightHigh = heightLow+1;


        //create daily data
        //record info:
        DataColumn dailyNorthColumn = model.getDailyDataSet().registerGatherer("# of North Tows", state -> {
            double towsNorth = 0;
            for (int x = 0; x < width; x++)
                for (int y = 0; y < heightLow; y++)
                    towsNorth += map.getDailyTrawlsMap().get(x, y);

            return towsNorth;
        }, Double.NaN);
        DataColumn dailySouthColumn = model.getDailyDataSet().registerGatherer("# of South Tows", state -> {
            double towsSouth = 0;
            for (int x = 0; x < width; x++)
                for (int y = heightHigh; y < map.getHeight(); y++)
                    towsSouth += map.getDailyTrawlsMap().get(x, y);

            return towsSouth;
        }, Double.NaN);


        //create yearly data
        model.getYearlyDataSet().registerGatherer("# of North Tows",
                                                  FishStateUtilities.generateYearlySum(dailyNorthColumn),
                                                  Double.NaN);
        model.getYearlyDataSet().registerGatherer("# of South Tows",
                                                  FishStateUtilities.generateYearlySum(dailySouthColumn),
                                                  Double.NaN);
    }

    @Override
    public int getNumberOfSpecies() {
        return 2;
    }
}
