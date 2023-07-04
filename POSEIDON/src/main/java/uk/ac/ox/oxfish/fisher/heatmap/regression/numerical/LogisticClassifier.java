/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Very simple logit regression returning true or false
 * Created by carrknight on 12/1/16.
 */
public class LogisticClassifier {


    /**
     * for each observation extractor (basically for each x) this stores the betas associated with it
     */
    private final LinkedHashMap<ObservationExtractor, Double> betas;


    @SuppressWarnings("unchecked")
    public LogisticClassifier(final Entry<ObservationExtractor, Double>... betas) {

        this.betas = new LinkedHashMap<>();
        for (final Entry<ObservationExtractor, Double> beta : betas)
            this.betas.put(beta.getKey(), beta.getValue());
    }

    public LogisticClassifier(
        final LinkedHashMap<ObservationExtractor, Double> betas
    ) {
        this.betas = betas;
    }

    /**
     * returns true or false with probability given by the logistic
     *
     * @param agent    the agent making the choice
     * @param testTime the hours since start of simulation at which point we want the test to be made
     * @param model    the model being used
     * @param tile     the tile we are considering (can be null)
     * @param random   the randomizer
     * @return true or false
     */
    public boolean test(
        final Fisher agent,
        final double testTime,
        final FishState model,
        final SeaTile tile,
        final MersenneTwisterFast random
    ) {

        return random.nextBoolean(getProbability(agent, testTime, model, tile));
    }

    /**
     * returns the probability 1/(1+exp(-xb))
     *
     * @param agent           the fisher
     * @param hoursSinceStart
     * @param model           the model
     * @param tile            the tile we are considering (can be null)
     * @return a number between 0 and 1
     */
    public double getProbability(
        final Fisher agent, final double hoursSinceStart, final FishState model, final SeaTile tile
    ) {
        double linearComponent = 0;
        for (final Entry<ObservationExtractor, Double> element : betas.entrySet()) {
            linearComponent += element.getKey().extract(
                tile,
                hoursSinceStart,
                agent,
                model
            ) * element.getValue();
        }
        return 1d / (1 + Math.exp(-linearComponent));
    }

    /**
     * returns true or false with probability given by the logistic. It assumes you are asking to test about the current time
     *
     * @param agent  the agent making the choice
     * @param model  the model being used
     * @param tile   the tile we are considering (can be null)
     * @param random the randomizer
     * @return true or false
     */
    public boolean test(
        final Fisher agent, final FishState model, final SeaTile tile, final MersenneTwisterFast random
    ) {

        return random.nextBoolean(getProbability(agent, model.getHoursSinceStart(), model, tile));
    }


    public int getSize() {
        return betas.size();
    }

}
