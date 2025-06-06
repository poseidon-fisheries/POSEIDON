/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

package uk.ac.ox.oxfish.maximization.generic;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.growers.CommonLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.MultipleIndependentSpeciesBiomassFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBiomassFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleOptimizationParameterTest {

    @Test
    public void twoStepsWork() throws IllegalAccessException, NoSuchMethodException,
        InvocationTargetException {
        final PrototypeScenario scenario = new PrototypeScenario();
        final FromLeftToRightFactory biologyInitializer = new FromLeftToRightFactory();

        biologyInitializer.setExponent(new FixedDoubleParameter(100));
        scenario.setBiologyInitializer(biologyInitializer);

        assertEquals(
            ((FixedDoubleParameter) biologyInitializer.getExponent()).getValue(),
            100,
            .0001
        );

        final SimpleOptimizationParameter parameter = new SimpleOptimizationParameter(
            "biologyInitializer.exponent",
            0, 200
        );

        parameter.parametrize(
            scenario,
            new double[]{10}
        );

        assertEquals(
            ((FixedDoubleParameter) biologyInitializer.getExponent()).getValue(),
            200,
            .0001
        );
    }

    @Test
    public void oneStepWork() throws IllegalAccessException, NoSuchMethodException,
        InvocationTargetException {
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setSpeedInKmh(new FixedDoubleParameter(150));

        assertEquals(((FixedDoubleParameter) scenario.getSpeedInKmh()).getValue(), 150, .0001);

        final SimpleOptimizationParameter parameter = new SimpleOptimizationParameter(
            "speedInKmh",
            0, 20
        );

        parameter.parametrize(
            scenario,
            new double[]{0}
        );

        assertEquals(((FixedDoubleParameter) scenario.getSpeedInKmh()).getValue(), 10, .0001);
    }

    @Test
    public void indexedWorks() throws IllegalAccessException, NoSuchMethodException,
        InvocationTargetException {
        final PrototypeScenario scenario = new PrototypeScenario();
        final MultipleIndependentSpeciesBiomassFactory biology =
            new MultipleIndependentSpeciesBiomassFactory();
        scenario.setBiologyInitializer(biology);
        biology.getFactories().clear();
        final SingleSpeciesBiomassFactory first = new SingleSpeciesBiomassFactory();
        final SingleSpeciesBiomassFactory second = new SingleSpeciesBiomassFactory();
        biology.getFactories().add(first);
        biology.getFactories().add(second);

        first.setGrower(new CommonLogisticGrowerFactory(.567));
        second.setGrower(new CommonLogisticGrowerFactory(.567));

        assertEquals(
            ((FixedDoubleParameter) ((CommonLogisticGrowerFactory) first.getGrower()).getSteepness()).getValue(),
            .567,
            .0001
        );
        assertEquals(
            ((FixedDoubleParameter) ((CommonLogisticGrowerFactory) second.getGrower()).getSteepness()).getValue(),
            .567,
            .0001
        );

        final SimpleOptimizationParameter parameter = new SimpleOptimizationParameter(
            "biologyInitializer.factories[1].grower.steepness",
            10, 20
        );

        parameter.parametrize(
            scenario,
            new double[]{-10}
        );

        OptimizationParameter.navigateAndSet(
            scenario,
            "biologyInitializer.factories[1].grower.steepness",
            new FixedDoubleParameter(10)
        );

        assertEquals(
            ((FixedDoubleParameter) ((CommonLogisticGrowerFactory) first.getGrower()).getSteepness()).getValue(),
            .567,
            .0001
        );
        assertEquals(
            ((FixedDoubleParameter) ((CommonLogisticGrowerFactory) second.getGrower()).getSteepness()).getValue(),
            10,
            .0001
        );
    }

    @Test
    void computeMappedValue() {
        final SimpleOptimizationParameter parameter =
            new SimpleOptimizationParameter(null, -100, 100);
        assertEquals(-20, parameter.computeMappedValue(-200));
        assertEquals(-10, parameter.computeMappedValue(-100));
        assertEquals(0, parameter.computeMappedValue(0));
        assertEquals(10, parameter.computeMappedValue(100));
        assertEquals(20, parameter.computeMappedValue(200));
    }

    @Test
    void computeRandomMappedValues() {
        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final double min = rng.nextGaussian();
        final SimpleOptimizationParameter parameter =
            new SimpleOptimizationParameter(null, min, min + rng.nextDouble());
        DoubleStream
            .generate(rng::nextGaussian)
            .limit(100)
            .forEach(v -> assertEquals(
                v,
                parameter.computeNumericValue(parameter.computeMappedValue(v)),
                1E-10
            ));
    }
}
