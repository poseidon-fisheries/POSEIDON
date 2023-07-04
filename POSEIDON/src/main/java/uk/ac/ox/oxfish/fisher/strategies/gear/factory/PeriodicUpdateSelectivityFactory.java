package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.DecoratorGearPair;
import uk.ac.ox.oxfish.fisher.equipment.gear.SelectivityAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticAbundanceFilter;
import uk.ac.ox.oxfish.fisher.strategies.gear.PeriodicUpdateGearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;

/**
 * a gear strategy with little re-use I am afraid.
 * Basically changes selectivity of the gear while keeping the catchability and retention fixed
 */
public class PeriodicUpdateSelectivityFactory implements AlgorithmFactory<PeriodicUpdateGearStrategy> {

    public final static Startable SELECTIVITY_DATA_GATHERERS = new Startable() {
        @Override
        public void start(final FishState model) {
            //first add data gatherers
            final Gatherer<FishState> aParameterGatherer = state -> {
                final double size = state.getFishers().size();
                if (size == 0)
                    return Double.NaN;
                else {
                    double total = 0;
                    for (final Fisher fisher1 : state.getFishers())
                        total += ((SelectivityAbundanceGear) DecoratorGearPair.getActualGear(fisher1.getGear())
                            .getDecorated()).
                            getaParameter();
                    return total / size;
                }
            };
            model.getDailyDataSet().registerGatherer("Average Selectivity A Parameter", aParameterGatherer, Double.NaN);
            model.getYearlyDataSet()
                .registerGatherer("Average Selectivity A Parameter", aParameterGatherer, Double.NaN);

            final Gatherer<FishState> bParameterGatherer = state -> {
                final double size = state.getFishers().size();
                if (size == 0)
                    return Double.NaN;
                else {
                    double total = 0;
                    for (final Fisher fisher1 : state.getFishers())
                        total += ((SelectivityAbundanceGear) DecoratorGearPair.getActualGear(fisher1.getGear())
                            .getDecorated()).
                            getbParameter();
                    return total / size;
                }
            };
            model.getDailyDataSet().registerGatherer("Average Selectivity B Parameter", bParameterGatherer, Double.NaN);
            model.getYearlyDataSet()
                .registerGatherer("Average Selectivity B Parameter", bParameterGatherer, Double.NaN);

        }


        @Override
        public void turnOff() {

        }
    };
    /**
     * locker to make sure data collectors are done only once
     */
    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, Startable> locker =
        new uk.ac.ox.oxfish.utility.Locker<>();
    /**
     * probability this gets activated
     */
    private AlgorithmFactory<? extends AdaptationProbability>
        probability = new FixedProbabilityFactory(.2, .6);
    /**
     * when this is false, the agent will change bi-monthly
     */
    private boolean yearly = false;
    private double maxPercentageChangeA = .1;
    private double maxPercentageChangeB = .1;

    @Override
    public PeriodicUpdateGearStrategy apply(final FishState model) {

        locker.presentKey(
            model.getUniqueID(),
            () -> {

                model.registerStartable(SELECTIVITY_DATA_GATHERERS);
                return SELECTIVITY_DATA_GATHERERS;

            }
        );
        return new PeriodicUpdateGearStrategy
            (
                yearly,
                (state, random, fisher, current) -> {
                    final SelectivityAbundanceGear actualGear =
                        (SelectivityAbundanceGear)
                            DecoratorGearPair.getActualGear(current).getDecorated();


                    final double newAParameter = actualGear.getaParameter() * (1d - maxPercentageChangeA + 2 * random.nextDouble() * maxPercentageChangeA);
                    final double newBParameter = actualGear.getbParameter() * (1d - maxPercentageChangeB + 2 * random.nextDouble() * maxPercentageChangeB);
                    if (actualGear.getRetention() == null)
                        return new SelectivityAbundanceGear(
                            actualGear.getLitersOfGasConsumedEachHourFishing(),
                            actualGear.getCatchabilityFilter(),
                            new LogisticAbundanceFilter(newAParameter, newBParameter,
                                actualGear.getSelectivity().isMemoization(),
                                actualGear.getSelectivity().isRounding(), true
                            )
                        );
                    else
                        return new SelectivityAbundanceGear(
                            actualGear.getLitersOfGasConsumedEachHourFishing(),
                            actualGear.getCatchabilityFilter(),
                            new LogisticAbundanceFilter(newAParameter, newBParameter,
                                actualGear.getSelectivity().isMemoization(),
                                actualGear.getSelectivity().isRounding(), true
                            ),
                            actualGear.getRetention()
                        );
                },
                probability.apply(model)
            );


    }

    public AlgorithmFactory<? extends AdaptationProbability> getProbability() {
        return probability;
    }

    public void setProbability(final AlgorithmFactory<? extends AdaptationProbability> probability) {
        this.probability = probability;
    }

    public boolean isYearly() {
        return yearly;
    }

    public void setYearly(final boolean yearly) {
        this.yearly = yearly;
    }

    public double getMaxPercentageChangeA() {
        return maxPercentageChangeA;
    }

    public void setMaxPercentageChangeA(final double maxPercentageChangeA) {
        this.maxPercentageChangeA = maxPercentageChangeA;
    }

    public double getMaxPercentageChangeB() {
        return maxPercentageChangeB;
    }

    public void setMaxPercentageChangeB(final double maxPercentageChangeB) {
        this.maxPercentageChangeB = maxPercentageChangeB;
    }
}
