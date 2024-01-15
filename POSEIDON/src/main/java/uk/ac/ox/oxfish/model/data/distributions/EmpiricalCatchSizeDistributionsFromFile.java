package uk.ac.ox.oxfish.model.data.distributions;

import com.google.common.primitives.Doubles;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class EmpiricalCatchSizeDistributionsFromFile implements AlgorithmFactory<EmpiricalDistributions> {

    private static final CacheByFile<EmpiricalDistributions> cache =
        new CacheByFile<>(path -> new MapBasedEmpiricalDistributions(
            recordStream(path).collect(groupingBy(
                record -> record.getInt("year"),
                groupingBy(
                    record -> record.getString("species_code"),
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
    public EmpiricalDistributions apply(final FishState fishState) {
        return cache.apply(path.get());
    }
}
