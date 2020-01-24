package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSortedMap;

import javax.measure.Quantity;
import javax.measure.quantity.Volume;

import static com.google.common.base.Preconditions.checkArgument;

public class VolumeRelativeLimits {

    private final ImmutableSortedMap<Integer, Integer> limits;

    public VolumeRelativeLimits(ImmutableSortedMap<Integer, Integer> limits) {
        checkArgument(limits.keySet().contains(0));
        this.limits = limits;
    }

    public int getLimit(Quantity<Volume> volume) {
        return getLimit(volume.toSystemUnit().getValue().intValue());
    }

    public int getLimit(int volume) {
        checkArgument(volume > 0);
        return limits.floorEntry(volume).getValue();
    }

}
