package uk.ac.ox.oxfish.model.data.distributions;

import com.google.common.primitives.Doubles;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import static java.util.stream.Collectors.*;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class EmpiricalCatchSizeDistributionsFromFile implements AlgorithmFactory<GroupedYearlyDistributions> {

    private static final CacheByFile<GroupedYearlyDistributions> cache =
        new CacheByFile<>(path -> new MapBasedGroupedYearlyDistributions(
            recordStream(path).collect(groupingBy(
                record -> record.getString("species_code"),
                groupingBy(
                    record -> record.getInt("year"),
                    collectingAndThen(
                        mapping(
                            record -> record.getDouble("catch_in_tonnes") * 1000,
                            toList()
                        ),
                        Doubles::toArray
                    )
                )
            ))));
    private InputPath path;

    @SuppressWarnings("unused")
    public EmpiricalCatchSizeDistributionsFromFile() {
    }

    public EmpiricalCatchSizeDistributionsFromFile(final InputPath path) {
        this.path = path;
    }

    public InputPath getPath() {
        return path;
    }

    public void setPath(final InputPath path) {
        this.path = path;
    }

    @Override
    public GroupedYearlyDistributions apply(final FishState fishState) {
        return cache.apply(path.get());
    }
}
