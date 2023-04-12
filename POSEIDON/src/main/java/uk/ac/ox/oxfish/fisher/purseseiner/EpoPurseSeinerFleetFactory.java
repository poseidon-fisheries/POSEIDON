package uk.ac.ox.oxfish.fisher.purseseiner;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.model.market.YearlyMarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasFromFolderFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.model.scenario.StandardIattcRegulationsFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.Supplier;

public class EpoPurseSeinerFleetFactory<B extends LocalBiology> extends PurseSeinerFleetFactory<B> {
    public EpoPurseSeinerFleetFactory() {
    }

    public EpoPurseSeinerFleetFactory(
        final int targetYear,
        final InputPath inputFolder,
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final PurseSeineGearFactory<B> purseSeineGearFactory,
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategyFactory,
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory
    ) {
        super(
            inputFolder.path("boats.csv"),
            inputFolder.path("costs.csv"),
            purseSeineGearFactory,
            new FadRefillGearStrategyFactory(
                targetYear,
                inputFolder.path("max_deployments.csv")
            ),
            destinationStrategyFactory,
            fishingStrategyFactory,
            new StandardIattcRegulationsFactory(
                new ProtectedAreasFromFolderFactory(
                    inputFolder.path("regions"),
                    "region_tags.csv"
                )
            ),
            new PurseSeinerDepartingStrategyFactory(),
            new YearlyMarketMapFromPriceFileFactory(
                inputFolder.path("prices.csv"),
                speciesCodesSupplier
            ),
            new FromSimpleFilePortInitializer(
                targetYear,
                inputFolder.path("ports.csv")
            )
        );
    }
}
