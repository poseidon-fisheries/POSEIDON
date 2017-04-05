package uk.ac.ox.oxfish.experiments;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 4/3/17.
 */
public class AsymmetricShock {


    private final static int RUNS = 10;

    private static final Path MAIN_DIRECTORY = Paths.get("docs",
                                                         "20170403 narrative",
                                                         "asymmetry");


    final static private double[] gasPrices = new double[]{.05,.25,.01};

    public static void main(String[] args) throws FileNotFoundException {

        for(double gasPrice : gasPrices)
            for(int run = 0; run<RUNS; run++)
            {
                FishYAML yaml = new FishYAML();
                Scenario scenario = yaml.loadAs(new FileReader(MAIN_DIRECTORY.resolve("baseline.yaml").toFile()),
                                                Scenario.class);

                FishState state = new FishState(run);
                state.setScenario(scenario);
                state.start();
                //at year 5: shock!
                state.scheduleOnceInXDays(
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                for(Port port : state.getPorts())
                                    port.setGasPricePerLiter(gasPrice);
                            }
                        },
                        StepOrder.POLICY_UPDATE,
                        5 * 365 - 1 //at the end of year 5!
                );
                while(state.getYear()<=10)
                    state.schedule.step(state);
                FishStateUtilities.printCSVColumnsToFile(
                        MAIN_DIRECTORY.resolve("asymmetry_"+gasPrice+"_"+run +".csv").toFile(),
                        state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                        state.getYearlyDataSet().getColumn("Small Fishers Total Income"),
                        state.getYearlyDataSet().getColumn("Large Fishers Total Income")
                );
            }


    }

}
