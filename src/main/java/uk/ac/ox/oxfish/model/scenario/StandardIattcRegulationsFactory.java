/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.scenario;

import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader.chooseClosurePeriod;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.dayOfYear;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.input;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;
import uk.ac.ox.oxfish.model.regs.factory.MultipleRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.regs.factory.SpecificProtectedAreaFromCoordinatesFactory;
import uk.ac.ox.oxfish.model.regs.factory.SpecificProtectedAreaFromShapeFileFactory;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class StandardIattcRegulationsFactory extends MultipleRegulationsFactory {

    public static final AlgorithmFactory<TemporaryRegulation> closureAReg =
        new TemporaryRegulationFactory(
            dayOfYear(JULY, 29), dayOfYear(OCTOBER, 8),
            new NoFishingFactory()
        );

    public static final AlgorithmFactory<TemporaryRegulation> closureBReg =
        new TemporaryRegulationFactory(
            dayOfYear(NOVEMBER, 9), dayOfYear(JANUARY, 19),
            new NoFishingFactory()
        );

    public static final AlgorithmFactory<TemporaryRegulation> elCorralitoReg =
        new TemporaryRegulationFactory(
            dayOfYear(OCTOBER, 9), dayOfYear(NOVEMBER, 8),
            new SpecificProtectedAreaFromCoordinatesFactory(4, -110, -3, -96)
        );

    private static final Path GALAPAGOS_EEZ_SHAPE_FILE =
        input("galapagos_eez").resolve("eez.shp");

    public static final AlgorithmFactory<SpecificProtectedArea> galapagosEezReg =
        new SpecificProtectedAreaFromShapeFileFactory(GALAPAGOS_EEZ_SHAPE_FILE);

    public StandardIattcRegulationsFactory() {
        super(ImmutableMap.of(
            galapagosEezReg, TAG_FOR_ALL,
            elCorralitoReg, TAG_FOR_ALL,
            closureAReg, "closure A",
            closureBReg, "closure B"
        ));
    }

    public static void scheduleClosurePeriodChoice(final FishState model, final Fisher fisher) {
        // Every year, on July 15th, purse seine vessels must choose which temporal closure
        // period they will observe.
        final int daysFromNow = 1 + dayOfYear(JULY, 15);
        final Steppable assignClosurePeriod = simState -> {
            if (fisher.getRegulation() instanceof MultipleRegulations) {
                chooseClosurePeriod(fisher, model.getRandom());
                ((MultipleRegulations) fisher.getRegulation()).reassignRegulations(model, fisher);
            }
        };
        model.scheduleOnceInXDays(assignClosurePeriod, StepOrder.DAWN, daysFromNow);
        model.scheduleOnceInXDays(
            simState -> model.scheduleEveryXDay(assignClosurePeriod, StepOrder.DAWN, 365),
            StepOrder.DAWN,
            daysFromNow
        );
    }

}
