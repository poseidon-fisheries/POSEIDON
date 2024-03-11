/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.epo.fleet;

import uk.ac.ox.oxfish.fisher.purseseiner.fads.EvaluatorBasedFadDeactivationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.MonetaryValueFadEvaluatorFactory;
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
            ),
            new EvaluatorBasedFadDeactivationStrategyFactory(
                new MonetaryValueFadEvaluatorFactory()
            )
        );
    }
}
