package uk.ac.ox.oxfish.geography.fads;

import sim.util.Double2D;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import static java.util.stream.Collectors.*;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

/**
 * reads csv file, with column "day" for what day each fad gets dropped x,y for the coordinates (simulated coordinates,
 * not grid coordinates)
 */
public class ExogenousFadMakerCSVFactory implements AlgorithmFactory<AdditionalStartable>, Dummyable {

    private InputPath deploymentsFile; // = "./inputs/tests/fad_dummy_deploy.csv";

    private AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializerFactory =
        new CompressedBiomassFadInitializerFactory();

    /**
     * Empty constructor for YAML initialization
     */
    public ExogenousFadMakerCSVFactory() {
    }

    public ExogenousFadMakerCSVFactory(
        final InputPath deploymentsFile,
        final AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializerFactory
    ) {
        this.deploymentsFile = deploymentsFile;
        this.fadInitializerFactory = fadInitializerFactory;
    }

    @Override
    public AdditionalStartable apply(final FishState state) {
        return model -> {
            final ExogenousFadMaker<?, ?> maker = new ExogenousFadMaker<>(
                fadInitializerFactory.apply(model),
                recordStream(deploymentsFile.get()).collect(groupingBy(
                    record -> record.getInt("day"),
                    mapping(
                        record -> new Double2D(record.getDouble("x"), record.getDouble("y")),
                        toList()
                    )
                ))
            );
            maker.start(model);
        };
    }

    public InputPath getDeploymentsFile() {
        return deploymentsFile;
    }

    public void setDeploymentsFile(final InputPath deploymentsFile) {
        this.deploymentsFile = deploymentsFile;
    }

    public AlgorithmFactory<? extends FadInitializer<?, ?>> getFadInitializer() {
        return fadInitializerFactory;
    }

    public void setFadInitializer(final AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializer) {
        this.fadInitializerFactory = fadInitializer;
    }

    @Override
    public void useDummyData(final InputPath dummyDataFolder) {
        deploymentsFile = dummyDataFolder.path("dummy_fad_deployments.csv");
    }
}
