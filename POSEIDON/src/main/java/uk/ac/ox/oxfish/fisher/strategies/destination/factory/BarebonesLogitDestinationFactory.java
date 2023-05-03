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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by carrknight on 7/17/17.
 */
public abstract class BarebonesLogitDestinationFactory implements
    AlgorithmFactory<LogitDestinationStrategy> {
    /**
     * everybody shares the parent same destination logit strategy
     */
    private final Locker<String, MapDiscretization> discretizationLocker = new Locker<>();
    protected AlgorithmFactory<? extends MapDiscretizer> discretizer =
        new CentroidMapFileFactory();
    /**
     * intercept of dummy variable (I have been here in the past 90 days)
     */
    private DoubleParameter habitIntercept =
        new FixedDoubleParameter(2.53163185);
    private DoubleParameter distanceInKm =
        new FixedDoubleParameter(-0.00759009);
    private DoubleParameter habitPeriodInDays = new FixedDoubleParameter(90);
    private boolean automaticallyAvoidMPA = true;
    private boolean automaticallyAvoidWastelands = true;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    public LogitDestinationStrategy apply(final FishState state) {
        //create the discretization
        final MapDiscretization discretization = discretizationLocker.presentKey(
            state.getUniqueID(), () -> {
                final MapDiscretizer mapDiscretizer = discretizer.apply(state);
                final MapDiscretization toReturn = new MapDiscretization(mapDiscretizer);
                toReturn.discretize(state.getMap());
                return toReturn;

            }
        );

        //every area is valid
        final int areas = discretization.getNumberOfGroups();
        final List<Integer> validAreas = new LinkedList<>();
        for (int i = 0; i < areas; i++)
            validAreas.add(i);


        final double[][] betas = buildBetas(state, areas, validAreas);

        final ObservationExtractor[][] extractors = buildExtractors(state, discretization, areas, betas);


        automaticallyAvoidMPA = true;
        automaticallyAvoidWastelands = true;
        return new LogitDestinationStrategy(
            betas,
            extractors,
            validAreas,
            discretization,
            new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
            state.getRandom(),
            automaticallyAvoidMPA, automaticallyAvoidWastelands
        );


    }

    protected double[][] buildBetas(final FishState state, final int areas, final List<Integer> validAreas) {
        //the same parameters for all the choices
        final double[][] betas = new double[areas][2];
        betas[0][0] = habitIntercept.applyAsDouble(state.getRandom());
        betas[0][1] = distanceInKm.applyAsDouble(state.getRandom());
        for (int i = 1; i < areas; i++) {
            betas[i][0] = betas[0][0];
            betas[i][1] = betas[0][1];
        }
        return betas;
    }

    @NotNull
    protected ObservationExtractor[][] buildExtractors(
        final FishState state, final MapDiscretization discretization, final int areas, final double[][] betas
    ) {
        //get the extractors
        final ObservationExtractor[][] extractors = new ObservationExtractor[betas.length][];
        final ObservationExtractor[] commonExtractor = new ObservationExtractor[]{
            buildHabitExtractor(
                discretization,
                (int) getHabitPeriodInDays().applyAsDouble(state.getRandom())
            ),
            new PortDistanceExtractor()
        };
        for (int i = 0; i < areas; i++)
            extractors[i] = commonExtractor;
        return extractors;
    }

    @NotNull
    public abstract ObservationExtractor buildHabitExtractor(MapDiscretization discretization, int period);

    /**
     * Getter for property 'habitPeriodInDays'.
     *
     * @return Value for property 'habitPeriodInDays'.
     */
    public DoubleParameter getHabitPeriodInDays() {
        return habitPeriodInDays;
    }

    /**
     * Setter for property 'habitPeriodInDays'.
     *
     * @param habitPeriodInDays Value to set for property 'habitPeriodInDays'.
     */
    public void setHabitPeriodInDays(final DoubleParameter habitPeriodInDays) {
        this.habitPeriodInDays = habitPeriodInDays;
    }

    /**
     * Getter for property 'habitIntercept'.
     *
     * @return Value for property 'habitIntercept'.
     */
    public DoubleParameter getHabitIntercept() {
        return habitIntercept;
    }

    /**
     * Setter for property 'habitIntercept'.
     *
     * @param habitIntercept Value to set for property 'habitIntercept'.
     */
    public void setHabitIntercept(final DoubleParameter habitIntercept) {
        this.habitIntercept = habitIntercept;
    }

    /**
     * Getter for property 'distanceInKm'.
     *
     * @return Value for property 'distanceInKm'.
     */
    public DoubleParameter getDistanceInKm() {
        return distanceInKm;
    }

    /**
     * Setter for property 'distanceInKm'.
     *
     * @param distanceInKm Value to set for property 'distanceInKm'.
     */
    public void setDistanceInKm(final DoubleParameter distanceInKm) {
        this.distanceInKm = distanceInKm;
    }

    /**
     * Getter for property 'discretizer'.
     *
     * @return Value for property 'discretizer'.
     */
    public AlgorithmFactory<? extends MapDiscretizer> getDiscretizer() {
        return discretizer;
    }

    /**
     * Setter for property 'discretizer'.
     *
     * @param discretizer Value to set for property 'discretizer'.
     */
    public void setDiscretizer(
        final AlgorithmFactory<? extends MapDiscretizer> discretizer
    ) {
        this.discretizer = discretizer;
    }

    /**
     * Getter for property 'automaticallyAvoidMPA'.
     *
     * @return Value for property 'automaticallyAvoidMPA'.
     */
    public boolean isAutomaticallyAvoidMPA() {
        return automaticallyAvoidMPA;
    }

    /**
     * Setter for property 'automaticallyAvoidMPA'.
     *
     * @param automaticallyAvoidMPA Value to set for property 'automaticallyAvoidMPA'.
     */
    public void setAutomaticallyAvoidMPA(final boolean automaticallyAvoidMPA) {
        this.automaticallyAvoidMPA = automaticallyAvoidMPA;
    }

    /**
     * Getter for property 'automaticallyAvoidWastelands'.
     *
     * @return Value for property 'automaticallyAvoidWastelands'.
     */
    public boolean isAutomaticallyAvoidWastelands() {
        return automaticallyAvoidWastelands;
    }

    /**
     * Setter for property 'automaticallyAvoidWastelands'.
     *
     * @param automaticallyAvoidWastelands Value to set for property 'automaticallyAvoidWastelands'.
     */
    public void setAutomaticallyAvoidWastelands(final boolean automaticallyAvoidWastelands) {
        this.automaticallyAvoidWastelands = automaticallyAvoidWastelands;
    }
}
