/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2022-2025, University of Oxford.
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

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;

public class GenericFadMapFactory extends FadMapFactory {

    public GenericFadMapFactory(
        final CurrentPatternMapSupplier currentPatternMapSupplier
    ) {
        super(LocalBiology.class, currentPatternMapSupplier);
    }

    public GenericFadMapFactory() {
        super(LocalBiology.class);
    }
}
