/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by carrknight on 5/23/17.
 */
public class KitchenSinkHalfComparison {


    public static final long SEED = 0L;
    public static final int NUMBER_OF_RUNS = 100;
    private final static Map<String, Path> scenarios = new HashMap<>();
    private final static Path outputFolder =
        Paths.get("docs",
            "20170511 optimisation_remake",
            "kitchensink", "half", "best", "comparison"
        );

    static {
        scenarios.put(
            "tac",
            Paths.get("docs",
                "20170511 optimisation_remake",
                "kitchensink", "half", "best",
                "tac.yaml"
            )
        );
        scenarios.put(
            "kitchensink",
            Paths.get("docs",
                "20170511 optimisation_remake",
                "kitchensink", "half", "best",
                "kitchensink.yaml"
            )
        );
        scenarios.put(
            "mpa",
            Paths.get("docs",
                "20170511 optimisation_remake",
                "kitchensink", "half", "best",
                "mpa-only.yaml"
            )
        );
        scenarios.put(
            "season",
            Paths.get("docs",
                "20170511 optimisation_remake",
                "kitchensink", "half", "best",
                "season.yaml"
            )
        );
        scenarios.put(
            "itq",
            Paths.get("docs",
                "20170511 optimisation_remake",
                "kitchensink", "half", "best",
                "itq.yaml"
            )
        );
    }

    public static void main(final String[] args) throws FileNotFoundException {

        Logger.getGlobal().setLevel(Level.INFO);

        for (long run = SEED; run < SEED + NUMBER_OF_RUNS; run++) {

            for (final Map.Entry<String, Path> scenario : scenarios.entrySet()) {

                final FishYAML yaml = new FishYAML();
                final PrototypeScenario scenario1 = yaml.loadAs(
                    new FileReader(scenario.getValue().toFile()), PrototypeScenario.class
                );
                final FishState state = new FishState(run);
                state.setScenario(scenario1);
                state.start();
                while (state.getYear() < 20)
                    state.schedule.step(state);
                Logger.getGlobal().info(scenario.getKey() + " " + run);

                FishStateUtilities.printCSVColumnsToFile(
                    outputFolder.resolve(scenario.getKey() + "_" + run + ".csv").toFile(),
                    state.getYearlyDataSet().getColumn("Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Species 1 Landings"),
                    state.getYearlyDataSet().getColumn("Species 0 Recruitment"),
                    state.getYearlyDataSet().getColumn("Species 1 Recruitment"),
                    state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                    state.getYearlyDataSet().getColumn("Total Effort"),
                    state.getYearlyDataSet().getColumn("Biomass Species 0"),
                    state.getYearlyDataSet().getColumn("Biomass Species 1")
                );

                double score = 0;
                for (final double landings : state.getYearlyDataSet().getColumn("Species 0 Landings"))
                    score += landings;
                score += state.getYearlyDataSet().getColumn("Biomass Species 1").getLatest();

                Logger.getGlobal().info("score: " + score);
            }
        }


    }

}
