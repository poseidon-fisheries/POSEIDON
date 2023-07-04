/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments.tuna;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.heatmaps.BiomassHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProviderToOutputPluginAdaptor;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.scenario.EpoPathPlannerAbundanceScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * quick and easy experiment: run the model once, but attach to it a couple of loggers to output what is the geographical
 * distribution of biomass. This can be useful to study actual catchability when compared with real data
 */
public class TunaBiomassSensitivity {


    private final static Path MAIN_DIRECTORY = Paths.get(
        "docs/20220223 tuna_calibration/test_biomass/"
    );

    public static void main(final String[] args) throws IOException {

        ///set up the scenario
        final FishYAML yaml = new FishYAML();
        final EpoPathPlannerAbundanceScenario scenario = yaml.loadAs(
            new FileReader(MAIN_DIRECTORY.resolve("linear.yaml").toFile()),
            EpoPathPlannerAbundanceScenario.class
        );

        final FishState model = new FishState(0);
        model.setScenario(scenario);
        model.start();
        model.registerStartable(
            new RowProviderToOutputPluginAdaptor(
                new BiomassHeatmapGatherer(1, model.getSpecies("Skipjack tuna")),
                "skipjack.csv"
            )
        );
        model.registerStartable(
            new RowProviderToOutputPluginAdaptor(
                new BiomassHeatmapGatherer(1, model.getSpecies("Bigeye tuna")),
                "bigeye.csv"
            )
        );
        model.registerStartable(
            new RowProviderToOutputPluginAdaptor(
                new BiomassHeatmapGatherer(1, model.getSpecies("Yellowfin tuna")),
                "yellowfin.csv"
            )
        );
        model.registerStartable(model1 -> {
            for (final Fisher fisher : model1.getFishers()) {
                fisher.setRegulation(new FishingSeason(true, 0));
            }
        });
        while (model.getStep() <= 365)
            model.schedule.step(model);
//        model.schedule.step(model);
//        model.schedule.step(model);
//        model.schedule.step(model);
//        model.schedule.step(model);
        MAIN_DIRECTORY.resolve("biomass_test_2").toFile().mkdir();
        FishStateUtilities.writeAdditionalOutputsToFolder(
            MAIN_DIRECTORY.resolve("biomass_test_2"),
            model
        );


    }

}
