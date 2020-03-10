package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.ArrayList;

public class SinglePeakAllocatorFactory implements AlgorithmFactory<PyramidsAllocator> {


    private int peakX = 1;
    private int peakY = 1;
    private DoubleParameter smoothingValue = new FixedDoubleParameter(.7d);

    private int maxSpread = 6;

    private DoubleParameter peakBiomass = new FixedDoubleParameter(1);


    @Override
    public PyramidsAllocator apply(FishState fishState) {
        final ArrayList<int[]> singlePeak = Lists.newArrayList();
        singlePeak.add(new int[]{peakX,peakY});
        return new PyramidsAllocator(
                smoothingValue.apply(fishState.getRandom()),
                maxSpread,
                peakBiomass.apply(fishState.getRandom()),
                singlePeak
        );
    }

    public SinglePeakAllocatorFactory() {
    }

    public int getPeakX() {
        return peakX;
    }

    public void setPeakX(int peakX) {
        this.peakX = peakX;
    }

    public int getPeakY() {
        return peakY;
    }

    public void setPeakY(int peakY) {
        this.peakY = peakY;
    }

    public DoubleParameter getSmoothingValue() {
        return smoothingValue;
    }

    public void setSmoothingValue(DoubleParameter smoothingValue) {
        this.smoothingValue = smoothingValue;
    }

    public int getMaxSpread() {
        return maxSpread;
    }

    public void setMaxSpread(int maxSpread) {
        this.maxSpread = maxSpread;
    }

    public DoubleParameter getPeakBiomass() {
        return peakBiomass;
    }

    public void setPeakBiomass(DoubleParameter peakBiomass) {
        this.peakBiomass = peakBiomass;
    }
}
