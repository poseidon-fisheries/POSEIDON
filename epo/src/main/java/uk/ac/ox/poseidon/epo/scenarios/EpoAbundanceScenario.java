/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.epo.scenarios;

import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceProcessesFactory;
import uk.ac.ox.oxfish.biology.tuna.RecruitmentProcessesFactory;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadMapFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoAbundanceScenario extends EpoScenario<AbundanceLocalBiology> {

    public EpoAbundanceScenario() {
        final InputPath abundanceInputFolder = getInputFolder().path("abundance");
        setBiologicalProcesses(
            AbundanceProcessesFactory.create(
                abundanceInputFolder,
                new SpeciesCodesFromFileFactory(
                    getInputFolder().path("species_codes.csv")
                ),
                getMapExtentFactory(),
                new RecruitmentProcessesFactory(
                    abundanceInputFolder.path("recruitment_parameters_2022.csv")
                )
            )
        );
        setFadMap(new AbundanceFadMapFactory(getCurrentPatternMapSupplier()));
    }

}
