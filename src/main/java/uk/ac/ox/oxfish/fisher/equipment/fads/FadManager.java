package uk.ac.ox.oxfish.fisher.equipment.fads;

import ec.util.MersenneTwisterFast;
import org.apache.commons.collections15.set.ListOrderedSet;
import sim.util.Bag;
import sim.util.Double2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

public class FadManager {

    private final FadMap fadMap;
    private final ListOrderedSet<Fad> deployedFads = new ListOrderedSet<>();
    private FadInitializer fadInitializer;
    private Fisher fisher;
    private int numFadsInStock;

    public FadManager(FadMap fadMap, FadInitializer fadInitializer, int numFadsInStock) {
        this.fadInitializer = fadInitializer;
        checkArgument(numFadsInStock >= 0);
        this.numFadsInStock = numFadsInStock;
        this.fadMap = fadMap;
    }

    public Fisher getFisher() { return fisher; }

    public void setFisher(Fisher fisher) {
        this.fisher = fisher;
    }

    public ListOrderedSet<Fad> getDeployedFads() { return deployedFads; }

    public FadMap getFadMap() { return fadMap; }

    public boolean anyFadsHere() { return !getFadsHere().isEmpty(); }

    public Bag getFadsHere() {
        checkNotNull(fisher);
        return fadMap.fadsAt(fisher.getLocation());
    }

    Optional<Fad> oneOfFadsHere() { return oneOf(getFadsHere(), fisher.grabRandomizer()); }

    public int getNumFadsInStock() { return numFadsInStock; }

    public void loseFad(Fad fad) {
        checkArgument(deployedFads.contains(fad));
        deployedFads.remove(fad);
    }

    /**
     *  Deploys a FAD at a random position in the given sea tile
     */
    public Fad deployFad(SeaTile seaTile, MersenneTwisterFast random) {
        return deployFad(new Double2D(
            seaTile.getGridX() + random.nextDouble(),
            seaTile.getGridY() + random.nextDouble()
        ));
    }

    private Fad deployFad(Double2D location) {
        checkState(numFadsInStock >= 1);
        numFadsInStock--;
        final Fad newFad = fadInitializer.apply(this);
        fadMap.deployFad(newFad, location);
        deployedFads.add(newFad);
        return newFad;
    }

    public void pickUpFad(Fad fad) {
        fadMap.remove(fad);
        numFadsInStock++;
    }

}
