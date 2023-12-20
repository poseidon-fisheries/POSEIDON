package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.SetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.MTFApache;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * this object exists to draw a location given a locationvalues object and then turn that location into a planned action
 * of any sort
 */
public abstract class DrawFromLocationValuePlannedActionGenerator<PA extends PlannedAction> {

    protected final NauticalMap map;
    /**
     * the rng to use (compatible with Apache)
     */
    private final MTFApache localRng;
    /**
     * here I use Nic's object on location values to use the whole reading toolchain; in practice however all we need
     * here is a mapping coords --> weight
     */
    private final SetLocationValues<?> originalLocationValues;
    // todo
    // we can avoid ton of waste by not instantiating this every step and only
    // when there is a change in the location value deployment
    // but unfortunately it requires a bit of work with a specialized listener
    private EnumeratedDistribution<SeaTile> seaTilePicker;

    DrawFromLocationValuePlannedActionGenerator(
        final SetLocationValues<?> originalLocationValues,
        final NauticalMap map,
        final MersenneTwisterFast random
    ) {
        this.originalLocationValues = originalLocationValues;
        this.map = map;
        this.localRng = new MTFApache(random);
    }

    public void start() {
        Preconditions.checkState(
            originalLocationValues.hasStarted(),
            "need to start the location values first!"
        );
        preparePicker();
    }

    private void preparePicker() {

        if (!originalLocationValues.getValues().isEmpty()) {
            List<Pair<SeaTile, Double>> valuePairs = originalLocationValues.getValues().stream().map(
                entry -> new Pair<>(
                    map.getSeaTile(entry.getKey()),
                    entry.getValue()
                )
                // avoid areas where values have turned negative
            ).filter(seaTileDoublePair -> seaTileDoublePair.getValue() >= 0).collect(Collectors.toList());
            if (valuePairs.isEmpty())
                return;

            // some weird inputs have 0s everywhere. They need to sum up to something other than 0 or the randomizer
            // goes in some sort of middle life crisis
            double sum = 0;
            for (final Pair<SeaTile, Double> valuePair : valuePairs) {
                sum += valuePair.getSecond();
            }
            if (sum == 0) {
                final List<Pair<SeaTile, Double>> valuePairsNew = new LinkedList<>();
                for (final Pair<SeaTile, Double> valuePair : valuePairs) {
                    valuePairsNew.add(new Pair<>(valuePair.getKey(), valuePair.getValue() + 1));
                }
                valuePairs = valuePairsNew;
            }
            seaTilePicker = new EnumeratedDistribution<>(
                localRng,
                valuePairs
            );

        }
    }

    abstract public PA drawNewPlannedAction();

    SeaTile drawNewLocation() {
        if (originalLocationValues.getValues().isEmpty()) {
//            System.out.println("WARNING: " + this + " had to draw a completely random location due to empty " +
//                    "locationValues");
            return map.getAllSeaTilesExcludingLandAsList().get(
                localRng.nextInt(
                    map.getAllSeaTilesExcludingLandAsList().size()
                )
            );
        } else
            return seaTilePicker.sample();
    }

    public boolean isReady() {
        return seaTilePicker != null || originalLocationValues.getValues().isEmpty();
    }

}
