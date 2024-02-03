package uk.ac.ox.oxfish.fisher.purseseiner;

import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.ports.PortInitializerFromFileFactory;
import uk.ac.ox.oxfish.model.data.distributions.EmpiricalCatchSizeDistributionsFromFile;
import uk.ac.ox.oxfish.model.data.monitors.CatchSizeDistributionMonitorsFactory;
import uk.ac.ox.oxfish.model.market.YearlyMarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

public class EpoPurseSeinerFleetFactory extends PurseSeinerFleetFactory {
    @SuppressWarnings("unused")
    public EpoPurseSeinerFleetFactory() {
    }

    public EpoPurseSeinerFleetFactory(
        final IntegerParameter targetYear,
        final InputPath inputFolder,
        final PurseSeineGearFactory purseSeineGearFactory,
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategyFactory,
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory
    ) {
        super(
            targetYear,
            inputFolder.path("vessels.csv"),
            inputFolder.path("costs.csv"),
            purseSeineGearFactory,
            new FadRefillGearStrategyFactory(
                targetYear,
                inputFolder.path("max_deployments.csv")
            ),
            destinationStrategyFactory,
            fishingStrategyFactory,
            new PurseSeinerDepartingStrategyFactory(),
            new YearlyMarketMapFromPriceFileFactory(
                inputFolder.path("prices.csv")
            ),
            new PortInitializerFromFileFactory(
                targetYear,
                inputFolder.path("ports.csv")
            ),
            new CatchSizeDistributionMonitorsFactory(
                new EmpiricalCatchSizeDistributionsFromFile(
                    inputFolder.path("catch_size_distributions.csv")
                )
            )
        );
    }
}
