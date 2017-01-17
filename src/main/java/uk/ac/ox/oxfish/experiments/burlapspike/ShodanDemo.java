package uk.ac.ox.oxfish.experiments.burlapspike;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.RandomOpenCloseController;
import uk.ac.ox.oxfish.model.regs.factory.ShodanFromFileFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is to produce the demo csvs, it doesn't actually discover anything
 * Created by carrknight on 1/4/17.
 */
public class ShodanDemo {


    public static void main2(String[] args) throws FileNotFoundException {


        FishYAML yaml = new FishYAML();

        PrototypeScenario scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        RandomOpenCloseController random = new RandomOpenCloseController();
        scenario.setRegulation(random);

        runSimulation(scenario, "random", 20, Paths.get("docs", "20170103 shodan_test", "random" + ".csv"));
        runSimulation(scenario, "random80", 80, Paths.get("docs", "20170103 shodan_test", "random80" + ".csv"));

        scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        AnarchyFactory anarchy = new AnarchyFactory();
        scenario.setRegulation(anarchy);

        runSimulation(scenario, "anarchy", 20, Paths.get("docs", "20170103 shodan_test", "anarchy" + ".csv"));
        runSimulation(scenario, "anarchy80", 80, Paths.get("docs", "20170103 shodan_test", "anarchy80" + ".csv"));



/*
        //policy optimal
        scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        TACMonoFactory regulation = new TACMonoFactory();
        regulation.setQuota(new FixedDoubleParameter(712038.574219)); //found through the bayesian optimizer
        scenario.setRegulation(regulation);

        runSimulation(scenario, "baseline", 20, Paths.get("docs", "20170103 shodan_test", "baseline" + ".csv"));
        runSimulation(scenario, "baseline80", 80, Paths.get("docs", "20170103 shodan_test", "baseline80" + ".csv"));
*/


        /*
        //optimal shodan biomass
        scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        ShodanFromFileFactory shodan = new ShodanFromFileFactory();
        shodan.setPathToXml(Paths.get("docs", "20170103 shodan_test", "biomassQ.xml").toAbsolutePath().toString());
        scenario.setRegulation(shodan);

        runSimulation(scenario, "shodan_B", 20, Paths.get("docs", "20170103 shodan_test", "shodan_B" + ".csv"));
        runSimulation(scenario, "shodan_B80", 80, Paths.get("docs", "20170103 shodan_test", "shodan_B80" + ".csv"));



        //optimal cash-distance
        scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        shodan = new ShodanFromFileFactory();
        shodan.setPathToXml(Paths.get("docs", "20170103 shodan_test", "cashQ.xml").toAbsolutePath().toString());
        scenario.setRegulation(shodan);
        runSimulation(scenario, "shodan_C", 20, Paths.get("docs", "20170103 shodan_test", "shodan_C" + ".csv"));
        runSimulation(scenario, "shodan_C80", 80, Paths.get("docs", "20170103 shodan_test", "shodan_C80" + ".csv"));


        //long term biomass
        scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        shodan = new ShodanFromFileFactory();
        shodan.setPathToXml(Paths.get("docs", "20170103 shodan_test", "shodan_general_B.xml").toAbsolutePath().toString());
        scenario.setRegulation(shodan);
        runSimulation(scenario, "shodan_general_B", 20,
                      Paths.get("docs", "20170103 shodan_test", "shodan_general_B" + ".csv"));
        runSimulation(scenario, "shodan_general_B80", 80,
                      Paths.get("docs", "20170103 shodan_test", "shodan_general_B80" + ".csv"));
*/



    }


    public static void main(String[] args) throws FileNotFoundException {
        /*
        generalRun(Paths.get("runs",
                             "burlap_infinity_general",
                             "99_sarsa_biomass_9lambda_fourier"), "agent_500");
                             */
        /*
        generalRun(Paths.get("runs",
                             "burlap_infinity_general",
                             "99_sarsa_cashdistance_9lambda_fourier"), "agent_500");
                                     */
        /*
        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_biomass_9lambda_fourier2"), "agent_1000");

        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_biomass_9lambda_fourier_baseline"), "agent_1000");

                             */

        /*
        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_biomass_9lambda_fourier_baseline"), "agent_1000");
*.
        /*
        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "90_sarsa_biomass_9lambda_fourier_baseline"), "agent_1000");

        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "90_sarsa_biomass_9lambda_fourier"), "agent_1000");
                             */

/*
        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_cashdistanceclosed_9lambda_fourier"), "agent_1000");

        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_cashdistanceclosed_9lambda_fourier_baseline"), "agent_1000");
        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_cashdistanceclosed_9lambda_fourier_highepsilon"), "agent_1000");
*/


        //maxes!

        /*
        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_cashdistanceclosed_9lambda_fourier_baseline_highepsilon"), "agent_780");
                             */
/*
        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_cashdistanceclosed_9lambda_fourier_highepsilon"), "agent_860");

        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_cashdistanceclosed_9lambda_fourier_baseline"), "agent_430");
                             */
        /*
        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_cashdistanceclosed_9lambda_fourier"), "agent_400");
                             */

        generalRun(Paths.get("runs",
                             "burlap_infinity",
                             "999_sarsa_cashdistanceclosed_9lambda_fourier_baseline_highepsilon2"), "agent_1160");
    }

    private static void generalRun(final Path containerPath, final String agentName) throws FileNotFoundException {
        FishYAML yaml = new FishYAML();
        //policy optimal
        PrototypeScenario scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        ShodanFromFileFactory shodan = new ShodanFromFileFactory();
        shodan.setPathToXml(containerPath.resolve(agentName+".xml").toAbsolutePath().toString());
        scenario.setRegulation(shodan);
        runSimulation(scenario, agentName, 20,
                      containerPath.resolve(agentName+".csv"));
        runSimulation(scenario, "80_"+agentName, 80,
                      containerPath.resolve("80_"+agentName+".csv"));


    }

    private static void runSimulation(
            PrototypeScenario scenario, final String name, final int yearsToRun, final Path outputPath) {
        FishState state = new FishState(0);
        state.setScenario(scenario);
        state.start();
        state.attachAdditionalGatherers();
        while(state.getYear()< yearsToRun)
            state.schedule.step(state);

        //ugly
        if(scenario.getRegulation() instanceof ShodanFromFileFactory)
            FishStateUtilities.printCSVColumnsToFile(
                    outputPath.toFile(),
                    state.getDailyDataSet().getColumn("Biomass Species 0"),
                    state.getDailyDataSet().getColumn("Species 0 Landings"),
                    state.getDailyDataSet().getColumn("Average Cash-Flow"),
                    state.getDailyDataSet().getColumn("Shodan Policy"), //add this when it's a shodan run
                    state.getDailyDataSet().getColumn("Average Distance From Port")
            );
        else
            FishStateUtilities.printCSVColumnsToFile(
                    outputPath.toFile(),
                    state.getDailyDataSet().getColumn("Biomass Species 0"),
                    state.getDailyDataSet().getColumn("Species 0 Landings"),
                    state.getDailyDataSet().getColumn("Average Cash-Flow"),
                    state.getDailyDataSet().getColumn("Average Distance From Port")
            );
        System.out.println(name + ": " + reward(state));
    }


    public static double reward(FishState state)
    {
        double initialScore = 0;
        for(Double cashflow : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore+=cashflow;
        return initialScore;
    }
}
