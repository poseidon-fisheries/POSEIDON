/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.*;

import static java.util.stream.Collectors.toCollection;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class BiomassDrivenTimeSeriesExogenousCatchesFactory
    implements AlgorithmFactory<BiomassDrivenTimeSeriesExogenousCatches> {

    private IntegerParameter startingYear;
    private InputPath catchesFile; // = Paths.get("inputs", "tuna", "exogenous_catches.csv");
    private AlgorithmFactory<SpeciesCodes> speciesCodesSupplier;
    private boolean fadMortality = false;

    @SuppressWarnings("unused")
    public BiomassDrivenTimeSeriesExogenousCatchesFactory() {
    }

    public BiomassDrivenTimeSeriesExogenousCatchesFactory(
        final AlgorithmFactory<SpeciesCodes> speciesCodesSupplier,
        final InputPath catchesFile,
        final IntegerParameter startingYear,
        final boolean fadMortalityIncluded
    ) {
        this.speciesCodesSupplier = speciesCodesSupplier;
        this.catchesFile = catchesFile;
        this.startingYear = startingYear;
        this.fadMortality = fadMortalityIncluded;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<SpeciesCodes> getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    public void setSpeciesCodesSupplier(final AlgorithmFactory<SpeciesCodes> speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getStartingYear() {
        return startingYear;
    }

    @SuppressWarnings("unused")
    public void setStartingYear(final IntegerParameter startingYear) {
        this.startingYear = startingYear;
    }

    @SuppressWarnings("unused")
    public InputPath getCatchesFile() {
        return catchesFile;
    }

    public void setCatchesFile(final InputPath catchesFile) {
        this.catchesFile = catchesFile;
    }

    /**
     * Reads from catchesFile to build the map of exogenous catches tobe passed to the
     * BiomassDrivenTimeSeriesExogenousCatches constructor. Since we don't want to depend on the input file having the
     * years in the right order, we first use sorted maps from year to catches as values in the main map and then
     * convert the properly ordered values of these inner maps to linked lists of catches.
     */
    @Override
    public BiomassDrivenTimeSeriesExogenousCatches apply(final FishState fishState) {
        final Map<Species, SortedMap<Integer, Quantity<Mass>>> catchesBySpecies = new HashMap<>();
        final SpeciesCodes speciesCodes = speciesCodesSupplier.apply(fishState);
        recordStream(catchesFile.get()).forEach(record -> {
            final Integer year = record.getInt("year");
            if (year >= startingYear.getValue()) {
                final String speciesName = speciesCodes.getSpeciesName(record.getString("species_code"));
                final Species species = fishState.getSpecies(speciesName);
                catchesBySpecies
                    .computeIfAbsent(species, __ -> new TreeMap<>())
                    .put(year, getQuantity(record.getDouble("catches_in_tonnes"), TONNE));
            }
        });
        final LinkedHashMap<Species, Queue<Double>> catchesTimeSeries = new LinkedHashMap<>();
        catchesBySpecies.forEach((species, catches) ->
            catchesTimeSeries.put(
                species,
                catches.values().stream().map(q -> asDouble(q, KILOGRAM)).collect(toCollection(LinkedList::new))
            )
        );
        return new BiomassDrivenTimeSeriesExogenousCatches(catchesTimeSeries, fadMortality);
    }

    /**
     * Getter for property 'fadMortality'.
     *
     * @return Value for property 'fadMortality'.
     */
    @SuppressWarnings("unused")
    public boolean isFadMortality() {
        return fadMortality;
    }

    /**
     * Setter for property 'fadMortality'.
     *
     * @param fadMortality Value to set for property 'fadMortality'.
     */
    @SuppressWarnings("unused")
    public void setFadMortality(final boolean fadMortality) {
        this.fadMortality = fadMortality;
    }
}
