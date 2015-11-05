package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * From left to right, two species, one at the top, one at the bottom
 * Created by carrknight on 9/24/15.
 */
public class FromLeftToRightSplitInitializer implements BiologyInitializer {


    /**
     * leftmost biomass
     */
    private double maximumBiomass;

    /**
     * how many times we attempt to smooth the biology between two elements
     */
    private int biologySmoothingIndex;


    public FromLeftToRightSplitInitializer(double maximumBiomass, int biologySmoothingIndex) {
        this.maximumBiomass = maximumBiomass;
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
        if (seaTile.getAltitude() > 0)
            return new EmptyLocalBiology();
        else
        if(seaTile.getGridY() < mapHeightInCells/2d)
            return new ConstantHeterogeneousLocalBiology(maximumBiomass*
                                                    Math.pow((1-seaTile.getGridX()/(double)mapWidthInCells)
                                                            ,2),0);
        else
            return new ConstantHeterogeneousLocalBiology(0,maximumBiomass*
                                                                 Math.pow((1-seaTile.getGridX()/(double)mapWidthInCells)
                                                                         ,2));
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
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {
        List<Species> species = biology.getSpecies();

        ObjectGrid2D baseGrid = (ObjectGrid2D) map.getRasterBathymetry().getGrid();
        for (int i = 0; i < biologySmoothingIndex; i++) {
            int width = map.getWidth();
            int x = random.nextInt(width);
            int height = map.getHeight();
            int y = random.nextInt(height);
            SeaTile toChange = (SeaTile) baseGrid.get(x, y);
            if (toChange.getAltitude() > 0) //land is cool man
            {
                assert toChange.getBiomass(null) <= 0;
                continue;
            }
            x += random.nextInt(3) - 1;
            x = Math.max(0, x);
            x = Math.min(x, width - 1);
            y += random.nextInt(3) - 1;
            y = Math.max(0, y);
            y = Math.min(y, height - 1);
            SeaTile fixed = (SeaTile) baseGrid.get(x, y);
            int specie = random.nextInt(2);
            if (toChange.getBiomass(species.get(specie)) > 0 && fixed.getBiomass(species.get(specie)) > 0) {

                double newBiology = Math.round(toChange.getBiomass(species.get(specie)) +
                                                       (random.nextFloat() * .025f) *
                                                               (fixed.getBiomass(
                                                                       species.get(specie)) - toChange.getBiomass(
                                                                       species.get(specie))));
                if (newBiology <= 0)
                    newBiology = 1;

                double[] biomass = new double[2];
                biomass[specie] = newBiology;
                //put the new one in!
                toChange.setBiology(new ConstantHeterogeneousLocalBiology(biomass));
            }

        }
    }

    public double getMaximumBiomass() {
        return maximumBiomass;
    }

    public void setMaximumBiomass(double maximumBiomass) {
        this.maximumBiomass = maximumBiomass;
    }

    public int getBiologySmoothingIndex() {
        return biologySmoothingIndex;
    }

    public void setBiologySmoothingIndex(int biologySmoothingIndex) {
        this.biologySmoothingIndex = biologySmoothingIndex;
    }
    /**
     * "Species 0" and "Species 1"
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        return new String[]{"Species 0", "Species 1"};
    }

}


