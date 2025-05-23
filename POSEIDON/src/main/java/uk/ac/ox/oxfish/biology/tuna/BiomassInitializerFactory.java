/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.NoMovement;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.growers.FadAwareLogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.initializer.ConstantInitialBiomass;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantBiomassAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

/**
 * A factory for a {@link BiomassInitializer} that reads the Schaefer parameters from a CSV file.
 */
public class BiomassInitializerFactory
    extends BiologyInitializerFactory<BiomassLocalBiology> {

    private AlgorithmFactory<SpeciesCodes> speciesCodesSupplier;
    private InputPath schaeferParamsFile;

    @SuppressWarnings("unused")
    public BiomassInitializerFactory() {
    }

    public BiomassInitializerFactory(
        final AlgorithmFactory<Reallocator<BiomassLocalBiology>> reallocator,
        final AlgorithmFactory<SpeciesCodes> speciesCodesSupplier,
        final InputPath schaeferParamsFile
    ) {
        super(reallocator);
        this.speciesCodesSupplier = speciesCodesSupplier;
        this.schaeferParamsFile = schaeferParamsFile;
    }

    public InputPath getSchaeferParamsFile() {
        return schaeferParamsFile;
    }

    @SuppressWarnings("unused")
    public void setSchaeferParamsFile(final InputPath schaeferParamsFile) {
        this.schaeferParamsFile = schaeferParamsFile;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<SpeciesCodes> getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodesSupplier(final AlgorithmFactory<SpeciesCodes> speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    @Override
    public BiomassInitializer apply(final FishState fishState) {
        checkNotNull(
            getReallocator(),
            "setReallocator must be called before using."
        );
        final Map<String, GridAllocator> initialAllocators =
            getReallocator().apply(fishState)
                .getAllocationGrids()
                .getGrids()
                .get(0)
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    entry -> entry.getKey().getSpeciesCode(),
                    entry -> new GridAllocator(entry.getValue())
                ));

        final List<SingleSpeciesBiomassInitializer> biomassInitializers =
            makeBiomassInitializers(initialAllocators, speciesCodesSupplier.apply(fishState));

        return new BiomassInitializer(biomassInitializers);
    }

    /**
     * Creates biomass initializers by loading the relevant values from file.
     *
     * @param initialAllocators A map from species names to allocators.
     * @param speciesCodes      The object to use to map species codes to names
     */
    private List<SingleSpeciesBiomassInitializer> makeBiomassInitializers(
        final Map<String, ? extends GridAllocator> initialAllocators,
        final SpeciesCodes speciesCodes
    ) {
        return recordStream(schaeferParamsFile.get())
            .map(r -> {
                final String speciesCode = r.getString("species_code");
                final String speciesName = speciesCodes.getSpeciesName(speciesCode);
                final Double logisticGrowthRate = r.getDouble("logistic_growth_rate");
                final Quantity<Mass> carryingCapacity =
                    getQuantity(r.getDouble("carrying_capacity_in_tonnes"), TONNE);
                final Quantity<Mass> totalBiomass =
                    getQuantity(r.getDouble("total_biomass_in_tonnes"), TONNE);
                return new SingleSpeciesBiomassInitializer(
                    new ConstantInitialBiomass(asDouble(totalBiomass, KILOGRAM)),
                    initialAllocators.get(speciesCode),
                    new ConstantInitialBiomass(asDouble(carryingCapacity, KILOGRAM)),
                    new ConstantBiomassAllocator(Double.MAX_VALUE),
                    new NoMovement(),
                    speciesName,
                    speciesCode,
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
