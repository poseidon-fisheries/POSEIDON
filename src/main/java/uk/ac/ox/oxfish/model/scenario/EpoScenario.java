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

package uk.ac.ox.oxfish.model.scenario;

import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.model.regs.factory.SpecificProtectedAreaFromShapeFileFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public abstract class EpoScenario implements Scenario {

    public static final int TARGET_YEAR = 2017;
    public static final Path INPUT_PATH = Paths.get("inputs", "epo");
    public static final SpeciesCodesFromFileFactory speciesCodesSupplier =
        new SpeciesCodesFromFileFactory(INPUT_PATH.resolve("species_codes.csv"));

    static final ImmutableMap<CurrentPattern, Path> currentFiles =
        new ImmutableMap.Builder<CurrentPattern, Path>()
            //.put(Y2015, input("currents_2015.csv"))
            //.put(Y2016, input("currents_2016.csv"))
            .put(Y2017, INPUT_PATH.resolve("currents").resolve("currents_2017.csv"))
            //.put(Y2018, input("currents_2018.csv"))
            //.put(NEUTRAL, input("currents_neutral.csv"))
            //.put(EL_NINO, input("currents_el_nino.csv"))
            //.put(LA_NINA, input("currents_la_nina.csv"))
            .build();

}
