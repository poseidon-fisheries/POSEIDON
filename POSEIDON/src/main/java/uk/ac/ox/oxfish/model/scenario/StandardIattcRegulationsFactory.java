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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;
import uk.ac.ox.oxfish.model.regs.factory.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import static java.time.Month.*;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.dayOfYear;

public class StandardIattcRegulationsFactory implements AlgorithmFactory<MultipleRegulations> {

    public static final AlgorithmFactory<TemporaryRegulation> CLOSURE_A_REG =
        new TemporaryRegulationFactory(
            dayOfYear(JULY, 29), dayOfYear(OCTOBER, 8),
            new NoFishingFactory()
        );
    public static final AlgorithmFactory<TemporaryRegulation> CLOSURE_B_REG =
        new TemporaryRegulationFactory(
            dayOfYear(NOVEMBER, 9), dayOfYear(JANUARY, 19),
            new NoFishingFactory()
        );
    public static final AlgorithmFactory<TemporaryRegulation> EL_CORRALITO_REG =
        new TemporaryRegulationFactory(
            dayOfYear(OCTOBER, 9), dayOfYear(NOVEMBER, 8),
            new SpecificProtectedAreaFromCoordinatesFactory(4, -110, -3, -96)
        );

    private AlgorithmFactory<TemporaryRegulation> closureAReg = CLOSURE_A_REG;
    private AlgorithmFactory<TemporaryRegulation> closureBReg = CLOSURE_B_REG;
    private AlgorithmFactory<TemporaryRegulation> elCorralitoReg = EL_CORRALITO_REG;
    private ProtectedAreasFromFolderFactory protectedAreasFromFolderFactory;

    StandardIattcRegulationsFactory() {
    }

    public StandardIattcRegulationsFactory(final ProtectedAreasFromFolderFactory protectedAreasFromFolderFactory) {
        this.protectedAreasFromFolderFactory = protectedAreasFromFolderFactory;
    }

    public AlgorithmFactory<TemporaryRegulation> getClosureAReg() {
        return closureAReg;
    }

    @SuppressWarnings("unused")
    public void setClosureAReg(AlgorithmFactory<TemporaryRegulation> closureAReg) {
        this.closureAReg = closureAReg;
    }

    public AlgorithmFactory<TemporaryRegulation> getClosureBReg() {
        return closureBReg;
    }

    @SuppressWarnings("unused")
    public void setClosureBReg(AlgorithmFactory<TemporaryRegulation> closureBReg) {
        this.closureBReg = closureBReg;
    }

    public AlgorithmFactory<TemporaryRegulation> getElCorralitoReg() {
        return elCorralitoReg;
    }

    @SuppressWarnings("unused")
    public void setElCorralitoReg(AlgorithmFactory<TemporaryRegulation> elCorralitoReg) {
        this.elCorralitoReg = elCorralitoReg;
    }

    public ProtectedAreasFromFolderFactory getProtectedAreasFromFolderFactory() {
        return protectedAreasFromFolderFactory;
    }

    @SuppressWarnings("unused")
    public void setProtectedAreasFromFolderFactory(ProtectedAreasFromFolderFactory protectedAreasFromFolderFactory) {
        this.protectedAreasFromFolderFactory = protectedAreasFromFolderFactory;
    }

    @Override
    public MultipleRegulations apply(FishState fishState) {
        return new CompositeMultipleRegulationsFactory(
            ImmutableList.of(
                new MultipleRegulationsFactory(
                    ImmutableMap.of(
                        getElCorralitoReg(), TAG_FOR_ALL,
                        getClosureAReg(), "closure A",
                        getClosureBReg(), "closure B"
                    )
                ),
                getProtectedAreasFromFolderFactory()
            )
        ).apply(fishState);
    }
}
