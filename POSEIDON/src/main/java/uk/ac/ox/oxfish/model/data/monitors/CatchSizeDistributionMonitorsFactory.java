package uk.ac.ox.oxfish.model.data.monitors;

import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.distributions.GroupedYearlyDistributions;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.stream.Stream;

public class CatchSizeDistributionMonitorsFactory implements AlgorithmFactory<Monitors<AbstractSetAction>> {

    private AlgorithmFactory<GroupedYearlyDistributions> empiricalCatchSizeDistributions;

    @SuppressWarnings("unused")
    public CatchSizeDistributionMonitorsFactory() {
    }

    public CatchSizeDistributionMonitorsFactory(
        final AlgorithmFactory<GroupedYearlyDistributions> empiricalCatchSizeDistributions
    ) {
        this.empiricalCatchSizeDistributions = empiricalCatchSizeDistributions;
    }

    public AlgorithmFactory<GroupedYearlyDistributions> getEmpiricalCatchSizeDistributions() {
        return empiricalCatchSizeDistributions;
    }

    public void setEmpiricalCatchSizeDistributions(final AlgorithmFactory<GroupedYearlyDistributions> empiricalCatchSizeDistributions) {
        this.empiricalCatchSizeDistributions = empiricalCatchSizeDistributions;
    }

    @Override
    public Monitors<AbstractSetAction> apply(final FishState fishState) {
        return new MonitorList<>(
            Stream
                .of(FadSetAction.class, OpportunisticFadSetAction.class)
                .flatMap(actionClass ->
                    fishState
                        .getSpecies()
                        .stream()
                        .map(species ->
                            new CatchSizeDistributionMonitor<>(
                                actionClass,
                                species,
                                empiricalCatchSizeDistributions.apply(fishState).apply(species.getCode())
                            )
                        )
                )
        );
    }
}
