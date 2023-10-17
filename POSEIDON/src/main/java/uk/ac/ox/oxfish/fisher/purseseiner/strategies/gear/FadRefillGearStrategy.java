/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadRefillGearStrategy implements GearStrategy {

    private final Map<String, Integer> maxFadsPerFisher;
    private final double fadCost;

    FadRefillGearStrategy(final Map<String, Integer> maxFadsPerFisher, final double fadCost) {
        this.maxFadsPerFisher = ImmutableMap.copyOf(maxFadsPerFisher);
        this.fadCost = fadCost;
    }

    @Override
    public void updateGear(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState model,
        final Action currentAction
    ) {
        final FadManager fadManager = getFadManager(fisher);
        // As a temporary measure, hardcode maxFads as the max number of active FADs
        // that a vessel can possibly have and see if it helps with calibration.
        final int maxFads = 450;
//        final String boatId = fisher.getId();
//        final int maxFads = checkNotNull(
//            maxFadsPerFisher.get(boatId),
//            "Max number of FADs not found for fisher %s", boatId
//        );
        // The very first time this method is called, at the start of the
        // simulation, the current trip isn't initialized yet,so we can't
        // charge the vessels for FADs, but that's fine with us since
        // we give them their initial inventory for free anyway.
        if (fisher.getCurrentTrip() != null) {
            final int newFads = maxFads - fadManager.getNumFadsInStock();
            fisher.spendForTrip(newFads * fadCost);
        }
        fadManager.setNumFadsInStock(maxFads);
    }

}
