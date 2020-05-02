package uk.ac.ox.oxfish.biology.boxcars;

import com.google.common.base.Preconditions;
import org.apache.commons.collections.list.UnmodifiableList;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * tries to keep a precise number of boats for each tag in the sample
 */
public class CatchSamplerFixedSample implements CatchSampler, Steppable {



    private final LinkedHashMap<String,Integer> numberOfSamplesPerTag = new LinkedHashMap<>();

    private final HashSet<Fisher> observedFishers = new HashSet<>();

    private final CatchSample delegate;
    private Stoppable receipt;

    public CatchSamplerFixedSample(
            LinkedHashMap<String,Integer> numberOfSamplesPerTag,
            Species species) {
        this.delegate = new CatchSample(species,
                new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()]);

            numberOfSamplesPerTag.forEach(
                    CatchSamplerFixedSample.this.numberOfSamplesPerTag::put);
    }




    @Override
    public void observeDaily() {
        delegate.observeDaily(observedFishers);
    }

    @Override
    public void resetCatchObservations() {
            delegate.resetCatchObservations();
    }

    @Override
    public double[][] getAbundance() {
        return  delegate.getAbundance();
    }

    @Override
    public double[][] getAbundance(Function<Pair<Integer, Integer>, Double> subdivisionBinToWeightFunction) {
        return delegate.getAbundance(subdivisionBinToWeightFunction);
    }

    @Override
    public Species getSpecies() {
        return  delegate.getSpecies();
    }

    @Override
    public double[][] getLandings() {
        return delegate.getLandings();
    }

    @Override
    public List<Fisher> viewObservedFishers() {
        return Collections.unmodifiableList(new LinkedList<>(observedFishers));
    }


    @Override
    public void start(FishState model) {

        step(model);
        receipt = model.scheduleEveryYear(this, StepOrder.DAWN);


    }


    /**
     * builds the list of fishers to observe
     * @param model the model
     */
    private void checkWhichFisherToObserve(FishState model){

        observedFishers.clear();

        for (Map.Entry<String, Integer> tagToSample : numberOfSamplesPerTag.entrySet()) {



            model.getFishers().stream().
                    //ignore fishers that quit

                            filter(

                            fisher -> model.getYear()== 0 || fisher.hasBeenActiveThisYear()

                    ).
                    //pick only the right tag
                    filter(
                            fisher -> fisher.getTags().contains(tagToSample.getKey())
                    ).
                    //shuffle them
                    sorted((fisher, t1) -> model.getRandom().nextInt(3)-1).
                    //pick only first x
                    limit(tagToSample.getValue()).
                    //add them list of fishers
                    forEach(
                    fisher -> observedFishers.add(fisher)
            );




        }




    }



    @Override
    public void step(SimState simState) {
        checkWhichFisherToObserve(((FishState) simState));
    }

    @Override
    public void turnOff() {
        if(receipt!=null)
            receipt.stop();
    }
}
