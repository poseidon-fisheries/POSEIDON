package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.ConstantLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Consumer;

/**
 * Uniformly distributed constant biomass for each cell
 * Created by carrknight on 6/22/15.
 */
public class RandomConstantBiologyInitializer implements BiologyInitializer {


    final private double minBiomass;

    final private double maxBiomass;

    /**
     * how many times we attempt to smooth the biology between two elements
     */
    private int biologySmoothingIndex;


    public RandomConstantBiologyInitializer(double minBiomass, double maxBiomass, int biologySmoothingIndex) {
        this.minBiomass = minBiomass;
        this.maxBiomass = maxBiomass;
        this.biologySmoothingIndex = biologySmoothingIndex;
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
            int mapWidthInCells)
    {
        assert minBiomass > maxBiomass;

            if (seaTile.getAltitude() > 0)
                return new EmptyLocalBiology();
            else
                return new ConstantLocalBiology(random.nextDouble()* (maxBiomass - minBiomass)+ minBiomass);
        }

    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *  @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random the randomizer
     * @param model
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model)
    {
        //standard neighboring tiles smoother
        //a bad copy of the NETLOGO prototype
        final Consumer<NauticalMap> smoother =
                NauticalMapFactory.smoothConstantBiology(
                        biologySmoothingIndex, map.getWidth(), map.getHeight()).apply(random);

        smoother.accept(map);
    }


    public double getMinBiomass() {
        return minBiomass;
    }

    public double getMaxBiomass() {
        return maxBiomass;
    }

    /**
     * Singleton array with "species 0" in it
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        return new String[]{"Species 0"};
    }

}
