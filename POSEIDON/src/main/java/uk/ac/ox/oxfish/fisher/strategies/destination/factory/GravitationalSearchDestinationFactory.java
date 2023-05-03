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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripObjective;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.CoordinateTransformer;
import uk.ac.ox.oxfish.utility.adaptation.maximization.GravitationalSearchAdaptation;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by carrknight on 10/6/16.
 */
public class GravitationalSearchDestinationFactory implements AlgorithmFactory<PerTripIterativeDestinationStrategy>
{


    private DoubleParameter explorationSize = new FixedDoubleParameter(20);

    private DoubleParameter initialSpeed = new FixedDoubleParameter(0);

    private DoubleParameter gravitationalConstant = new FixedDoubleParameter(100);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public PerTripIterativeDestinationStrategy apply(FishState state) {
        MersenneTwisterFast random = state.random;
        NauticalMap map = state.getMap();


        FavoriteDestinationStrategy delegate = new FavoriteDestinationStrategy(map, random);
        GravitationalSearchAdaptation<SeaTile> search = new GravitationalSearchAdaptation<>(
                new Sensor<Fisher,SeaTile>() {
                    @Override
                    public SeaTile scan(Fisher fisher) {

                        if(
                                fisher.getDestinationStrategy() instanceof PerTripIterativeDestinationStrategy &&
                                ((PerTripIterativeDestinationStrategy) fisher.getDestinationStrategy()).getDelegate().equals(
                                delegate))
                            return delegate.getFavoriteSpot();
                        else {
                            TripRecord lastFinishedTrip = fisher.getLastFinishedTrip();
                            return lastFinishedTrip == null ? null : lastFinishedTrip.getMostFishedTileInTrip();

                        }
                    }
                },
                new Actuator<Fisher,SeaTile>() {
                    @Override
                    public void apply(Fisher fisher, SeaTile change, FishState model) {
                            delegate.setFavoriteSpot(change);
                    }
                },
                new Predicate<Fisher>() {
                    @Override
                    public boolean test(Fisher fisher) {
                        return true;
                    }
                },
                new CoordinateTransformer<SeaTile>() {
                    @Override
                    public double[] toCoordinates(
                            SeaTile variable,
                            Fisher fisher,
                            FishState model) {
                        return variable == null ? null :
                                new double[]{
                                        variable.getGridX(),
                                        variable.getGridY()};
                    }

                    @Override
                    public SeaTile fromCoordinates(
                            double[] variable,
                            Fisher fisher,
                            FishState model) {
                        return model.getMap().getSeaTile(
                                (int)variable[0],
                                (int)variable[1]);
                    }
                },
                new HourlyProfitInTripObjective(true),
                100,
                10,
                new FixedDoubleParameter(0),
                state.getRandom()

        );
        //bound and randomize if you end up on land!
        search.setCoordinatesBounder(new Consumer<double[]>() {
            @Override
            public void accept(double[] variable) {
                variable[0] = Math.max(Math.min(variable[0], state.getMap().getWidth() - 1), 0);
                variable[1] = Math.max(Math.min(variable[1], state.getMap().getHeight() - 1), 0);

                SeaTile presumedLocation = map.getSeaTile((int) variable[0], (int) variable[1]);
                if(presumedLocation.isLand())
                {
                    Object[] options = map.getMooreNeighbors(presumedLocation, 3).stream().filter(new Predicate() {
                        @Override
                        public boolean test(Object o) {
                            return ((SeaTile) o).isWater();
                        }
                    }).toArray();
                    SeaTile tile;
                    if(options.length>0)
                        tile = (SeaTile) options[random.nextInt(options.length)];
                    else
                        tile = map.getRandomBelowWaterLineSeaTile(random);
                    variable[0] = tile.getGridX();
                    variable[1] = tile.getGridY();
                }
            }
        });
        return  new PerTripIterativeDestinationStrategy(
                delegate,
                search

        );
    }


    /**
     * Getter for property 'explorationSize'.
     *
     * @return Value for property 'explorationSize'.
     */
    public DoubleParameter getExplorationSize() {
        return explorationSize;
    }

    /**
     * Setter for property 'explorationSize'.
     *
     * @param explorationSize Value to set for property 'explorationSize'.
     */
    public void setExplorationSize(DoubleParameter explorationSize) {
        this.explorationSize = explorationSize;
    }

    /**
     * Getter for property 'initialSpeed'.
     *
     * @return Value for property 'initialSpeed'.
     */
    public DoubleParameter getInitialSpeed() {
        return initialSpeed;
    }

    /**
     * Setter for property 'initialSpeed'.
     *
     * @param initialSpeed Value to set for property 'initialSpeed'.
     */
    public void setInitialSpeed(DoubleParameter initialSpeed) {
        this.initialSpeed = initialSpeed;
    }

    /**
     * Getter for property 'gravitationalConstant'.
     *
     * @return Value for property 'gravitationalConstant'.
     */
    public DoubleParameter getGravitationalConstant() {
        return gravitationalConstant;
    }

    /**
     * Setter for property 'gravitationalConstant'.
     *
     * @param gravitationalConstant Value to set for property 'gravitationalConstant'.
     */
    public void setGravitationalConstant(DoubleParameter gravitationalConstant) {
        this.gravitationalConstant = gravitationalConstant;
    }
}
