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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.BiomassCatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.BiomassCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.model.scenario.InputFile;

import java.util.function.Supplier;

public class PurseSeinerBiomassFishingStrategyFactory
    extends PurseSeinerFishingStrategyFactory<BiomassLocalBiology, BiomassFad> {

    public PurseSeinerBiomassFishingStrategyFactory() {
        super(BiomassLocalBiology.class, BiomassFad.class);
    }

    public PurseSeinerBiomassFishingStrategyFactory(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final InputFile actionWeightsFile,
        final BiomassCatchSamplersFactory catchSamplersFactory,
        final SetDurationSamplersFactory setDurationSamplersFactory,
        final InputFile maxCurrentSpeedsFile
    ) {
        super(
            BiomassLocalBiology.class,
            BiomassFad.class,
            speciesCodesSupplier,
            actionWeightsFile,
            catchSamplersFactory,
            setDurationSamplersFactory,
            maxCurrentSpeedsFile
        );
    }

    @Override
    CatchMaker<BiomassLocalBiology> getCatchMaker(final GlobalBiology globalBiology) {
        return new BiomassCatchMaker(globalBiology);
    }
}
