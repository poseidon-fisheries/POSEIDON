package uk.ac.ox.oxfish.model.event;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.stream.Collectors.toCollection;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class BiomassDrivenTimeSeriesExogenousCatchesFactory
    implements AlgorithmFactory<BiomassDrivenTimeSeriesExogenousCatches> {

    private int startingYear = 2000;
    private Path catchesFile = Paths.get("inputs", "tuna", "exogenous_catches.csv");
    private SpeciesCodes speciesCodes;
    private boolean fadMortality = false;

    @SuppressWarnings("unused")
    public BiomassDrivenTimeSeriesExogenousCatchesFactory() {}

    public BiomassDrivenTimeSeriesExogenousCatchesFactory(
        Path catchesFile,
        int startingYear,
        boolean fadMortalityIncluded
    ) {
        this.catchesFile = catchesFile;
        this.startingYear = startingYear;
        this.fadMortality = fadMortalityIncluded;
    }

    @SuppressWarnings("unused")
    public SpeciesCodes getSpeciesCodes() {
        return speciesCodes;
    }

    public void setSpeciesCodes(SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    @SuppressWarnings("unused")
    public int getStartingYear() { return startingYear; }

    @SuppressWarnings("unused")
    public void setStartingYear(int startingYear) { this.startingYear = startingYear; }

    @SuppressWarnings("unused")
    public Path getCatchesFile() { return catchesFile; }

    public void setCatchesFile(Path catchesFile) { this.catchesFile = catchesFile; }

    /**
     * Reads from catchesFile to build the map of exogenous catches tobe passed to the
     * BiomassDrivenTimeSeriesExogenousCatches constructor. Since we don't want to depend on the input file
     * having the years in the right order, we first use sorted maps from year to catches as values in the
     * main map and then convert the properly ordered values of these inner maps to linked lists of catches.
     */
    @Override
    public BiomassDrivenTimeSeriesExogenousCatches apply(FishState fishState) {
        Map<Species, SortedMap<Integer, Quantity<Mass>>> catchesBySpecies = new HashMap<>();
        recordStream(catchesFile).forEach(record -> {
            final Integer year = record.getInt("year");
            if (year >= startingYear) {
                String speciesName = speciesCodes.getSpeciesName(record.getString("species_code"));
                Species species = fishState.getSpecies(speciesName);
                catchesBySpecies
                    .computeIfAbsent(species, __ -> new TreeMap<>())
                    .put(year, getQuantity(record.getDouble("catches_in_tonnes"), TONNE));
            }
        });
        LinkedHashMap<Species, Queue<Double>> catchesTimeSeries = new LinkedHashMap<>();
        catchesBySpecies.forEach((species, catches) ->
            catchesTimeSeries.put(species,
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
    public void setFadMortality(boolean fadMortality) {
        this.fadMortality = fadMortality;
    }
}
