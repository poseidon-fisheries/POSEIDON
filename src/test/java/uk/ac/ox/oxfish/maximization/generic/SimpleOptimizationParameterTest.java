/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.maximization.generic;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.growers.CommonLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.MultipleIndependentSpeciesBiomassFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBiomassFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class SimpleOptimizationParameterTest {


    @Test
    public void twoStepsWork() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrototypeScenario scenario  = new PrototypeScenario();
        FromLeftToRightFactory biologyInitializer = new FromLeftToRightFactory();

        biologyInitializer.setExponent(new FixedDoubleParameter(100));
        scenario.setBiologyInitializer(biologyInitializer);


        assertEquals(((FixedDoubleParameter) biologyInitializer.getExponent()).getFixedValue(),
                     100,.0001);

        SimpleOptimizationParameter parameter = new SimpleOptimizationParameter(
                "biologyInitializer.exponent",
                0,200
        );

        parameter.parametrize(scenario,
                              new double[]{10});


        assertEquals(((FixedDoubleParameter) biologyInitializer.getExponent()).getFixedValue(),
                     200,.0001);
    }

    @Test
    public void oneStepWork() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrototypeScenario scenario  = new PrototypeScenario();
        scenario.setSpeedInKmh(new FixedDoubleParameter(150));


        assertEquals(((FixedDoubleParameter) scenario.getSpeedInKmh()).getFixedValue(),
                     150,.0001);

        SimpleOptimizationParameter parameter = new SimpleOptimizationParameter(
                "speedInKmh",
                0,20
        );

        parameter.parametrize(scenario,
                              new double[]{0});

        assertEquals(((FixedDoubleParameter) scenario.getSpeedInKmh()).getFixedValue(),
                     10,.0001);
    }

    @Test
    public void indexedWorks() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrototypeScenario scenario  = new PrototypeScenario();
        MultipleIndependentSpeciesBiomassFactory biology = new MultipleIndependentSpeciesBiomassFactory();
        scenario.setBiologyInitializer(biology);
        biology.getFactories().clear();
        SingleSpeciesBiomassFactory first = new SingleSpeciesBiomassFactory();
        SingleSpeciesBiomassFactory second = new SingleSpeciesBiomassFactory();
        biology.getFactories().add(first);
        biology.getFactories().add(second);

        first.setGrower(new CommonLogisticGrowerFactory(.567));
        second.setGrower(new CommonLogisticGrowerFactory(.567));


        assertEquals(((FixedDoubleParameter) ((CommonLogisticGrowerFactory) first.getGrower()).getSteepness()).getFixedValue(),
                     .567,.0001);
        assertEquals(((FixedDoubleParameter) ((CommonLogisticGrowerFactory) second.getGrower()).getSteepness()).getFixedValue(),
                     .567,.0001);


        SimpleOptimizationParameter parameter = new SimpleOptimizationParameter(
                "biologyInitializer.factories$1.grower.steepness",
                10,20
        );

        parameter.parametrize(scenario,
                              new double[]{-10});

        OptimizationParameter.navigateAndSet(scenario,
                                             "biologyInitializer.factories$1.grower.steepness",
                                             new FixedDoubleParameter(10));


        assertEquals(((FixedDoubleParameter) ((CommonLogisticGrowerFactory) first.getGrower()).getSteepness()).getFixedValue(),
                     .567,.0001);
        assertEquals(((FixedDoubleParameter) ((CommonLogisticGrowerFactory) second.getGrower()).getSteepness()).getFixedValue(),
                     10,.0001);
    }


}