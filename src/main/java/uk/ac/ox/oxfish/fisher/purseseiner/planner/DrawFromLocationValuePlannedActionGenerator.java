package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.SetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.MTFApache;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * this object exists to draw a location given a locationvalues object
 * and then turn that location into a planned action of any sort
 */
public abstract class DrawFromLocationValuePlannedActionGenerator<PA extends PlannedAction> {

    /**
     * here I use Nic's object on location values to use the whole reading toolchain;
     * in practice however all we need here is a mapping coords --> weight
     */
    private final SetLocationValues<? extends AbstractSetAction> originalLocationValues;


    protected final NauticalMap map;

    /**
     * the rng to use (compatible with Apache)
     */
    protected final MTFApache localRng;



    //todo
    //we can avoid ton of waste by not instantiating this every step and only
    //when there is a change in the location value deployment
    //but unfortunately it requires a bit of work with a specialized listener
    private EnumeratedDistribution<SeaTile> seatilePicker;


    public DrawFromLocationValuePlannedActionGenerator(
            SetLocationValues<? extends AbstractSetAction> originalLocationValues,
            NauticalMap map, MersenneTwisterFast random) {
        this.originalLocationValues = originalLocationValues;
        this.map = map;
        localRng = new MTFApache(random);
    }

    private void preparePicker(){

        if(originalLocationValues.getValues().size()>0) {
            List<Pair<SeaTile, Double>> valuePairs = originalLocationValues.getValues().stream().map(
                    entry -> new Pair<>(
                            map.getSeaTile(entry.getKey()),
                            entry.getValue()
                    )
            ).collect(Collectors.toList());

            //some weird inputs have 0s everywhere. They need to sum up to something other than 0 or the randomizer
            //goes in some sort of middle life crisis
            double sum = 0;
            for (Pair<SeaTile, Double> valuePair : valuePairs) {
                sum += valuePair.getSecond();
            }
            if(sum==0) {
                List<Pair<SeaTile, Double>> valuePairsNew = new LinkedList<>();
                for (Pair<SeaTile, Double> valuePair : valuePairs) {
                    valuePairsNew.add(new Pair<>(valuePair.getKey(),valuePair.getValue()+1));
                }
                valuePairs=valuePairsNew;
            }
            seatilePicker = new EnumeratedDistribution<>(localRng,
                                                         valuePairs);


        }
    }

    public void start(){
        Preconditions.checkState(originalLocationValues.hasStarted(),
                "need to start the location values first!");
        preparePicker();
    }

    abstract public PA drawNewPlannedAction();

    protected SeaTile drawNewLocation(){
        if(originalLocationValues.getValues().size()==0)
        {
//            System.out.println("WARNING: " + this + " had to draw a completely random location due to empty " +
//                    "locationValues");
            return map.getAllSeaTilesExcludingLandAsList().get(
                    localRng.nextInt(
                            map.getAllSeaTilesExcludingLandAsList().size()
                    )
            );
        }
        else
            return seatilePicker.sample();
    }

    public boolean isReady(){
        return seatilePicker != null || originalLocationValues.getValues().size()==0;
    }

}
