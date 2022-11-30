/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.experiments.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.heatmaps.BiomassHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.CatchFromFadSetsHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.CatchFromUnassociatedSetsHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.FadDensityHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.FadDeploymentHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.FadSetHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.HeatmapGatherer;
import uk.ac.ox.oxfish.model.data.heatmaps.NonAssociatedSetHeatmapGatherer;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SampleTunaRun {

    private static final int NUM_YEARS_TO_RUN = 1;
    private static final int NUM_RUNS_PER_POLICY = 1;

    private static final Path scenarioPath =
        Paths.get(
            System.getProperty("user.home"),
            "workspace", "tuna", "calibration", "results",
            "cenv0729", "2022-11-11_17.58.21_catchability_global",
            "calibrated_scenario.yaml"
        );

    private static final Path outputPath = scenarioPath.getParent();

    public static void main(final String[] args) {
        new Runner<>(EpoBiomassScenario.class, scenarioPath, outputPath)
            .setAfterStartConsumer(state -> System.out.println(state.getModel().getMap().asASCII()))
//            .requestYearlyData()
//            .requestFisherYearlyData()
//            .registerRowProvider("action_log.csv", PurseSeineActionsLogger::new)
//            .registerRowProviders("heatmap_data.csv", SampleTunaRun::makeHeatmapProviders)
            .run(NUM_YEARS_TO_RUN, NUM_RUNS_PER_POLICY);
    }

    private static Iterable<? extends RowProvider> makeHeatmapProviders(final FishState fishState) {
        final int interval = 30;
        ImmutableList.Builder<HeatmapGatherer> gatherers = new ImmutableList.Builder<>();
        gatherers.add(
            new FadDeploymentHeatmapGatherer(interval),
            new FadSetHeatmapGatherer(interval),
            new NonAssociatedSetHeatmapGatherer(interval),
            new FadDensityHeatmapGatherer(interval)
        );
        fishState.getSpecies().forEach(species -> {
            gatherers.add(new BiomassHeatmapGatherer(interval, species));
            gatherers.add(new CatchFromFadSetsHeatmapGatherer(interval, species));
            gatherers.add(new CatchFromUnassociatedSetsHeatmapGatherer(interval, species));
        });
        return gatherers.build();
    }

}
