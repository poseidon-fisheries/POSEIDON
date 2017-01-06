package uk.ac.ox.oxfish.experiments.burlapspike;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.ShodanFromFileFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

/**
 * This is to produce the demo csvs, it doesn't actually discover anything
 * Created by carrknight on 1/4/17.
 */
public class ShodanDemo {


    public static void main(String[] args) throws FileNotFoundException {


        FishYAML yaml = new FishYAML();
        //policy optimal
        PrototypeScenario scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        TACMonoFactory regulation = new TACMonoFactory();
        regulation.setQuota(new FixedDoubleParameter(712038.574219)); //found through the bayesian optimizer
        scenario.setRegulation(regulation);

        FishState state = new FishState(0);
        state.setScenario(scenario);
        state.start();
        state.attachAdditionalGatherers();
        while(state.getYear()<20)
            state.schedule.step(state);

        FishStateUtilities.printCSVColumnsToFile(
                Paths.get("docs", "20170103 shodan_test", "baseline.csv").toFile(),
                state.getDailyDataSet().getColumn("Biomass Species 0"),
                state.getDailyDataSet().getColumn("Species 0 Landings"),
                state.getDailyDataSet().getColumn("Average Cash-Flow"),
                state.getDailyDataSet().getColumn("Average Distance From Port")
        );
        System.out.println("baseline: " + reward(state));


        //optimal shodan biomass
        scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        ShodanFromFileFactory shodan = new ShodanFromFileFactory();
        shodan.setPathToXml(Paths.get("docs", "20170103 shodan_test", "biomassQ.xml").toAbsolutePath().toString());
        scenario.setRegulation(shodan);

        state = new FishState(0);
        state.setScenario(scenario);
        state.start();
        state.attachAdditionalGatherers();
        while(state.getYear()<20)
            state.schedule.step(state);

        FishStateUtilities.printCSVColumnsToFile(
                Paths.get("docs", "20170103 shodan_test", "shodan_B.csv").toFile(),
                state.getDailyDataSet().getColumn("Biomass Species 0"),
                state.getDailyDataSet().getColumn("Species 0 Landings"),
                state.getDailyDataSet().getColumn("Average Cash-Flow"),
                state.getDailyDataSet().getColumn("Shodan Policy"),
                state.getDailyDataSet().getColumn("Average Distance From Port")
        );
        System.out.println("shodan biomass: " + reward(state));



        scenario = yaml.loadAs(
                new FileReader(Paths.get("docs", "20170103 shodan_test", "scenario.yaml").toFile()),
                PrototypeScenario.class);
        shodan = new ShodanFromFileFactory();
        shodan.setPathToXml(Paths.get("docs", "20170103 shodan_test", "cashQ.xml").toAbsolutePath().toString());
        scenario.setRegulation(shodan);

        state = new FishState(0);
        state.setScenario(scenario);
        state.start();
        state.attachAdditionalGatherers();
        while(state.getYear()<20)
            state.schedule.step(state);

        FishStateUtilities.printCSVColumnsToFile(
                Paths.get("docs", "20170103 shodan_test", "shodan_B.csv").toFile(),
                state.getDailyDataSet().getColumn("Biomass Species 0"),
                state.getDailyDataSet().getColumn("Species 0 Landings"),
                state.getDailyDataSet().getColumn("Average Cash-Flow"),
                state.getDailyDataSet().getColumn("Shodan Policy"),
                state.getDailyDataSet().getColumn("Average Distance From Port")
        );
        System.out.println("shodan cash: " + reward(state));



    }


    public static double reward(FishState state)
    {
        double initialScore = 0;
        for(Double cashflow : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore+=cashflow;
        return initialScore;
    }
}
