package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 9/30/16.
 */
public class BiasedHardSwitch {


    public static final Path FOLDER = Paths.get("docs", "20161004 adaptive_tax");

    final public static long RANDOM_SEED = 0;

    public static void main(String[] args) throws IOException {


        //adaptive tax
        /*
        runSwitchScenario("good_tax.yaml", "good_tax.csv", FOLDER);
        runSwitchScenario("hardswitch.yaml", "default.csv", FOLDER);

        runSwitchScenario("good_subsidy.yaml", "good_subsidy.csv", FOLDER);

        runSwitchScenario("biased_hardswitch.yaml", "no-policy.csv", FOLDER);
        runSwitchScenario("tax.yaml", "crazy_tax.csv", FOLDER);
*/

        //pid tax
        Path pid = Paths.get("docs","20161012 pid_tax");

        runSwitchScenario("tax.yaml", "crazy_tax.csv", pid);
        runSwitchScenario("optimal_tax.yaml", "optimal_tax.csv", pid);



    }

    public static void runSwitchScenario(final String inputFileName, final String outputFileName,
                                         final Path container) throws IOException {
        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                container.resolve(inputFileName)));
        FishState state = new FishState(RANDOM_SEED);


        int firstSpecies = 0;
        int secondSpecies = 1;
        PrototypeScenario scenario = yaml.loadAs(scenarioYaml,
                                                 PrototypeScenario.class);


        //add special data
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                model.getYearlyDataSet().registerGatherer(model.getSpecies().get(firstSpecies)+ " Catchers", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers()) {
                            if(((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[firstSpecies]>0)
                                total ++;

                        }
                        return total;
                    }
                }, Double.NaN);

                model.getDailyDataSet().registerGatherer(model.getSpecies().get(firstSpecies)+ " Catchers", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers()) {
                            if(((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[firstSpecies]>0)
                                total ++;

                        }
                        return total;
                    }
                }, Double.NaN);

                model.getYearlyDataSet().registerGatherer(model.getSpecies().get(secondSpecies) + " Catchers", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers()) {
                            if(((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[secondSpecies]>0)
                                total ++;

                        }
                        return total;
                    }
                }, Double.NaN);

                model.getDailyDataSet().registerGatherer(model.getSpecies().get(secondSpecies) + " Catchers", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers()) {
                            if(((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[secondSpecies]>0)
                                total ++;

                        }
                        return total;
                    }
                }, Double.NaN);


            }

            /**
             * tell the startable to turnoff,
             */
            @Override
            public void turnOff() {

            }
        });


        //now work!
        state.setScenario(scenario);
        state.start();
        while(state.getYear() < 45)
            state.schedule.step(state);


        FishStateUtilities.printCSVColumnsToFile(container.resolve(outputFileName).toFile(),
                                                 state.getYearlyDataSet().getColumn(state.getSpecies().get(firstSpecies)+ " Catchers"),
                                                 state.getYearlyDataSet().getColumn(state.getSpecies().get(secondSpecies)+ " Catchers"),
                                                 state.getYearlyDataSet().getColumn(state.getSpecies().get(firstSpecies)+ " Average Sale Price"),
                                                 state.getYearlyDataSet().getColumn(state.getSpecies().get(secondSpecies)+ " Average Sale Price"),
                                                 state.getYearlyDataSet().getColumn(state.getSpecies().get(firstSpecies)+ " Landings"),
                                                 state.getYearlyDataSet().getColumn(state.getSpecies().get(secondSpecies)+ " Landings"),
                                                 state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                                                 state.getYearlyDataSet().getColumn( "Biomass " + state.getSpecies().get(firstSpecies).getName()),
                                                 state.getYearlyDataSet().getColumn( "Biomass " + state.getSpecies().get(secondSpecies).getName()));

        FishStateUtilities.printCSVColumnsToFile(container.resolve("tax_" + outputFileName).toFile(),
                                                 state.getDailyDataSet().getColumn(state.getSpecies().get(firstSpecies)+ " Landings"),
                                                 state.getDailyDataSet().getColumn(state.getSpecies().get(secondSpecies)+ " Landings"),
                                                 state.getDailyDataSet().getColumn("Price of Species 1 at Port 0")
        );
    }

}
