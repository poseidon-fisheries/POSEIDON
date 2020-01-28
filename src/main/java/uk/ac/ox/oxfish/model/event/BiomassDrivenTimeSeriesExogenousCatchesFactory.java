package uk.ac.ox.oxfish.model.event;

import com.univocity.parsers.common.record.Record;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toCollection;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class BiomassDrivenTimeSeriesExogenousCatchesFactory
    implements AlgorithmFactory<BiomassDrivenTimeSeriesExogenousCatches> {

    private int startingYear = 2000;
    private Path catchesFile = Paths.get("inputs", "tuna", "exogenous_catches.csv");
    private BiFunction<FishState, String, Species> speciesFromCode =
        (fishState, speciesCode) -> fishState.getBiology().getSpecie(speciesCode);

    @SuppressWarnings("unused") public BiomassDrivenTimeSeriesExogenousCatchesFactory() {}

    public BiomassDrivenTimeSeriesExogenousCatchesFactory(
        Path catchesFile,
        int startingYear,
        BiFunction<FishState, String, Species> speciesFromCode
    ) {
        this.catchesFile = catchesFile;
        this.startingYear = startingYear;
        this.speciesFromCode = speciesFromCode;
    }

    /**
     * Reads from catchesFile to build the map of exogenous catches tobe passed to the
     * BiomassDrivenTimeSeriesExogenousCatches constructor. Since we don't want to depend on the input file
     * having the years in the right order, we first use sorted maps from year to catches as values in the
     * main map and then convert the properly ordered values of these inner maps to linked lists of catches.
     */
    @Override public BiomassDrivenTimeSeriesExogenousCatches apply(FishState fishState) {
        Map<Species, SortedMap<Integer, Quantity<Mass>>> catchesBySpecies = new HashMap<>();
        for (Record record : parseAllRecords(catchesFile)) {
            final Integer year = record.getInt("year");
            if (year >= startingYear) {
                final Species species = speciesFromCode.apply(fishState, record.getString("species_code"));
                catchesBySpecies
                    .computeIfAbsent(species, __ -> new TreeMap<>())
                    .put(year, getQuantity(record.getDouble("catches_in_tonnes"), TONNE));
            }
        }
        LinkedHashMap<Species, Queue<Double>> catchesTimeSeries = new LinkedHashMap<>();
        catchesBySpecies.forEach((species, catches) ->
            catchesTimeSeries.put(species,
                catches.values().stream().map(q -> asDouble(q, KILOGRAM)).collect(toCollection(LinkedList::new))
            )
        );
        return new BiomassDrivenTimeSeriesExogenousCatches(catchesTimeSeries);
    }
}
