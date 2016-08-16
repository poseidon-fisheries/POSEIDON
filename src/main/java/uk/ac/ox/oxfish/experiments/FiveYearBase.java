package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

/**
 * Created by carrknight on 8/16/16.
 */
public class FiveYearBase {


    public static void main(String[] args) throws FileNotFoundException {

        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(
                new FileReader(Paths.get("runs", "optimization", "kalman_tune",  "rbf",
                                         "itq_plan","itq_plan.yaml").toFile()),
                PrototypeScenario.class);
        scenario.setDestinationStrategy(new PerTripImitativeDestinationFactory());

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        while(state.getYear()<5)
            state.schedule.step(state);


        double sum = 0;
        for(Double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
            sum+=cash;
        System.out.println(sum);
    }
}
