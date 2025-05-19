/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.event;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.ExponentialMortalityFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExogenousInstantaneousMortalityCatches extends AbstractExogenousCatches {


    private static final long serialVersionUID = -4344067631722700547L;
    private final HashMap<String, Double> exponentialMortality;


    private final boolean isAbundanceBased;


    public ExogenousInstantaneousMortalityCatches(
        final String dataColumnName,
        final LinkedHashMap<String, Double> exponentialMortality,
        final boolean isAbundanceBased
    ) {
        super(dataColumnName);
        this.exponentialMortality = exponentialMortality;
        this.isAbundanceBased = isAbundanceBased;
    }

    @Override
    public void step(final SimState simState) {
        final FishState model = (FishState) simState;

        final List<? extends LocalBiology> allTiles = getAllCatchableBiologies(model);

        for (final Map.Entry<String, Double> mortality : exponentialMortality.entrySet()) {
            //total landed
            double totalLanded = 0;

            //get the species
            final Species target = model.getSpecies(mortality.getKey());
            Preconditions.checkArgument(target != null, "Couldn't find this species");


            //worry only about tiles that have this fish
            final List<? extends LocalBiology> tiles = allTiles.stream().filter(
                seaTile -> getFishableBiomass(target, seaTile) >
                    FishStateUtilities.EPSILON).collect(Collectors.toList());

            final Double instantMortality = mortality.getValue();
            for (final LocalBiology tile : tiles) {
                if (isAbundanceBased) {
                    totalLanded += abundanceCatch(instantMortality,
                        model, target, tile
                    );
                } else {


                    totalLanded += biomassCatch(model, target, instantMortality, tile);
                }

            }


            super.lastExogenousCatchesMade.put(target, totalLanded);

        }


    }

    private double abundanceCatch(
        final double mortality,
        final FishState fishstate, final Species species, final LocalBiology tile
    ) {
        final HomogeneousAbundanceGear gear = new HomogeneousAbundanceGear(
            0d,
            new ExponentialMortalityFilter(mortality)
        );
        final GlobalBiology biology = fishstate.getBiology();
        final StructuredAbundance[] structuredAbundances = new StructuredAbundance[biology.getSize()];
        for (int i = 0; i < structuredAbundances.length; i++)
            structuredAbundances[i] = new StructuredAbundance(
                biology.getSpecie(i).getNumberOfSubdivisions(),
                biology.getSpecie(i).getNumberOfBins()
            );
        structuredAbundances[species.getIndex()] =
            gear.catchesAsAbundanceForThisSpecies(tile, 1, species);
        final Catch fish = new Catch(
            structuredAbundances,
            biology
        );
        tile.reactToThisAmountOfBiomassBeingFished(fish, fish, biology);
        return fish.getTotalWeight();
    }

    private double biomassCatch(final FishState fishstate, final Species target, final Double instantMortality, final LocalBiology tile) {
        assert tile.getBiomass(target) > FishStateUtilities.EPSILON;
        final double caught = tile.getBiomass(target) * (1 - Math.exp(-instantMortality));
        Catch fish = new Catch(
            target,
            caught,
            fishstate.getBiology()
        );
        //round to be supersafe
        if (fish.totalCatchWeight() > tile.getBiomass(target)) {
            //should be by VERY little!
            assert tile.getBiomass(target) + FishStateUtilities.EPSILON > fish.getTotalWeight();
            //bound it to what is available
            fish = new Catch(target, tile.getBiomass(target), fishstate.getBiology());
            assert (fish.totalCatchWeight() <= tile.getBiomass(target));
        }
        tile.reactToThisAmountOfBiomassBeingFished(fish, fish, fishstate.getBiology());
        return fish.getTotalWeight();
    }

}
