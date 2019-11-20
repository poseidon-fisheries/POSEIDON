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
import sim.display.Console;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FullSeasonalRetiredDecoratorFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumDaysAYearFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.jsonexport.JsonManagerFactory;
import uk.ac.ox.oxfish.model.plugins.StartingMPAFactory;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.factory.DepthMPAFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
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

//
//
//
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "mpa_rect",
                "No Take Zone (Example)",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {
                        FlexibleScenario flexible = (FlexibleScenario) scenario;

                        StartingMPAFactory mpa = new StartingMPAFactory();
                        mpa.getStartingMPAs().add(new StartingMPA(74,50,24,20));

                        flexible.getPlugins().add(mpa);
                        for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {
                            fisherDefinition.setRegulation(new ProtectedAreasOnlyFactory());
                        }


                    }
                },
                "No take zone: a sample no take zone is shown near the border of WPP 712 and 713 near Sumenep. Fishing is restricted for all boats (5 GT and above) in this area. Boats do pass through this area "


        );

//
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "noquit",
                "No Management (baseline), No Fishery Exit",
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
                }, "Baseline scenario - no management. Vessels do not exit the fishery when profitability declines and we see a long term decline in the stock."


        );
//
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "baseline",
                "No Management (baseline), No Fishery Exit",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                null,
                "Baseline scenario: no management of fishing so we see stocks get depleted. Unprofitable boats leave the fishery, so eventually stocks recover."


        );
//
//        //150 days
//
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "150days",
                "150 Fishing Days - All Boats",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupEffortControlConsumer(
                        new String[]{"big","small","medium","small10"},
                        2,
                        150
                ), "Season closure for all boats: boats are allowed 150 fishing days each year."

        );


        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "150days_10gt",
                "150 Fishing Days - Boats 10GT+",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupEffortControlConsumer(
                        new String[]{"big","medium","small10"},
                        2,
                        150
                ), "Season closure for boats 10GT and above: these boats are allowed 150 fishing days each year. Small boats (5-9 GT) have no management."

        );
//
//        //100 days
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "100days",
                "100 Fishing Days - All Boats",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupEffortControlConsumer(
                        new String[]{"big","small","medium","small10"},
                        2,
                        100
                ), "Season closure for all boats: boats are allowed 100 fishing days each year."

        );

//
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "100days_10gt",
                "100 Fishing Days - Boats 10GT+",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupEffortControlConsumer(
                        new String[]{"big","medium","small10"},
                        2,
                        100
                ), "Season closure for boats 10GT and above: these boats are allowed 100 fishing days each year. Small boats (5-9 GT) have no management."

        );
//
//        //premium
//
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "premium",
                "Price Premium for Mature L. malabaricus",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupPremiumConsumer(
                        10,"Lutjanus malabaricus",2


                ), "Price Premium: Fishers (all boats 5GT and above) receive double the price for any mature Lutjanus malabaricus they catch."

        );
//
//        //delays
//        runScenario(
//                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
//                "delays",
//                "10 Days Port Delay - All",
//                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
//                YEARS_TO_RUN,
//                1,
//                0l,
//                Slice2019Sweeps.setupDelaysConsumer(
//                        new String[]{"big","small","medium","small10"},
//                        2,
//                        10
//                ), "Each boat is forced to spend 10 days at port between each trip"
//
//        );
//
//        //fleet reduction
        runScenario(
                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
                "reduction",
                "8% Annual Reduction in Fishing Fleet",
                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
                YEARS_TO_RUN,
                1,
                0l,
                Slice2019Sweeps.setupFleetReductionConsumer(
                        2,
                        .08
                ), "Fleet reduction: The fishing fleet (all boats 5GT and above) is reduced by 8% each year. Vessels may also exit the fishery voluntarily due to lack of profitability."

        );
//
//
//
//
//        runScenario(
//                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
//                "mpa75",
//                "Marine Protected Area - >75m",
//                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
//                YEARS_TO_RUN,
//                1,
//                0l,
//                new Consumer<Scenario>() {
//                    @Override
//                    public void accept(Scenario scenario) {
//                        FlexibleScenario flexible = (FlexibleScenario) scenario;
//
//                        for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {
//
//                            //large boats already never quit
//                            DepthMPAFactory regulation = new DepthMPAFactory();
//                            regulation.setMaxDepth(new FixedDoubleParameter(10000));
//                            regulation.setMinDepth(new FixedDoubleParameter(75));
//                            fisherDefinition.setRegulation(regulation);
//                        }
//                    }
//                }, "All areas whose depth is above 75m are closed to fishing. This protects somewhat Pristipomoides multidens and Lutjanus erythropterus; in the short run boats fish more Lutjanus malabaricus, in the long run they quit"
//
//
//        );
//
//
//        runScenario(
//                Paths.get(Slice2019Sweeps.DIRECTORY, "historical20_baranov_8h.yaml"),
//                "mpa_coastal",
//                "Marine Protected Area - <75m",
//                Paths.get("docs/indonesia_hub/runs/712/slice2019/visuals/"),
//                YEARS_TO_RUN,
//                1,
//                0l,
//                new Consumer<Scenario>() {
//                    @Override
//                    public void accept(Scenario scenario) {
//                        FlexibleScenario flexible = (FlexibleScenario) scenario;
//
//                        for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {
//
//                            //large boats already never quit
//                            DepthMPAFactory regulation = new DepthMPAFactory();
//                            regulation.setMaxDepth(new FixedDoubleParameter(75));
//                            regulation.setMinDepth(new FixedDoubleParameter(-10000000));
//                            fisherDefinition.setRegulation(regulation);
//                        }
//                    }
//                }, "All areas whose depth is below 75m are closed to fishing. This makes Lutjanus malabaricus impossible to catch which results in the liquidation of the entire small fleet"
//
//
//        );
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

        for (FisherDefinition fisherDefinition : scenario.getFisherDefinitions()) {
            fisherDefinition.setFishingStrategy(
                    new MaximumDaysAYearFactory(
                            240,
                            fisherDefinition.getFishingStrategy()
                    )


            );
        }

        final FishState model = new FishState(seed);
        model.setScenario(scenario);


//        FishGUI fishGUI = new FishGUI(model);
//        Console c = new Console(fishGUI);
//        c.setVisible(true);


        model.start();

        while (model.getYear() < yearsToRun)
            model.schedule.step(model);

        model.schedule.step(model);

        FishStateUtilities.writeAdditionalOutputsToFolder(outputDirectory,
                                                          model);

    }





}
