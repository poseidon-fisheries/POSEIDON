/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments.indonesia;

import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FullSeasonalRetiredDecoratorFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.jsonexport.JsonManagerFactory;
import uk.ac.ox.oxfish.model.regs.factory.DepthMPAFactory;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class Slice2019Visuals {


    private static final int YEARS_TO_RUN = 11;

    public static void main(String[] args) throws IOException {


        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "noquit",
                "Business as Usual - No Quitting",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {
                        FlexibleScenario flexible = (FlexibleScenario) scenario;

                        for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {

                            //large boats already never quit
                            if(fisherDefinition.getDepartingStrategy() instanceof FullSeasonalRetiredDecoratorFactory)

                                ((FullSeasonalRetiredDecoratorFactory) fisherDefinition.getDepartingStrategy()).
                                        setInertia(new FixedDoubleParameter(100));
                        }
                    }
                }, "Business as usual but boats aren't allowed to quit the fishery. This results in a protracted period of low depletion for all species but aereolatus."


        );

        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "baseline",
                "Business as Usual",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                null,
                "Business as usual; low profitability leads to sharp reduction of the fishing fleet and consequent recovery of the stock"


        );

        //150 days

        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "150days",
                "Max 150 Days - All",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupEffortControlConsumer(
                        new String[]{"big","small","medium","small10"},
                        2,
                        150
                ), "All boats are allowed only 150 days effective out each year"

        );


        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "150days_10gt",
                "Max 150 Days - 10+GT",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupEffortControlConsumer(
                        new String[]{"big","medium","small10"},
                        2,
                        150
                ), "Each boat above 10GT is allowed only 150 days effective out each year"

        );

        //100 days
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "100days",
                "Max 100 Days - All",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupEffortControlConsumer(
                        new String[]{"big","small","medium","small10"},
                        2,
                        100
                ), "Each boat above 10GT is allowed only 100 days effective out each year"

        );


        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "100days_10gt",
                "Max 100 Days - 10+GT",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupEffortControlConsumer(
                        new String[]{"big","medium","small10"},
                        2,
                        100
                ), "Each boat above 10GT is allowed only 100 days effective out each year"

        );

        //premium

        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "premium",
                "Premium Malabaricus - 100%",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupPremiumConsumer(
                        10,"Lutjanus malabaricus",2


                ), "Price for mature Lutjanus malabaricus is doubled"

        );

        //delays
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "delays",
                "10 Days Port Delay - All",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupDelaysConsumer(
                        new String[]{"big","small","medium","small10"},
                        2,
                        10
                ), "Each boat is forced to spend 10 days at port between each trip"

        );

        //fleet reduction
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "reduction",
                "8% Yearly Fleet Reduction",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupFleetReductionConsumer(
                        2,
                        .08
                ), "A steady 8% rate of fleet reduction is imposed on all boats (on top of voluntary exits)"

        );


                runScenario(
                        Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                        "mpa75",
                        "Marine Protected Area - >75m",
                        Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                        YEARS_TO_RUN,
                        1,
                        0l,
                        new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {
                        FlexibleScenario flexible = (FlexibleScenario) scenario;

                        for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {

                            //large boats already never quit
                            DepthMPAFactory regulation = new DepthMPAFactory();
                            regulation.setMaxDepth(new FixedDoubleParameter(10000));
                            regulation.setMinDepth(new FixedDoubleParameter(75));
                            fisherDefinition.setRegulation(regulation);
                        }
                    }
                }, "All areas whose depth is above 75m are closed to fishing. This protects somewhat Pristipomoides multidens and Lutjanus erythropterus; in the short run boats fish more Lutjanus malabaricus, in the long run they quit"


                );


        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "mpa_coastal",
                "Marine Protected Area - <75m",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {
                        FlexibleScenario flexible = (FlexibleScenario) scenario;

                        for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {

                            //large boats already never quit
                            DepthMPAFactory regulation = new DepthMPAFactory();
                            regulation.setMaxDepth(new FixedDoubleParameter(75));
                            regulation.setMinDepth(new FixedDoubleParameter(-10000000));
                            fisherDefinition.setRegulation(regulation);
                        }
                    }
                }, "All areas whose depth is below 75m are closed to fishing. This makes Lutjanus malabaricus impossible to catch which results in the liquidation of the entire small fleet"


        );
    }



    static public void runScenario(
            Path scenarioFile,
            String simulationFilePrefix,
            String simulationTitle,
            Path outputDirectory,
            int yearsToRun,
            int yearsToSkip,
            long seed,
            @Nullable Consumer<Scenario> modifier,
            final String modelDescription) throws IOException {


        FishYAML yaml = new FishYAML();
        FlexibleScenario scenario = yaml.loadAs(
                new FileReader(scenarioFile.toFile()),
                FlexibleScenario.class);

        final JsonManagerFactory jsonManagerFactory = new JsonManagerFactory();
        jsonManagerFactory.setNumYearsToSkip(yearsToSkip);
        jsonManagerFactory.setFilePrefix(simulationFilePrefix);
        jsonManagerFactory.setDashboardName(simulationTitle);
        jsonManagerFactory.setModelDescription(modelDescription);
        scenario.getPlugins().add(jsonManagerFactory);

        if(modifier!=null)
            modifier.accept(scenario);

        final FishState model = new FishState(seed);
        model.setScenario(scenario);
        model.start();

        while (model.getYear() < yearsToRun)
            model.schedule.step(model);

        model.schedule.step(model);

        FishStateUtilities.writeAdditionalOutputsToFolder(outputDirectory,
                                                          model);

    }





}
