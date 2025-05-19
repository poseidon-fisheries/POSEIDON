/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by carrknight on 4/7/17.
 */
public class ClimateChange {


    private static final Path[] anarchyFiles = new Path[]
        {
            Paths.get("docs", "20170407 climate", "base", "base.yaml"),
            Paths.get("docs", "20170407 climate", "climate", "climate.yaml")
        };

    public static void main(final String[] args) throws FileNotFoundException {


        //anarchy files
        for (final Path file : anarchyFiles) {
            Logger.getGlobal().info("Starting " + file.getFileName());
            final FishYAML yaml = new FishYAML();
            final PrototypeScenario scenario = yaml.loadAs(
                new FileReader(file.toFile()),
                PrototypeScenario.class
            );
            scenario.setRegulation(new AnarchyFactory());
            final FishState state = new FishState(0L);
            state.setScenario(scenario);
            state.start();
            while (state.getYear() < 20)
                state.schedule.step(state);

            FishStateUtilities.printCSVColumnsToFile(
                file.getParent().resolve(
                    file.getName(file.getNameCount() - 1).toString().split("\\.")[0] +
                        "_anarchy.csv").toFile(),
                state.getYearlyDataSet().getColumn("Biomass Species 0"),
                state.getYearlyDataSet().getColumn("Species 0 Landings"),
                state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                state.getYearlyDataSet().getColumn("Total Effort")
            );

        }
    }


}
