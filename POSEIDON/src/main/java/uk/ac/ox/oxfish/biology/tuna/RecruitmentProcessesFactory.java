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

package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.complicated.TunaMeristics;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

/**
 * This will create the {@link RecruitmentProcess} objects to be used by the {@link
 * ScheduledAbundanceProcessesFactory}. It will read the parameters from the provided CSV file.
 */
public class RecruitmentProcessesFactory
    implements AlgorithmFactory<Map<Species, ? extends RecruitmentProcess>> {

    private Supplier<SpeciesCodes> speciesCodesSupplier;
    private GlobalBiology globalBiology;
    private InputPath recruitmentParametersFile;

    /**
     * Empty constructor for YAML construction
     */
    public RecruitmentProcessesFactory() {
    }

    public RecruitmentProcessesFactory(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final InputPath recruitmentParametersFile
    ) {
        this.speciesCodesSupplier = speciesCodesSupplier;
        this.recruitmentParametersFile = checkNotNull(recruitmentParametersFile);
    }

    public Supplier<SpeciesCodes> getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    public void setSpeciesCodesSupplier(final Supplier<SpeciesCodes> speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    public void setGlobalBiology(final GlobalBiology globalBiology) {
        this.globalBiology = globalBiology;
    }

    @SuppressWarnings("unused")
    public InputPath getRecruitmentParametersFile() {
        return recruitmentParametersFile;
    }

    @SuppressWarnings("unused")
    public void setRecruitmentParametersFile(final InputPath recruitmentParametersFile) {
        this.recruitmentParametersFile = recruitmentParametersFile;
    }

    @Override
    public Map<Species, ? extends RecruitmentProcess> apply(final FishState fishState) {
        checkNotNull(globalBiology);
        final SpeciesCodes speciesCodes = speciesCodesSupplier.get();
        return recordStream(recruitmentParametersFile.get())
            .map(record -> {
                final Species species = speciesCodes.getSpeciesFromCode(
                    globalBiology,
                    record.getString("species_code")
                );
                final float r0 = record.getFloat("R0");
                //noinspection UnstableApiUsage
                return entry(species, new RecruitmentBySpawningBiomass(
                    Math.round(r0),
                    record.getDouble("h_steepness"),
                    record.getDouble("virgin_ssb") / r0,
                    false,
                    ((TunaMeristics) species.getMeristics()).getMaturity().toArray(),
                    null,
                    FEMALE,
                    false
                ));
            })
            .collect(toImmutableMap(Entry::getKey, Entry::getValue));
    }
}
