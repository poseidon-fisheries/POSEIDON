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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.input;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.NoMovement;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.growers.FadAwareLogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.initializer.BiomassReallocatorInitializer;
import uk.ac.ox.oxfish.biology.initializer.ConstantInitialBiomass;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class BiomassReallocatorInitializerFactory
    implements AlgorithmFactory<BiomassReallocatorInitializer> {

    private AlgorithmFactory<ScheduledBiomassReallocator> biomassReallocatorFactory;
    private SpeciesCodes speciesCodes = TunaScenario.speciesCodesSupplier.get();
    private Path schaeferParamsFile = input("schaefer_params.csv");

    public AlgorithmFactory<ScheduledBiomassReallocator> getBiomassReallocatorFactory() {
        return biomassReallocatorFactory;
    }

    public void setBiomassReallocatorFactory(
        final AlgorithmFactory<ScheduledBiomassReallocator> biomassReallocatorFactory
    ) {
        this.biomassReallocatorFactory = biomassReallocatorFactory;
    }

    public void setSpeciesCodes(final SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    @SuppressWarnings("unused")
    public Path getSchaeferParamsFile() {
        return schaeferParamsFile;
    }

    @SuppressWarnings("unused")
    public void setSchaeferParamsFile(final Path schaeferParamsFile) {
        this.schaeferParamsFile = schaeferParamsFile;
    }

    @Override
    public BiomassReallocatorInitializer apply(final FishState fishState) {

        final Map<String, GridAllocator> initialAllocators =
            biomassReallocatorFactory
                .apply(fishState)
                .getReallocator()
                .getAllocationGrids()
                .atStep(0)
                .map(grids -> grids.entrySet().stream()
                    .collect(toImmutableMap(
                        Entry::getKey,
                        entry -> new GridAllocator(entry.getValue())
                    )))
                .orElseThrow(() ->
                    new IllegalStateException("No allocation grids found for step 0")
                );

        final List<SingleSpeciesBiomassInitializer> biomassInitializers =
            makeBiomassInitializers(initialAllocators, speciesCodes);

        return new BiomassReallocatorInitializer(biomassInitializers);
    }

    /**
     * Creates biomass initializers by loading the relevant values from file.
     *
     * @param initialAllocators A map from species names to allocators.
     * @param speciesCodes      The object to use to map species codes to names
     */
    @NotNull
    private List<SingleSpeciesBiomassInitializer> makeBiomassInitializers(
        final Map<String, ? extends GridAllocator> initialAllocators,
        final SpeciesCodes speciesCodes
    ) {
        return parseAllRecords(schaeferParamsFile)
            .stream()
            .map(r -> {
                final String speciesName = speciesCodes.getSpeciesName(r.getString("species_code"));
                final Double logisticGrowthRate = r.getDouble("logistic_growth_rate");
                final Quantity<Mass> carryingCapacity =
                    getQuantity(r.getDouble("carrying_capacity_in_tonnes"), TONNE);
                final Quantity<Mass> totalBiomass =
                    getQuantity(r.getDouble("total_biomass_in_tonnes"), TONNE);
                return new SingleSpeciesBiomassInitializer(
                    new ConstantInitialBiomass(asDouble(totalBiomass, KILOGRAM)),
                    initialAllocators.get(speciesName),
                    new ConstantInitialBiomass(asDouble(carryingCapacity, KILOGRAM)),
                    new ConstantBiomassAllocator(Double.MAX_VALUE),
                    new NoMovement(),
                    speciesName,
                    new FadAwareLogisticGrowerInitializer(asDouble(carryingCapacity, KILOGRAM),
                        logisticGrowthRate, true
                    ),
                    false,
                    false
                );
            })
            .collect(toList());
    }
}
