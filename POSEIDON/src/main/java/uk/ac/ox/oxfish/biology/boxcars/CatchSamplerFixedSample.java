package uk.ac.ox.oxfish.biology.boxcars;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * tries to keep a precise number of boats for each tag in the sample
 */
public class CatchSamplerFixedSample implements CatchAtLengthSampler, Steppable {



    private final LinkedHashMap<String,Integer> numberOfSamplesPerTag = new LinkedHashMap<>();

    private final HashSet<Fisher> observedFishers = new HashSet<>();

    private final CatchSample delegate;
    private Stoppable receipt;

    public CatchSamplerFixedSample(
            LinkedHashMap<String, Integer> numberOfSamplesPerTag,
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
        receipt = model.scheduleEveryYear(this, StepOrder.AFTER_DATA);


    }


    /**
     * builds the list of fishers to observe
     * @param model the model
     */
    private void checkWhichFisherToObserve(FishState model){

        //remove fishers who do not go out anymore from the list of observations
        final List<Fisher> stillValidFishersToObserve = observedFishers.stream().filter(
                fisher -> model.getYear() == 0 || fisher.hasBeenActiveThisYear()
        ).collect(Collectors.toList());

        observedFishers.clear();
        observedFishers.addAll(stillValidFishersToObserve);


        for (Map.Entry<String, Integer> tagToSample : numberOfSamplesPerTag.entrySet()) {

            //how many are you already monitoring?
            long currentlyContained = observedFishers.stream().filter(
                    fisher -> fisher.getTags().contains(tagToSample.getKey())
            ).count();
            //how many do you need to add to the sample?
            long shortfall = Math.max(tagToSample.getValue() - currentlyContained,0); //could go negative if the tag is shared among many populations
            if(shortfall>0)
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
                    sorted(new RandomComparator<>(model.getRandom())).
                    //pick only first x
                    limit(shortfall).
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


    private static final class RandomComparator<T> implements Comparator<T> {

        private final Map<T, Integer> map = new IdentityHashMap<>();
        private final MersenneTwisterFast random;


        public RandomComparator(MersenneTwisterFast random) {
            this.random = random;
        }

        @Override
        public int compare(T t1, T t2) {
            return Integer.compare(valueFor(t1), valueFor(t2));
        }

        private int valueFor(T t) {
            synchronized (map) {
                return map.computeIfAbsent(t, ignore -> random.nextInt());
            }
        }

    }
}
