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
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HeterogeneousGearFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HoldLimitingDecoratorFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.LogisticSelectivityGearFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class OptimizationParameterTest {


    @Test
    public void twoStepsWork() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrototypeScenario scenario  = new PrototypeScenario();
        FromLeftToRightFactory biologyInitializer = new FromLeftToRightFactory();

        biologyInitializer.setExponent(new FixedDoubleParameter(100));
        scenario.setBiologyInitializer(biologyInitializer);



        assertEquals(((FixedDoubleParameter) biologyInitializer.getExponent()).getFixedValue(),
                     100,.0001);

        OptimizationParameter.navigateAndSet(scenario,
                                             "biologyInitializer.exponent",
                                             new FixedDoubleParameter(200));


        assertEquals(((FixedDoubleParameter) biologyInitializer.getExponent()).getFixedValue(),
                     200,.0001);
    }

    @Test
    public void oneStepWork() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrototypeScenario scenario  = new PrototypeScenario();
        scenario.setFishers(150);


        assertEquals(scenario.getFishers(),
                     150,.0001);

        OptimizationParameter.navigateAndSet(scenario,
                                             "fishers",
                                             10);


        assertEquals(scenario.getFishers(),
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

        OptimizationParameter.navigateAndSet(scenario,
                                             "biologyInitializer.factories$1.grower.steepness",
                                             new FixedDoubleParameter(10));


        assertEquals(((FixedDoubleParameter) ((CommonLogisticGrowerFactory) first.getGrower()).getSteepness()).getFixedValue(),
                     .567,.0001);
        assertEquals(((FixedDoubleParameter) ((CommonLogisticGrowerFactory) second.getGrower()).getSteepness()).getFixedValue(),
                     10,.0001);
    }

    @Test
    public void mappedWorks() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrototypeScenario scenario  = new PrototypeScenario();
        HeterogeneousGearFactory gears = new HeterogeneousGearFactory();

        gears.gears.put("Pristipomoides multidens",
                        new LogisticSelectivityGearFactory(
                                41.9180563295419,
                                13.1000149168684,
                                0d,
                                0.01d
                        ));
        gears.gears.put("Lutjanus malabaricus",
                        new LogisticSelectivityGearFactory(
                                57.3802620814091,
                                17.0227436580102,
                                0d,
                                0.01d
                        ));
        gears.gears.put("Epinephelus areolatus",
                        new LogisticSelectivityGearFactory(
                                30.6447029784809,
                                6.46514981161133,
                                0d,
                                0.01d
                        )
        );
        gears.gears.put("Lutjanus erythropterus",
                        new LogisticSelectivityGearFactory(
                                48.532533910211,
                                19.3556829280302,
                                0d,
                                0.01d
                        )
        );

        HoldLimitingDecoratorFactory limiting = new HoldLimitingDecoratorFactory();
        limiting.setDelegate(gears);
        scenario.setGear(limiting);


        assertEquals(
                ((FixedDoubleParameter) gears.gears.get("Pristipomoides multidens").getAverageCatchability()).getFixedValue(),
                .01,
                .0001
        );
        //change catchability of multidens!
        OptimizationParameter.navigateAndSet(
                scenario,
                "gear.delegate.gears~Pristipomoides multidens.averageCatchability",
                new FixedDoubleParameter(22)
        );
        assertEquals(
                ((FixedDoubleParameter) gears.gears.get("Pristipomoides multidens").getAverageCatchability()).getFixedValue(),
                22,
                .0001
        );


    }

}
