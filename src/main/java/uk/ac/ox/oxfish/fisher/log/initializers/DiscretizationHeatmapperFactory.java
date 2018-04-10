/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.log.initializers;

import uk.ac.ox.oxfish.geography.discretization.IdentityDiscretizerFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;

import java.util.function.Supplier;

public class DiscretizationHeatmapperFactory implements AlgorithmFactory<DiscretizationHistogrammerInitializer> {

    /**
     * useful (in fact, needed) if you have multiple logbooks running at once!
     */
    private String identifier = "";


    private AlgorithmFactory<? extends MapDiscretizer> discretization
            = new IdentityDiscretizerFactory();


    private Locker<FishState, MapDiscretization> locker = new Locker<>() ;

    /**
     * if this is positive, that's when the histogrammer starts
     */
    private int histogrammerStartYear = 0;

    private boolean countEffort = false;

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public DiscretizationHistogrammerInitializer apply(FishState fishState) {
        MapDiscretization discretization = locker.presentKey(fishState,
                                                                new Supplier<MapDiscretization>() {
                                                                    @Override
                                                                    public MapDiscretization get() {
                                                                        MapDiscretization mapDiscretization = new
                                                                                MapDiscretization(
                                                                                DiscretizationHeatmapperFactory.this
                                                                                        .discretization.apply(
                                                                                        fishState)
                                                                        );
                                                                        mapDiscretization.discretize(fishState.getMap());

                                                                        return
                                                                                mapDiscretization;
                                                                    }
                                                                });

        return new DiscretizationHistogrammerInitializer(
                discretization,
                histogrammerStartYear,
                identifier,
                countEffort
        );



    }

    /**
     * Getter for property 'identifier'.
     *
     * @return Value for property 'identifier'.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Setter for property 'identifier'.
     *
     * @param identifier Value to set for property 'identifier'.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Getter for property 'discretization'.
     *
     * @return Value for property 'discretization'.
     */
    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    /**
     * Setter for property 'discretization'.
     *
     * @param discretization Value to set for property 'discretization'.
     */
    public void setDiscretization(
            AlgorithmFactory<? extends MapDiscretizer> discretization) {
        this.discretization = discretization;
    }

    /**
     * Getter for property 'histogrammerStartYear'.
     *
     * @return Value for property 'histogrammerStartYear'.
     */
    public int getHistogrammerStartYear() {
        return histogrammerStartYear;
    }

    /**
     * Setter for property 'histogrammerStartYear'.
     *
     * @param histogrammerStartYear Value to set for property 'histogrammerStartYear'.
     */
    public void setHistogrammerStartYear(int histogrammerStartYear) {
        this.histogrammerStartYear = histogrammerStartYear;
    }

    /**
     * Getter for property 'countEffort'.
     *
     * @return Value for property 'countEffort'.
     */
    public boolean isCountEffort() {
        return countEffort;
    }

    /**
     * Setter for property 'countEffort'.
     *
     * @param countEffort Value to set for property 'countEffort'.
     */
    public void setCountEffort(boolean countEffort) {
        this.countEffort = countEffort;
    }
}
