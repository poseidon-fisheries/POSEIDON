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

import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.ExhaustiveAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PlanningHeatmapDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

/**
 * Created by carrknight on 8/16/16.
 */
public class FiveYearBase {


    public static void exploreExploit(String[] args) throws FileNotFoundException {

        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(
                new FileReader(Paths.get("runs", "optimization", "kalman_tune",  "rbf",
                                         "kernel.yaml").toFile()),
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


    public static void main(String[] args) throws FileNotFoundException {

        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(
                new FileReader(Paths.get("runs", "optimization", "kalman_tune",  "itq_plan.yaml").toFile()),
                PrototypeScenario.class);
        PlanningHeatmapDestinationFactory destinationStrategy = new PlanningHeatmapDestinationFactory();
        ExhaustiveAcquisitionFunctionFactory acquisition = new ExhaustiveAcquisitionFunctionFactory();
        acquisition.setProportionSearched(new FixedDoubleParameter(.1));
        destinationStrategy.setAlmostPerfectKnowledge(true);
        destinationStrategy.setAcquisition(acquisition);
        scenario.setDestinationStrategy(destinationStrategy);

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
