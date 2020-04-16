package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class MirroredPyramidsAllocatorFactory implements AlgorithmFactory<MirroredPyramidsAllocator> {


    private final SinglePeakAllocatorFactory delegate = new SinglePeakAllocatorFactory();


    private DoubleParameter noiseLevel = new FixedDoubleParameter(0.1);


    @Override
    public MirroredPyramidsAllocator apply(FishState state) {
        return new MirroredPyramidsAllocator(delegate.apply(state),
                noiseLevel.apply(state.getRandom()));
    }


    public DoubleParameter getNoiseLevel() {
        return noiseLevel;
    }

    public void setNoiseLevel(DoubleParameter noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public DoubleParameter getPeakX() {
        return delegate.getPeakX();
    }

    public void setPeakX(DoubleParameter peakX) {
        delegate.setPeakX(peakX);
    }

    public DoubleParameter getPeakY() {
        return delegate.getPeakY();
    }

    public void setPeakY(DoubleParameter peakY) {
        delegate.setPeakY(peakY);
    }

    public DoubleParameter getSmoothingValue() {
        return delegate.getSmoothingValue();
    }

    public void setSmoothingValue(DoubleParameter smoothingValue) {
        delegate.setSmoothingValue(smoothingValue);
    }

    public int getMaxSpread() {
        return delegate.getMaxSpread();
    }

    public void setMaxSpread(int maxSpread) {
        delegate.setMaxSpread(maxSpread);
    }

    public DoubleParameter getPeakBiomass() {
        return delegate.getPeakBiomass();
    }

    public void setPeakBiomass(DoubleParameter peakBiomass) {
        delegate.setPeakBiomass(peakBiomass);
    }
}
