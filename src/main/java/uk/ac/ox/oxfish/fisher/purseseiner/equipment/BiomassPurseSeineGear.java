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

package uk.ac.ox.oxfish.fisher.purseseiner.equipment;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionField;

public class BiomassPurseSeineGear extends PurseSeineGear<BiomassLocalBiology, BiomassFad> {

    public BiomassPurseSeineGear(
        final FadManager<BiomassLocalBiology, BiomassFad> fadManager,
        final Iterable<AttractionField> attractionFields,
        final double successfulFadSetProbability
    ) {
        super(fadManager, attractionFields, successfulFadSetProbability);
    }

    @Override
    Catch makeCatch(
        final GlobalBiology globalBiology, final LocalBiology caughtBiology
    ) {
        return new Catch(
            globalBiology
                .getSpecies()
                .stream()
                .mapToDouble(caughtBiology::getBiomass)
                .toArray()
        );
    }

    @Override
    public Gear makeCopy() {
        return new BiomassPurseSeineGear(
            getFadManager(),
            ImmutableSet.copyOf(getAttractionFields()),
            getSuccessfulFadSetProbability()
        );
    }
}