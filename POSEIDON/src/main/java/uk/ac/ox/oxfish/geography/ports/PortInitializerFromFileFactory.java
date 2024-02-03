package uk.ac.ox.oxfish.geography.ports;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.function.Supplier;

public class PortInitializerFromFileFactory
    implements AlgorithmFactory<PortInitializer>, Supplier<PortInitializer> {

    private IntegerParameter targetYear;
    private InputPath portFile;

    public PortInitializerFromFileFactory() {
    }

    public PortInitializerFromFileFactory(
        final int targetYear,
        final InputPath portFile
    ) {
        this(new IntegerParameter(targetYear), portFile);
    }

    public PortInitializerFromFileFactory(
        final IntegerParameter targetYear,
        final InputPath portFile
    ) {
        this.targetYear = targetYear;
        this.portFile = portFile;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    public InputPath getPortFile() {
        return portFile;
    }

    public void setPortFile(final InputPath portFile) {
        this.portFile = portFile;
    }

    @Override
    public PortInitializer apply(final FishState fishState) {
        return get();
    }

    @Override
    public PortInitializer get() {
        return new PortInitializerFromFile(targetYear.getValue(), portFile.get());
    }
}
