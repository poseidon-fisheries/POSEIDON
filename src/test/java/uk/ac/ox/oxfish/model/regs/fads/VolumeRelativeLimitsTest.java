package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.CUBIC_METRE;

public class VolumeRelativeLimitsTest {
    final VolumeRelativeLimits volumeRelativeLimits =
        new VolumeRelativeLimits(ImmutableSortedMap.of(
            0, 70,
            213, 120,
            426, 300,
            1200, 450
        ));

    @Test
    public void getLimit() {
        try {
            volumeRelativeLimits.getLimit(getQuantity(0.0, CUBIC_METRE));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        ImmutableMap.<Double, Integer>builder()
            .put(1.0, 70)
            .put(212.0, 70)
            .put(213.0, 120)
            .put(425.0, 120)
            .put(426.0, 300)
            .put(1199.0, 300)
            .put(1200.0, 450)
            .put(99999.0, 450)
            .build()
            .forEach((volume, limit) ->
                assertTrue(volumeRelativeLimits.getLimit(getQuantity(volume, CUBIC_METRE)) == limit)
            );
    }

}