package uk.ac.ox.oxfish.fisher.purseseiner;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.model.market.YearlyMarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.regs.ConjunctiveRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.TaggedRegulationFactory;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasFromFolderFactory;
import uk.ac.ox.oxfish.model.regs.factory.SpecificProtectedAreaFromCoordinatesFactory;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.Supplier;

import static java.time.Month.*;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.dayOfYear;

public class EpoPurseSeinerFleetFactory extends PurseSeinerFleetFactory {
    @SuppressWarnings("unused")
    public EpoPurseSeinerFleetFactory() {
    }

    public EpoPurseSeinerFleetFactory(
        final int targetYear,
        final InputPath inputFolder,
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final PurseSeineGearFactory purseSeineGearFactory,
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategyFactory,
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory
    ) {
        super(
            inputFolder.path("vessels.csv"),
            inputFolder.path("costs.csv"),
            purseSeineGearFactory,
            new FadRefillGearStrategyFactory(
                targetYear,
                inputFolder.path("max_deployments.csv")
            ),
            destinationStrategyFactory,
            fishingStrategyFactory,
            new ConjunctiveRegulationsFactory(
                ImmutableList.of(
                    new ProtectedAreasFromFolderFactory(
                        inputFolder.path("regions"),
                        "region_tags.csv"
                    ),
                    new TemporaryRegulationFactory(
                        new SpecificProtectedAreaFromCoordinatesFactory(
                            "El Corralito",
                            4, -110, -3, -96
                        ),
                        dayOfYear(2017, OCTOBER, 9),
                        dayOfYear(2017, NOVEMBER, 8)
                    ),
                    new TaggedRegulationFactory(
                        new TemporaryRegulationFactory(
                            new NoFishingFactory(),
                            dayOfYear(2017, JULY, 29),
                            dayOfYear(2017, OCTOBER, 8)
                        ),
                        "closure A"
                    ),
                    new TaggedRegulationFactory(
                        new TemporaryRegulationFactory(
                            new NoFishingFactory(),
                            dayOfYear(2017, NOVEMBER, 9),
                            dayOfYear(2017, JANUARY, 19)
                        ),
                        "closure B"
                    )
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
