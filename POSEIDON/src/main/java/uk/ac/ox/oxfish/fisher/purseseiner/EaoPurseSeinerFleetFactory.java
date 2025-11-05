package uk.ac.ox.oxfish.fisher.purseseiner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.ports.PortInitializerFromFileFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.distributions.EmpiricalCatchSizeDistributionsFromFile;
import uk.ac.ox.oxfish.model.data.monitors.CatchSizeDistributionMonitorsFactory;
import uk.ac.ox.oxfish.model.market.YearlyMarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.scenario.EaoScenario;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import java.util.List;


/**
 * Created by Brian Powers 5/21/2025 for the Eastern Atlantic Tuna Model
 *
 * A Factory to create the Purse Seiner Fleet for the EAstern Atlantic Ocean Tuna fishery
 * This basically takes in the input files defining the vessels and economic information, ports, and some other
 * details for the production of the fleet (and the individual agents)
 **/

public class EaoPurseSeinerFleetFactory extends PurseSeinerFleetFactory {
    @SuppressWarnings("unused")
    public EaoPurseSeinerFleetFactory() {
    }

    public EaoPurseSeinerFleetFactory(
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
                inputFolder.path("max_deployments_dummy.csv")
            ),
            destinationStrategyFactory,
            fishingStrategyFactory,
            new PurseSeinerDepartingStrategyFactory(),
            new YearlyMarketMapFromPriceFileFactory(
                inputFolder.path("prices.csv")
            ),
            new PortInitializerFromFileFactory(
                targetYear,
                inputFolder.path("ports_min.csv")
            ),
            new CatchSizeDistributionMonitorsFactory(
                new EmpiricalCatchSizeDistributionsFromFile(
                    inputFolder.path("catch_size_distributions.csv")
                )
            )
        );
    }

    @Override
    public List<Fisher> makeFishers(
        final FishState fishState,
        final int targetYear
    ) {
        addMonitors(fishState, EaoScenario.REGIONAL_DIVISION);
        return new EaoPurseSeineVesselReader(
            getVesselsFile().get(),
            targetYear,
            makeFisherFactory(fishState),
            buildPorts(fishState)
        ).apply(fishState);
    }

}
