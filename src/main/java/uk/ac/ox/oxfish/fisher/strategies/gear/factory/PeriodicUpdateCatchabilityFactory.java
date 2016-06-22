package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.strategies.gear.PeriodicUpdateGearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A periodic gear updater that only looks at catchablity (for all species!)
 * Created by carrknight on 6/21/16.
 */
public class PeriodicUpdateCatchabilityFactory implements AlgorithmFactory<PeriodicUpdateGearStrategy> {


    private AlgorithmFactory<? extends AdaptationProbability>
            probability = new FixedProbabilityFactory(.2, 1);


    private boolean yearly = true;

    /**
     * mantains a (weak) set of fish states so that we initialize our data gatherers only once!
     */
    private final Set<FishState> weakStateMap = Collections.newSetFromMap(new WeakHashMap<>());

    private DoubleParameter minimumCatchability = new FixedDoubleParameter(0.01);

    private DoubleParameter maximumCatchability = new FixedDoubleParameter(0.2);

    /**
     * maximum change per update as a proportion of (maximumCatchability-minimumCatchability)
     */
    private DoubleParameter shockSize = new FixedDoubleParameter(0.05);


    /**
     * Creates the gear
     */
    @Override
    public PeriodicUpdateGearStrategy apply(FishState model) {

        //size of our delta
        final double shock = shockSize.apply(model.getRandom());
        final double minCatchability = minimumCatchability.apply(model.getRandom());
        final double maxCatchability = maximumCatchability.apply(model.getRandom());

        //add data gathering if necessary
        if(!weakStateMap.contains(model))
        {
            weakStateMap.add(model);
            addDataGatherers(model);
            assert weakStateMap.contains(model);
        }

        return new PeriodicUpdateGearStrategy(
                yearly,
                new RandomStep<Gear>() {
                    @Override
                    public Gear randomStep(
                            FishState state, MersenneTwisterFast random, Fisher fisher,
                            Gear current1) {
                        Preconditions.checkArgument(current1.getClass().equals(RandomCatchabilityTrawl.class),
                                                    "PeriodicUpdateMileageFactory works only with RandomCatchabilityTrawl gear");
                        assert current1.getClass().equals(RandomCatchabilityTrawl.class);
                        RandomCatchabilityTrawl current = ((RandomCatchabilityTrawl) current1);

                        double[] original = current.getCatchabilityMeanPerSpecie();
                        double[] catchability = Arrays.copyOf(original, original.length);
                        for(int i = 0; i< original.length; i++)
                        {
                            double currentShock = random.nextDouble() * shock * (maxCatchability-minCatchability);
                            if (random.nextBoolean())
                                currentShock -= currentShock;
                            catchability[i] = original[i] + currentShock;
                            catchability[i] = Math.max(catchability[i], minCatchability);
                            catchability[i] = Math.min(catchability[i], maxCatchability);

                        }
                        return new RandomCatchabilityTrawl(
                                catchability,
                                current.getCatchabilityDeviationPerSpecie(),
                                current.getGasPerHourFished()
                        );
                    }
                }
                ,
                probability.apply(model)

        );
    }

    private void addDataGatherers(FishState model) {
        //start collecting red catchability and blue catchability
        for(int species = 0; species<model.getSpecies().size(); species++) {
            int i = species;
            model.getYearlyDataSet().registerGatherer(model.getSpecies().get(species)+ " Catchability", state1 -> {
                double size = state1.getFishers().size();
                if (size == 0)
                    return Double.NaN;
                else {
                    double total = 0;
                    for (Fisher fisher1 : state1.getFishers())
                        total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[i]
                                ;
                    return total / size;
                }
            }, Double.NaN);

        }
    }


    /**
     * Getter for property 'probability'.
     *
     * @return Value for property 'probability'.
     */
    public AlgorithmFactory<? extends AdaptationProbability> getProbability() {
        return probability;
    }

    /**
     * Setter for property 'probability'.
     *
     * @param probability Value to set for property 'probability'.
     */
    public void setProbability(
            AlgorithmFactory<? extends AdaptationProbability> probability) {
        this.probability = probability;
    }

    /**
     * Getter for property 'yearly'.
     *
     * @return Value for property 'yearly'.
     */
    public boolean isYearly() {
        return yearly;
    }

    /**
     * Setter for property 'yearly'.
     *
     * @param yearly Value to set for property 'yearly'.
     */
    public void setYearly(boolean yearly) {
        this.yearly = yearly;
    }

    /**
     * Getter for property 'weakStateMap'.
     *
     * @return Value for property 'weakStateMap'.
     */
    public Set<FishState> getWeakStateMap() {
        return weakStateMap;
    }

    /**
     * Getter for property 'minimumCatchability'.
     *
     * @return Value for property 'minimumCatchability'.
     */
    public DoubleParameter getMinimumCatchability() {
        return minimumCatchability;
    }

    /**
     * Setter for property 'minimumCatchability'.
     *
     * @param minimumCatchability Value to set for property 'minimumCatchability'.
     */
    public void setMinimumCatchability(DoubleParameter minimumCatchability) {
        this.minimumCatchability = minimumCatchability;
    }

    /**
     * Getter for property 'maximumCatchability'.
     *
     * @return Value for property 'maximumCatchability'.
     */
    public DoubleParameter getMaximumCatchability() {
        return maximumCatchability;
    }

    /**
     * Setter for property 'maximumCatchability'.
     *
     * @param maximumCatchability Value to set for property 'maximumCatchability'.
     */
    public void setMaximumCatchability(DoubleParameter maximumCatchability) {
        this.maximumCatchability = maximumCatchability;
    }

    /**
     * Getter for property 'shockSize'.
     *
     * @return Value for property 'shockSize'.
     */
    public DoubleParameter getShockSize() {
        return shockSize;
    }

    /**
     * Setter for property 'shockSize'.
     *
     * @param shockSize Value to set for property 'shockSize'.
     */
    public void setShockSize(DoubleParameter shockSize) {
        this.shockSize = shockSize;
    }
}
