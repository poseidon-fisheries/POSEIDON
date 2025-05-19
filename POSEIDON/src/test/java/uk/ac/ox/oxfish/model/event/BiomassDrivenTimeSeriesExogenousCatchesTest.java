/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.growers.CommonLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;

public class BiomassDrivenTimeSeriesExogenousCatchesTest {


    @Test
    public void fourYears() {


        FlexibleScenario scenario = new FlexibleScenario();

        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(2));
        mapInitializer.setWidth(new FixedDoubleParameter(2));
        mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);


        //two species
        WellMixedBiologyFactory biologyInitializer = new WellMixedBiologyFactory();
        biologyInitializer.setGrower(new CommonLogisticGrowerFactory(0));
        biologyInitializer.setFirstSpeciesCapacity(new FixedDoubleParameter(5000d));
        biologyInitializer.setCapacityRatioSecondToFirst(new FixedDoubleParameter(1));

        scenario.setBiologyInitializer(biologyInitializer);

        //no fishing except for exogenous
        for (FisherDefinition fisherDefinition : scenario.getFisherDefinitions()) {
            fisherDefinition.setRegulation(new FishingSeasonFactory(0, true));
        }


        //exogenous stuff

        //3 years of landings
        final Queue<Double> exogenousLandings = new LinkedList<>();
        exogenousLandings.add(10d);
        exogenousLandings.add(20d);
        exogenousLandings.add(30d);

        final Queue<Double> exogenousLandingsSecondSpecies = new LinkedList<>();
        exogenousLandingsSecondSpecies.add(1d);
        exogenousLandingsSecondSpecies.add(2d);
        exogenousLandingsSecondSpecies.add(0d);

        scenario.setExogenousCatches(state -> {
            LinkedHashMap<Species, Queue<Double>> exogenous = new LinkedHashMap<>();
            exogenous.put(state.getSpecies().get(0), exogenousLandings);
            exogenous.put(state.getSpecies().get(1), exogenousLandingsSecondSpecies);
            return new BiomassDrivenTimeSeriesExogenousCatches(exogenous, false);
        });


        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        //because there is no regrowth, this will just drop by exactly the exogenous catches (-10-20-30-30) for species 0
        double initialBiomassFirst = state.getTotalBiomass(state.getSpecies().get(0));
        double initialBiomassSecond = state.getTotalBiomass(state.getSpecies().get(1));

        while (state.getYear() < 4)
            state.schedule.step(state);

        System.out.println(state.getYearlyDataSet().getColumn("Exogenous catches of Species 0"));
        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Exogenous catches of Species 0").get(0), 10d, .001);
        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Exogenous catches of Species 0").get(1), 20d, .001);
        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Exogenous catches of Species 0").get(2), 30d, .001);
        Assertions.assertEquals(state.getYearlyDataSet().getColumn("Exogenous catches of Species 0").get(3), 30d, .001);

        //no other landings must have occurred!
        for (int i = 0; i < 4; i++)
            Assertions.assertEquals(state.getYearlyDataSet().getColumn("Species 0 Landings").get(i), 0, .001);


        System.out.println(state.getYearlyDataSet().getColumn("Biomass Species 0"));
        System.out.println(state.getYearlyDataSet().getColumn("Species 0 Landings"));

        Assertions.assertEquals(
            state.getTotalBiomass(state.getSpecies().get(0)),
            initialBiomassFirst - 10 - 20 - 30 - 30,
            .0001
        );
        System.out.println(state.getTotalBiomass(state.getSpecies().get(0)));
        System.out.println(initialBiomassFirst);

        Assertions.assertEquals(state.getTotalBiomass(state.getSpecies().get(1)), initialBiomassSecond - 1 - 2, .0001);


    }
}
