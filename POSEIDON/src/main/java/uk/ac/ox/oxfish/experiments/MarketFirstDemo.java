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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.factory.IQMonoFactory;
import uk.ac.ox.oxfish.model.regs.factory.ITQMonoFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.ExplorationPenaltyProbabilityFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

public class MarketFirstDemo {

    public static void main(String[] args) throws IOException {

        generateAndRunMarketDemo(MarketDemoPolicy.ITQ, new FixedDoubleParameter(.1),
            new FixedDoubleParameter(5),
            Paths.get("runs", "market1", "itqFixed.csv").toFile(),
            10, 0, true
        );

        generateAndRunMarketDemo(MarketDemoPolicy.TAC, new FixedDoubleParameter(.1),
            new FixedDoubleParameter(5),
            Paths.get("runs", "market1", "tacFixed.csv").toFile(),
            10, 0, true
        );

        generateAndRunMarketDemo(MarketDemoPolicy.IQ, new FixedDoubleParameter(.1),
            new FixedDoubleParameter(5),
            Paths.get("runs", "market1", "iqFixed.csv").toFile(),
            10, 0, true
        );

        generateAndRunMarketDemo(MarketDemoPolicy.ITQ, new UniformDoubleParameter(0.01, 0.1),
            new FixedDoubleParameter(5),
            Paths.get("runs", "market1", "itqSmooth.csv").toFile(),
            10, 0, true
        );

        generateAndRunMarketDemo(MarketDemoPolicy.TAC, new UniformDoubleParameter(0.01, 0.1),
            new FixedDoubleParameter(5),
            Paths.get("runs", "market1", "tacSmooth.csv").toFile(),
            10, 0, true
        );

        generateAndRunMarketDemo(MarketDemoPolicy.IQ, new UniformDoubleParameter(0.05, 0.3),
            new FixedDoubleParameter(5),
            Paths.get("runs", "market1", "iqSmooth.csv").toFile(),
            10, 0, true
        );

        generateAndRunMarketDemo(MarketDemoPolicy.ITQ, new FixedDoubleParameter(.1),
            new UniformDoubleParameter(0, 20),
            Paths.get("runs", "market1", "itqOil.csv").toFile(),
            10, 0, true
        );

        generateAndRunMarketDemo(MarketDemoPolicy.TAC, new FixedDoubleParameter(.1),
            new UniformDoubleParameter(0, 20),
            Paths.get("runs", "market1", "tacOil.csv").toFile(),
            10, 0, true
        );

        generateAndRunMarketDemo(MarketDemoPolicy.IQ, new FixedDoubleParameter(.1),
            new UniformDoubleParameter(0, 20),
            Paths.get("runs", "market1", "iqOil.csv").toFile(),
            10, 0, true
        );

    }

    public static FishState generateAndRunMarketDemo(
        final MarketDemoPolicy policy,
        final DoubleParameter catchabilityMean,
        final DoubleParameter gasInefficiency,
        final File file,
        // nullable
        final int yearsToRun,
        final long seed,
        final boolean imitate
    ) throws IOException {
        FishState state = MarketFirstDemo.generateMarketedModel(policy, catchabilityMean,
            gasInefficiency, seed, imitate
        );
        state.start();
        while (state.getYear() < yearsToRun)
            state.schedule.step(state);
        Species species = state.getSpecies().get(0);
        // now write to file
        if (file != null) {

            LinkedList<String> list = new LinkedList<>();
            for (Fisher fisher : state.getFishers()) {
                list.add(String.valueOf
                    (((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0])
                    + "," +
                    (((RandomCatchabilityTrawl) fisher.getGear()).getGasPerHourFished())
                    + "," +
                    String.valueOf(
                        fisher.getLatestYearlyObservation(
                            species + " " + AbstractMarket.LANDINGS_COLUMN_NAME))
                    + "," +
                    String.valueOf(
                        fisher.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.FUEL_CONSUMPTION))

                );

            }
            String toWrite = list.stream().reduce((s, s2) -> s + "\n" + s2).get();

            Paths.get("runs", "market1").toFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(toWrite);
            writer.flush();
            writer.close();
        }
        return state;
    }

    public static FishState generateMarketedModel(
        final MarketDemoPolicy policy,
        final DoubleParameter catchabilityMean,
        final DoubleParameter gasInefficiency,
        final long seed,
        boolean imitate
    ) {
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new FromLeftToRightFactory());
        RandomCatchabilityTrawlFactory gear = new RandomCatchabilityTrawlFactory();
        gear.setMeanCatchabilityOtherSpecies(catchabilityMean);
        gear.setMeanCatchabilityFirstSpecies(catchabilityMean);
        gear.setGasPerHourFished(gasInefficiency);
        scenario.setGear(gear);
        // make gas expensive!
        scenario.setGasPricePerLiter(new FixedDoubleParameter(1));
        scenario.setLiterPerKilometer(new FixedDoubleParameter(0));

        if (!imitate) {

            ((ExplorationPenaltyProbabilityFactory) ((PerTripImitativeDestinationFactory) scenario.getDestinationStrategy()).
                getProbability()).setImitationProbability(new FixedDoubleParameter(0d));
        }

        if (policy.equals(MarketDemoPolicy.TAC)) {
            TACMonoFactory regulation = new TACMonoFactory();
            regulation.setQuota(new FixedDoubleParameter(400000));
            scenario.setRegulation(regulation);
            scenario.setUsePredictors(false);

        } else if (policy.equals(MarketDemoPolicy.IQ)) {
            IQMonoFactory regulation = new IQMonoFactory();
            regulation.setIndividualQuota(new FixedDoubleParameter(4000));
            scenario.setRegulation(regulation);
            scenario.setUsePredictors(false);

        } else {
            ITQMonoFactory regulation = new ITQMonoFactory();
            regulation.setIndividualQuota(new FixedDoubleParameter(4000));
            scenario.setRegulation(regulation);
            scenario.setUsePredictors(true);
        }

        FishState state = new FishState(seed, 2);
        state.setScenario(scenario);

        return state;
    }

    public enum MarketDemoPolicy {

        TAC,

        IQ,

        ITQ

    }

}
