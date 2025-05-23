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

import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingAllUnsellableFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscardingFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQStringFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by carrknight on 5/4/17.
 */
public class DiscardExperiment {


    public static final int TOTAL_QUOTA_0 = 200000;
    public static final int TOTAL_QUOTA_1 = 25000;
    public static final String YEARLY_QUOTA_MAPS = "0:" + TOTAL_QUOTA_0 + ",1:" + TOTAL_QUOTA_1;
    public static final int NUMBER_OF_FISHERS = 100;
    public static final String YEARLY_ITQ_MAPS = "0:" + TOTAL_QUOTA_0 / 100 + ",1:" + TOTAL_QUOTA_1 / NUMBER_OF_FISHERS;
    private static final Map<String, Consumer<PrototypeScenario>> conditions = new HashMap<>(4);
    public static Path baseline = Paths.get("docs", "20170504 discards");


    static {
        /*
        conditions.put("tac",
                       new Consumer<PrototypeScenario>() {
                           @Override
                           public void accept(PrototypeScenario prototypeScenario) {


                               MultiTACStringFactory regulation = new MultiTACStringFactory();
                               regulation.setYearlyQuotaMaps(YEARLY_QUOTA_MAPS);
                               prototypeScenario.setRegulation(regulation);


                           }
                       });

        conditions.put("weak_tac",
                       new Consumer<PrototypeScenario>() {
                           @Override
                           public void accept(PrototypeScenario prototypeScenario) {
                               WeakMultiTACStringFactory regulation = new WeakMultiTACStringFactory();
                               regulation.setYearlyQuotaMaps(YEARLY_QUOTA_MAPS);
                               prototypeScenario.setRegulation(regulation);
                               prototypeScenario.setDiscardingStrategy(new DiscardingAllUnsellableFactory());
                           }
                       });

        conditions.put("weak_tac_strict",
                       new Consumer<PrototypeScenario>() {
                           @Override
                           public void accept(PrototypeScenario prototypeScenario) {
                               WeakMultiTACStringFactory regulation = new WeakMultiTACStringFactory();
                               regulation.setYearlyQuotaMaps(YEARLY_QUOTA_MAPS);
                               prototypeScenario.setRegulation(regulation);
                               prototypeScenario.setDiscardingStrategy(new NoDiscardingFactory());
                           }
                       });

        conditions.put("no_limits",
                       new Consumer<PrototypeScenario>() {
                           @Override
                           public void accept(PrototypeScenario prototypeScenario) {


                               AnarchyFactory regulation = new AnarchyFactory();
                               prototypeScenario.setRegulation(regulation);
                               prototypeScenario.setDiscardingStrategy(new NoDiscardingFactory());


                           }
                       });
*/
        conditions.put(
            "itq",
            prototypeScenario -> {
                final MultiITQStringFactory regulation = new MultiITQStringFactory();
                regulation.setYearlyQuotaMaps(YEARLY_ITQ_MAPS);
                prototypeScenario.setRegulation(regulation);
                prototypeScenario.setDiscardingStrategy(new NoDiscardingFactory());
                prototypeScenario.setUsePredictors(true);
            }
        );

        conditions.put(
            "itq_discarding",
            prototypeScenario -> {
                final MultiITQStringFactory regulation = new MultiITQStringFactory();
                regulation.setYearlyQuotaMaps(YEARLY_ITQ_MAPS);
                prototypeScenario.setRegulation(regulation);
                prototypeScenario.setDiscardingStrategy(new DiscardingAllUnsellableFactory());
                prototypeScenario.setUsePredictors(true);

            }
        );

    }


    public static void main(final String[] args) throws FileNotFoundException {


        Logger.getGlobal().setLevel(Level.INFO);
        final FishYAML yaml = new FishYAML();

        for (final Map.Entry<String, Consumer<PrototypeScenario>> condition : conditions.entrySet()) {

            for (int run = 0; run < 10; run++) {
                final PrototypeScenario scenario = yaml.loadAs(
                    new FileReader(baseline.resolve("discards_base.yaml").toFile()),
                    PrototypeScenario.class
                );

                scenario.setFishers(NUMBER_OF_FISHERS);
                condition.getValue().accept(scenario);
                final FishState state = new FishState(run);
                state.setScenario(scenario);
                state.start();

                final String name = condition.getKey() + "#" + run;
                Logger.getGlobal().info(name);

                while (state.getYear() < 20) {
                    state.schedule.step(state);
                }


                //if(state.getYearlyDataSet().getColumn())

                //if there is no ITQ, don't try to collect price data!
                if (state.getYearlyDataSet().getColumn("ITQ Prices Of Species 0") == null)
                    FishStateUtilities.printCSVColumnsToFile(
                        baseline.resolve(name + ".csv").toFile(),
                        state.getYearlyDataSet().getColumn("Species 0 Landings"),
                        state.getYearlyDataSet().getColumn("Species 1 Landings"),
                        state.getYearlyDataSet().getColumn("Species 0 Catches"),
                        state.getYearlyDataSet().getColumn("Species 1 Catches"),
                        state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                        state.getYearlyDataSet().getColumn("Total Effort"),
                        state.getYearlyDataSet().getColumn("Biomass Species 0"),
                        state.getYearlyDataSet().getColumn("Biomass Species 1")
                    );
                else
                    FishStateUtilities.printCSVColumnsToFile(
                        baseline.resolve(name + ".csv").toFile(),
                        state.getYearlyDataSet().getColumn("Species 0 Landings"),
                        state.getYearlyDataSet().getColumn("Species 1 Landings"),
                        state.getYearlyDataSet().getColumn("Species 0 Catches"),
                        state.getYearlyDataSet().getColumn("Species 1 Catches"),
                        state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                        state.getYearlyDataSet().getColumn("Total Effort"),
                        state.getYearlyDataSet().getColumn("Biomass Species 0"),
                        state.getYearlyDataSet().getColumn("Biomass Species 1"),
                        state.getYearlyDataSet().getColumn("ITQ Volume Of Species 0"),
                        state.getYearlyDataSet().getColumn("ITQ Volume Of Species 1"),
                        state.getYearlyDataSet().getColumn("ITQ Prices Of Species 0"),
                        state.getYearlyDataSet().getColumn("ITQ Prices Of Species 1")
                    );


            }

        }


    }


}
