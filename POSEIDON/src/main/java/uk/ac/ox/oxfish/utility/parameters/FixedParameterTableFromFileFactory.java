package uk.ac.ox.oxfish.utility.parameters;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import static com.google.common.collect.ImmutableTable.toImmutableTable;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class FixedParameterTableFromFileFactory implements AlgorithmFactory<FixedParameterTable> {
    private InputPath parameterFile;

    @SuppressWarnings("unused")
    public FixedParameterTableFromFileFactory() {
    }

    public FixedParameterTableFromFileFactory(final InputPath parameterFile) {
        this.parameterFile = parameterFile;
    }

    @SuppressWarnings("unused")
    public InputPath getParameterFile() {
        return parameterFile;
    }

    @SuppressWarnings("unused")
    public void setParameterFile(final InputPath parameterFile) {
        this.parameterFile = parameterFile;
    }

    @Override
    public FixedParameterTable apply(final FishState fishState) {
        return new FixedParameterTable(
            recordStream(parameterFile.get()).collect(toImmutableTable(
                r -> r.getInt("year"),
                r -> r.getString("name"),
                r -> new FixedDoubleParameter(r.getDouble("value"))
            ))
        );
    }
}
