/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.biology.growers;

import com.google.common.collect.ImmutableList;
import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;

/**
 * This is a SchaeferLogisticGrower that:
 * - takes the biomass aggregated under the FADs into account as part of the total biomass.
 * - adds the biomass lost from FADs drifting out of the map or loosing fish over non-habitable
 * tiles back into the current biomass as part of the recruitment function.
 */
public class FadAwareCommonLogisticGrower extends SchaeferLogisticGrower {

    private final ImmutableList<BiomassLocalBiology> seaTileBiologies;
    private FishState model;

    FadAwareCommonLogisticGrower(
        double malthusianParameter,
        Species species,
        double distributionalWeight,
        ImmutableList<BiomassLocalBiology> seaTileBiologies
    ) {
        super(malthusianParameter, species, distributionalWeight);
        this.seaTileBiologies = seaTileBiologies;
        super.getBiologies().addAll(seaTileBiologies);
    }

    /**
     * Calls the normal recruitment function, but add the biomass lost by FADs to the total biomass recruited.
     */
    @SuppressWarnings("UnstableApiUsage")
    @Override protected double recruit(double current, double capacity, double malthusianParameter) {
        final double biomassLost = model.getFishers()
            .stream()
            .map(FadManagerUtils::getFadManager)
            .flatMap(fadManager -> stream(
                fadManager.getBiomassLostMonitor().flatMap(monitor -> monitor.getSubMonitor(species))
            ))
            .mapToDouble(monitor -> monitor.getAccumulator().get())
            .sum();
        return biomassLost + super.recruit(current, capacity, malthusianParameter);
    }

    @Override public void step(SimState simState) {
        model = (FishState) simState;
        final List<BiomassLocalBiology> biologies = Stream.concat(
            seaTileBiologies.stream(),
            model.getFadMap().allFads().map(Fad::getBiology)
        ).collect(toList());
        grow(model, biologies, seaTileBiologies);
    }

}
