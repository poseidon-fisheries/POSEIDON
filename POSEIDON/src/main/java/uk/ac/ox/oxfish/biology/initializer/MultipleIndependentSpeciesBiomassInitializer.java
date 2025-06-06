/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class MultipleIndependentSpeciesBiomassInitializer implements BiologyInitializer {


    private final List<SingleSpeciesBiomassInitializer> initializers;


    private final boolean addImaginarySpecies;


    private final boolean unfishable;

    public MultipleIndependentSpeciesBiomassInitializer(
        final List<SingleSpeciesBiomassInitializer> initializers,
        final boolean addImaginarySpecies,
        final boolean unfishable
    ) {
        this.initializers = initializers;
        this.addImaginarySpecies = addImaginarySpecies;
        this.unfishable = unfishable;
    }

    /**
     * if at least one species can live here, return a localBiomassBiology; else return a empty biology
     */
    @Override
    public LocalBiology generateLocal(
        final GlobalBiology biology,
        final SeaTile seaTile,
        final MersenneTwisterFast random,
        final int mapHeightInCells,
        final int mapWidthInCells,
        final NauticalMap map
    ) {
        assert !initializers.isEmpty();

        LocalBiology toReturn = null;
        for (final SingleSpeciesBiomassInitializer initializer : initializers) {
            final LocalBiology lastgen = initializer.generateLocal(
                biology,
                seaTile,
                random,
                mapHeightInCells,
                mapWidthInCells,
                map
            );
            if (toReturn == null ||
                (toReturn instanceof EmptyLocalBiology && lastgen instanceof BiomassLocalBiology))
                toReturn = lastgen;
        }
        //return one, it doesn't matter which
        assert toReturn != null;
        return toReturn;
    }

    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule
     *                stuff rather
     */
    @Override
    public void processMap(
        final GlobalBiology biology, final NauticalMap map, final MersenneTwisterFast random, final FishState model
    ) {


        final ArrayList<Entry<Species, BiomassMovementRule>> movements = new ArrayList<>(initializers.size());
        for (final SingleSpeciesBiomassInitializer initializer : initializers) {
            //do not let them create their own movement!
            initializer.setForceMovementOff(true);

            initializer.processMap(biology, map, random, model);
            movements.add(
                entry(
                    biology.getSpeciesByCaseInsensitiveName(initializer.getSpeciesName()),
                    initializer.getMovementRule()
                )
            );
        }


        if (unfishable) {
            for (final SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {
                if (!(seaTile.getBiology() instanceof EmptyLocalBiology))
                    seaTile.setBiology(new ConstantBiomassDecorator((BiomassLocalBiology) seaTile.getBiology()));
            }
        }


        //create a single movement now
        @SuppressWarnings({"unchecked", "rawtypes"}) final BiomassDiffuserContainer diffuser =
            new BiomassDiffuserContainer(
                map, random, biology, movements.toArray(new Entry[movements.size()])
            );
        model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);


    }

    /**
     * call global generation for each, grab the species built and move on
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return
     */
    @Override
    public GlobalBiology generateGlobal(final MersenneTwisterFast random, final FishState modelBeingInitialized) {


        final List<Species> species = new ArrayList<>();
        for (final SingleSpeciesBiomassInitializer initializer : initializers) {

            final GlobalBiology individualBiology = initializer.generateGlobal(random, modelBeingInitialized);
            assert individualBiology.getSize() == 1;
            species.add(individualBiology.getSpecie(0));
        }
        if (addImaginarySpecies)
            species.add(new Species(
                "Others",
                StockAssessmentCaliforniaMeristics.FAKE_MERISTICS,
                true
            ));

        return new GlobalBiology(species.toArray(new Species[species.size()]));

    }
}
