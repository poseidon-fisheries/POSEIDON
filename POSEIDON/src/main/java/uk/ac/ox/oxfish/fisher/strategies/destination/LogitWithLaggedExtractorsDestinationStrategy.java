/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.log.TripLaggedExtractor;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * like logit destination strategy but has some extractors that require starting
 */
public class LogitWithLaggedExtractorsDestinationStrategy extends LogitDestinationStrategy {

    /**
     * when true, the tripLaggedExtractors are assumed to
     */
    private final boolean fleetWide;


    private final List<TripLaggedExtractor>  extractorsToStart;


    /**
     * remember that the tripLaggedExtractors need to appear both in the tripLaggedExtractors argument AND in the covariates
     * @param betas                        table of all the betas (some might be ignored if the map doesn't cover them)
     * @param covariates                   table of all hte observation extractors (generate x on the spot)
     * @param rowNames                     column that assign to each row of betas the group it belongs to
     * @param discretization               the discretization map
     * @param delegate
     * @param random
     * @param automaticallyAvoidMPA        automatically avoid not allowed areas
     * @param automaticallyAvoidWastelands automatically avoid areas where fish can't grow
     * @param fleetWide
     * @param extractorsToStart
     */
    public LogitWithLaggedExtractorsDestinationStrategy(
            double[][] betas, ObservationExtractor[][] covariates,
            List<Integer> rowNames, MapDiscretization discretization,
            FavoriteDestinationStrategy delegate, MersenneTwisterFast random, boolean automaticallyAvoidMPA,
            boolean automaticallyAvoidWastelands,

            boolean fleetWide, List<TripLaggedExtractor> extractorsToStart) {
        super(betas, covariates, rowNames, discretization, delegate, random, automaticallyAvoidMPA,
              automaticallyAvoidWastelands);
        this.fleetWide = fleetWide;
        this.extractorsToStart = extractorsToStart;
    }


    @Override
    public void start(FishState model, Fisher fisher) {
        super.start(model, fisher);
        for (TripLaggedExtractor extractor : extractorsToStart) {
            if(!fleetWide) {
                Preconditions.checkArgument(extractor.getFisherTracked()==null, "I am trying to link this extractor to this fisher, but it seems like another fisher has already activated it!");
                extractor.setFisherTracked(fisher);
            }
            extractor.start(model);
        }


    }
}
