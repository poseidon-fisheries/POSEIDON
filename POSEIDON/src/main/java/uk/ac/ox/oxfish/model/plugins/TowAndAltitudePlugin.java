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

package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.initializers.TowAndAltitudeOutputInitializer;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;

import javax.annotation.Nullable;

/**
 * just like the TowAndAltitudeOutputInitializer except that unlike logbook interface that waits for you to feed it
 * agents, this one tracks all agents when start is called!
 */
public class TowAndAltitudePlugin implements AdditionalStartable {


    private final TowAndAltitudeOutputInitializer delegate;

    private final String tag;


    public TowAndAltitudePlugin(
        final int histogrammerStartYear, final String identifier,
        @Nullable final
        String tag
    ) {

        this.tag = tag;
        this.delegate = new TowAndAltitudeOutputInitializer(histogrammerStartYear, identifier);
    }


    @Override
    public void start(final FishState model) {
        delegate.start(model);
        for (final Fisher fisher : model.getFishers()) {
            if (tag == null || fisher.getTagsList().contains(tag)) {

                delegate.add(fisher, model);

            }
        }
    }

    @Override
    public void turnOff() {

        delegate.turnOff();
    }
}
