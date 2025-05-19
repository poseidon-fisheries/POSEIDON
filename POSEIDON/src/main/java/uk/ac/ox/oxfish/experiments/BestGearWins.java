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

import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.PeriodicUpdateFromListFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ServiceLoader;

import static com.google.common.collect.Streams.stream;

/**
 * Testing that imitation gets the best gear Created by carrknight on 8/5/15.
 */
public class BestGearWins {

    public static void main(final String[] args) throws IOException {

        Path root = Paths.get("runs", "ltr");

        root.toFile().mkdirs();
        for (int i = 0; i < 100; i++) {
            final DataColumn column = efficiencyImitation(
                .05,
                20,
                "From Left To Right Fixed",
                System.currentTimeMillis()
            );
            final File file = root.resolve("pricey" + i + ".csv").toFile();
            final FileWriter writer = new FileWriter(file);
            for (final Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }
        for (int i = 0; i < 100; i++) {
            final DataColumn column = efficiencyImitation(
                0,
                20,
                "From Left To Right Fixed",
                System.currentTimeMillis()
            );
            final File file = root.resolve("free" + i + ".csv").toFile();
            final FileWriter writer = new FileWriter(file);
            for (final Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }

        root = Paths.get("runs", "logistic");
        root.toFile().mkdirs();
        for (int i = 0; i < 100; i++) {
            final DataColumn column = efficiencyImitation(
                .05,
                20,
                "Independent Logistic",
                System.currentTimeMillis()
            );
            final File file = root.resolve("pricey" + i + ".csv").toFile();
            final FileWriter writer = new FileWriter(file);
            for (final Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }
        for (int i = 0; i < 100; i++) {
            final DataColumn column = efficiencyImitation(
                0,
                20,
                "Independent Logistic",
                System.currentTimeMillis()
            );
            final File file = root.resolve("free" + i + ".csv").toFile();
            final FileWriter writer = new FileWriter(file);
            for (final Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }

        root = Paths.get("runs", "marginal");
        root.toFile().mkdirs();
        for (int i = 0; i < 100; i++) {
            final DataColumn column = efficiencyImitation(
                .2,
                20,
                "Independent Logistic",
                System.currentTimeMillis()
            );
            final File file = root.resolve("pricey" + i + ".csv").toFile();
            final FileWriter writer = new FileWriter(file);
            for (final Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }
        for (int i = 0; i < 100; i++) {
            final DataColumn column = efficiencyImitation(
                .01,
                20,
                "Independent Logistic",
                System.currentTimeMillis()
            );
            final File file = root.resolve("free" + i + ".csv").toFile();
            final FileWriter writer = new FileWriter(file);
            for (final Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }

    }

    @SuppressWarnings("unchecked")
    public static DataColumn efficiencyImitation(
        final double gasPrice,
        final int simulationYears,
        final String biologyInitializer,
        final long seed
    ) {

        // without fuel cost:
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(
            (AlgorithmFactory<? extends BiologyInitializer>)
                stream(ServiceLoader.load(FactorySupplier.class))
                    .filter(factorySupplier ->
                        factorySupplier.getFactoryName().equals(biologyInitializer)
                    )
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(MessageFormat.format(
                        "Cannot find biology initializer: \"{0}\"",
                        biologyInitializer
                    )))
                    .get()
        );

        scenario.setFishers(100);
        scenario.setGasPricePerLiter(new FixedDoubleParameter(gasPrice));
        RandomCatchabilityTrawlFactory gear = new RandomCatchabilityTrawlFactory();
        gear.setGasPerHourFished(new FixedDoubleParameter(10));
        scenario.setGear(gear);
        final PeriodicUpdateFromListFactory gearStrategy = new PeriodicUpdateFromListFactory();
        gearStrategy.setProbability(new FixedProbabilityFactory(.05, .25));
        gearStrategy.setYearly(false);
        for (int i = 0; i < 20; i++) {
            gear = new RandomCatchabilityTrawlFactory();
            gear.setGasPerHourFished(new FixedDoubleParameter(i));
            gearStrategy.getAvailableGears().add(gear);
        }
        scenario.setGearStrategy(gearStrategy);
        // start everything
        final FishState state = new FishState(seed);
        state.setScenario(scenario);
        state.start();

        state.getDailyDataSet().registerGatherer("Thrawling Fuel Consumption", model -> {
            final double size = state.getFishers().size();
            if (size == 0)
                return Double.NaN;
            else {
                double total = 0;
                for (final Fisher fisher1 : state.getFishers())
                    total += ((RandomCatchabilityTrawl) fisher1.getGear()).getGasPerHourFished();
                return total / size;
            }
        }, Double.NaN);

        for (int i = 0; i < state.getSpecies().size(); i++) {
            final int finalI = i;
            state.getDailyDataSet().registerGatherer("Trawling Efficiency for Species " + i,
                model -> {
                    final double size = state.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (final Fisher fisher1 : state.getFishers())
                            total +=
                                ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[finalI];
                        return total / size;
                    }
                }, Double.NaN
            );
        }

        // pre-lspiRun average efficiency
        double average = 0;
        for (final Fisher fisher : state.getFishers()) {
            average += ((RandomCatchabilityTrawl) fisher.getGear()).getGasPerHourFished();
        }
        average /= 100;
        //     System.out.println(average);

        while (state.getYear() < simulationYears)
            state.schedule.step(state);

        state.schedule.step(state);
        // average now? Ought to be more or less the same
        average = 0;
        for (final Fisher fisher : state.getFishers()) {
            average += ((RandomCatchabilityTrawl) fisher.getGear()).getGasPerHourFished();
        }
        average /= 100;
        System.out.println(average);
        System.out.println(state
            .getDailyDataSet()
            .getColumn("Thrawling Fuel Consumption")
            .getLatest());
        return state.getDailyDataSet().getColumn("Thrawling Fuel Consumption");

    }
}
