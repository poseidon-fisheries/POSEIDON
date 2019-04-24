package uk.ac.ox.oxfish.fisher.equipment.fads;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

import java.util.Optional;
import org.apache.commons.collections15.set.ListOrderedSet;
import sim.util.Bag;
import sim.util.Double2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;

public class FadManager {

    private final FadMap fadMap;
    private final ListOrderedSet<Fad> deployedFads = new ListOrderedSet<>();
    private Fisher fisher;
    private int numFadsInStock;
    public FadManager(FadMap fadMap, int numFadsInStock) {
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
    private Bag getFadsHere() {
        checkNotNull(fisher);
        return fadMap.fadsAt(fisher.getLocation());
    }

    public boolean anyFadsHere() { return !getFadsHere().isEmpty(); }

    Optional<Fad> oneOfFadsHere() { return oneOf(getFadsHere(), fisher.grabRandomizer()); }

    public int getNumFadsInStock() { return numFadsInStock; }

    public void loseFad(Fad fad) {
        checkArgument(deployedFads.contains(fad));
        deployedFads.remove(fad);
    }

    public void deployFad(SeaTile seaTile) {
        deployFad(new Double2D(seaTile.getGridX(), seaTile.getGridY()));
    }

    private void deployFad(Double2D location) {
        checkState(numFadsInStock >= 1);
        numFadsInStock--;
        final Fad newFad = fadMap.deployFad(this, location);
        deployedFads.add(newFad);
    }

    public void pickUpFad(Fad fad) {
        fadMap.remove(fad);
        numFadsInStock++;
    }

}
