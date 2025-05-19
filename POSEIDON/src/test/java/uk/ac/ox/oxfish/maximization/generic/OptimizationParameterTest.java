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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.growers.CommonLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.MultipleIndependentSpeciesBiomassFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBiomassFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HeterogeneousGearFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HoldLimitingDecoratorFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.LogisticSelectivityGearFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.lang.reflect.InvocationTargetException;

public class OptimizationParameterTest {

    @Test
    public void twoStepsWork() throws IllegalAccessException, NoSuchMethodException,
        InvocationTargetException {
        final PrototypeScenario scenario = new PrototypeScenario();
        final FromLeftToRightFactory biologyInitializer = new FromLeftToRightFactory();

        biologyInitializer.setExponent(new FixedDoubleParameter(100));
        scenario.setBiologyInitializer(biologyInitializer);

        Assertions.assertEquals(
            ((FixedDoubleParameter) biologyInitializer.getExponent()).getValue(),
            100,
            .0001
        );

        OptimizationParameter.navigateAndSet(
            scenario,
            "biologyInitializer.exponent",
            new FixedDoubleParameter(200)
        );

        Assertions.assertEquals(
            ((FixedDoubleParameter) biologyInitializer.getExponent()).getValue(),
            200,
            .0001
        );
    }

    @Test
    public void oneStepWork() throws IllegalAccessException, NoSuchMethodException,
        InvocationTargetException {
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(150);

        Assertions.assertEquals(scenario.getFishers(), 150, .0001);

        OptimizationParameter.navigateAndSet(
            scenario,
            "fishers",
            10
        );

        Assertions.assertEquals(scenario.getFishers(), 10, .0001);
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

        Assertions.assertEquals(
            ((FixedDoubleParameter) ((CommonLogisticGrowerFactory) first.getGrower()).getSteepness()).getValue(),
            .567,
            .0001
        );
        Assertions.assertEquals(
            ((FixedDoubleParameter) ((CommonLogisticGrowerFactory) second.getGrower()).getSteepness()).getValue(),
            .567,
            .0001
        );

        OptimizationParameter.navigateAndSet(
            scenario,
            "biologyInitializer.factories[1].grower.steepness",
            new FixedDoubleParameter(10)
        );

        Assertions.assertEquals(
            ((FixedDoubleParameter) ((CommonLogisticGrowerFactory) first.getGrower()).getSteepness()).getValue(),
            .567,
            .0001
        );
        Assertions.assertEquals(
            ((FixedDoubleParameter) ((CommonLogisticGrowerFactory) second.getGrower()).getSteepness()).getValue(),
            10,
            .0001
        );
    }

    @Test
    public void mappedWorks() throws IllegalAccessException, NoSuchMethodException,
        InvocationTargetException {
        final PrototypeScenario scenario = new PrototypeScenario();
        final HeterogeneousGearFactory gears = new HeterogeneousGearFactory();

        gears.gears.put(
            "Pristipomoides multidens",
            new LogisticSelectivityGearFactory(
                41.9180563295419,
                13.1000149168684,
                0d,
                0.01d
            )
        );
        gears.gears.put(
            "Lutjanus malabaricus",
            new LogisticSelectivityGearFactory(
                57.3802620814091,
                17.0227436580102,
                0d,
                0.01d
            )
        );
        gears.gears.put(
            "Epinephelus areolatus",
            new LogisticSelectivityGearFactory(
                30.6447029784809,
                6.46514981161133,
                0d,
                0.01d
            )
        );
        gears.gears.put(
            "Lutjanus erythropterus",
            new LogisticSelectivityGearFactory(
                48.532533910211,
                19.3556829280302,
                0d,
                0.01d
            )
        );

        final HoldLimitingDecoratorFactory limiting = new HoldLimitingDecoratorFactory();
        limiting.setDelegate(gears);
        scenario.setGear(limiting);

        Assertions.assertEquals(((FixedDoubleParameter) gears.gears.get("Pristipomoides multidens")
            .getAverageCatchability()).getValue(), .01, .0001);
        // change catchability of multidens!
        OptimizationParameter.navigateAndSet(
            scenario,
            "gear.delegate.gears(Pristipomoides multidens).averageCatchability",
            new FixedDoubleParameter(22)
        );
        Assertions.assertEquals(((FixedDoubleParameter) gears.gears.get("Pristipomoides multidens")
            .getAverageCatchability()).getValue(), 22, .0001);

    }

}
