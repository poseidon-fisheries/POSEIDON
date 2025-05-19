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

package uk.ac.ox.oxfish.fisher.equipment.gear;

public class DecoratorGearPair {


    /**
     * the bottom decorator; can be null if gear is never decorated
     */
    private final GearDecorator deepestDecorator;

    /**
     * decorated gear, cannot be null
     */
    private final Gear decorated;

    public DecoratorGearPair(
        final GearDecorator deepestDecorator,
        final Gear decorated
    ) {
        this.deepestDecorator = deepestDecorator;
        this.decorated = decorated;
    }

    public static DecoratorGearPair getActualGear(final Gear gear) {
        if (gear instanceof GearDecorator) {
            //go deeper
            if (((GearDecorator) gear).getDelegate() instanceof GearDecorator)
                return getActualGear(((GearDecorator) gear).getDelegate());
            else
                return new DecoratorGearPair(
                    (GearDecorator) gear,
                    ((GearDecorator) gear).getDelegate()
                );
        } else
            return new DecoratorGearPair(null, gear);
    }

    public GearDecorator getDeepestDecorator() {
        return deepestDecorator;
    }

    public Gear getDecorated() {
        return decorated;
    }
}
