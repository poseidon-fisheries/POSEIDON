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

package uk.ac.ox.oxfish.geography.fads;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * checks each step all fads that pass a predicate; if they do, then lose them
 */
public class FadZapper implements Steppable, AdditionalStartable {

    private static final long serialVersionUID = -6875322207517588809L;
    private final Predicate<? super Fad> validator;

    public FadZapper(final Predicate<? super Fad> validator) {
        this.validator = validator;
    }

    @Override
    public void step(final SimState simState) {
        Optional.ofNullable(((FishState) simState).getFadMap()).ifPresent(fadMap ->
            fadMap.allFads().collect(toList()).stream()
                .filter(validator)
                .forEach(fad -> {
                    fadMap.destroyFad(fad);
                    fad.releaseFishIntoTheVoid(((FishState) simState).getSpecies());
                })
        );
    }

    @Override
    public void start(final FishState model) {
        model.scheduleEveryDay(this, StepOrder.DAWN);
    }
}
