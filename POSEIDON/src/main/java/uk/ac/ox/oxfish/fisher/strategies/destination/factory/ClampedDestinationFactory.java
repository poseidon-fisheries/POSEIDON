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


import uk.ac.ox.oxfish.fisher.strategies.destination.ClampedDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class ClampedDestinationFactory implements AlgorithmFactory<ClampedDestinationStrategy> {

    /**
     * share the same discretization
     */
    private final Locker<String, MapDiscretization> discretizationLocker = new Locker<>();
    private AlgorithmFactory<? extends MapDiscretizer> discretizer =
        new CentroidMapFileFactory();

    /**
     * grabbed from post-ITQ visit counts
     */
    private double[] preferences = new double[]{
        0.00586319218241042, 0.00260586319218241, 0.0560260586319218,
        0.0182410423452769, 0.0130293159609121, 0, 0.00195439739413681,
        0, 0.0501628664495114, 0.00325732899022801, 0, 0.10228013029316,
        0.0130293159609121, 0.00977198697068404, 0.0208469055374593, 0.0534201954397394,
        0.0514657980456026, 0.0495114006514658, 0.000651465798045603, 0.0638436482084691,
        0.00977198697068404, 0.193485342019544, 0.0104234527687296, 0, 0.0592833876221498,
        0.0377850162866449, 0.044299674267101, 0.0143322475570033, 0.0957654723127036,
        0.00130293159609121, 0, 0.0175895765472313
    };

    private DoubleParameter maxDistance = new FixedDoubleParameter(200);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ClampedDestinationStrategy apply(final FishState state) {

        //create the discretization
        final MapDiscretization discretization = discretizationLocker.presentKey(
            state.getUniqueID(), () -> {
                final MapDiscretizer mapDiscretizer = discretizer.apply(state);
                final MapDiscretization toReturn = new MapDiscretization(mapDiscretizer);
                toReturn.discretize(state.getMap());
                return toReturn;

            }
        );


        return new ClampedDestinationStrategy(
            new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
            discretization,
            maxDistance.applyAsDouble(state.getRandom()),
            preferences
        );

    }

    /**
     * Getter for property 'preferences'.
     *
     * @return Value for property 'preferences'.
     */
    public double[] getPreferences() {
        return preferences;
    }

    /**
     * Setter for property 'preferences'.
     *
     * @param preferences Value to set for property 'preferences'.
     */
    public void setPreferences(final double[] preferences) {
        this.preferences = preferences;
    }

    /**
     * Getter for property 'maxDistance'.
     *
     * @return Value for property 'maxDistance'.
     */
    public DoubleParameter getMaxDistance() {
        return maxDistance;
    }

    /**
     * Setter for property 'maxDistance'.
     *
     * @param maxDistance Value to set for property 'maxDistance'.
     */
    public void setMaxDistance(final DoubleParameter maxDistance) {
        this.maxDistance = maxDistance;
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
}
