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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.bandit.BanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.BanditSwitch;
import uk.ac.ox.oxfish.utility.bandit.factory.BanditSupplier;

import java.util.List;
import java.util.function.Function;

/**
 * A purely bandit-algorithm using destination strategy
 * Created by carrknight on 11/10/16.
 */
public class BanditDestinationStrategy implements DestinationStrategy {


    final ObjectiveFunction<Fisher> objective;
    private final BanditAlgorithm algorithm;
    private final MapDiscretization discretization;
    private final FavoriteDestinationStrategy delegate;
    /**
     * the index represents the "bandit arm" index, the number in teh array at that index represents
     * the discretemap group. This is done to skip areas that are just land
     */
    private final BanditSwitch banditSwitch;
    private final BanditAverage banditAverage;
    final private boolean respectMPA;
    private final boolean ignoreWastelands;
    private Adaptation concreteAdaptation;
    private Fisher fisher;
    private FishState model;
    private boolean imitate = false;

    /**
     * the constructor looks a bit weird but it's just due to the fact that we need to first
     * figure out the right number of arms from the discretization
     *
     * @param averagerMaker    builds the bandit average given the number of arms
     * @param banditMaker      builds the bandit algorithm given the bandit average
     * @param delegate         the delegate strategy
     * @param objective
     * @param respectMPA
     * @param ignoreWastelands
     */
    public BanditDestinationStrategy(
        final Function<Integer, BanditAverage> averagerMaker,
        final BanditSupplier banditMaker,
        final MapDiscretization discretization,
        final FavoriteDestinationStrategy delegate,
        final ObjectiveFunction<Fisher> objective,
        final boolean respectMPA, final boolean ignoreWastelands
    ) {
        //map arms to valid map groups
        this.discretization = discretization;

        this.banditSwitch = new BanditSwitch(
            discretization.getNumberOfGroups(),
            discretization::isValid
        );

        //create the bandit algorithm
        banditAverage = averagerMaker.apply(banditSwitch.getNumberOfArms());
        algorithm = banditMaker.apply(banditAverage);

        this.delegate = delegate;
        this.objective = objective;
        this.respectMPA = respectMPA;
        this.ignoreWastelands = ignoreWastelands;
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {

        this.fisher = fisher;
        this.model = model;
        delegate.start(model, fisher);
        concreteAdaptation = new Adaptation() {
            @Override
            public void adapt(final Fisher toAdapt, final FishState state, final MersenneTwisterFast random) {
                // observe previous trip
                final SeaTile lastDestination = toAdapt.getLastFinishedTrip().getMostFishedTileInTrip() == null ?
                    delegate.getFavoriteSpot() : toAdapt.getLastFinishedTrip().getMostFishedTileInTrip();
                assert toAdapt.getLastFinishedTrip().getMostFishedTileInTrip() == null ||
                    toAdapt.getLastFinishedTrip().getMostFishedTileInTrip().equals(lastDestination);
                final double reward = objective.computeCurrentFitness(fisher, fisher);

                //peek at friends?
                if (imitate)
                    for (final Fisher friend : fisher.getDirectedFriends()) {
                        final TripRecord friendTrip = friend.getLastFinishedTrip();
                        if (friendTrip != null && friendTrip.getMostFishedTileInTrip() != null) {
                            algorithm.observeReward(
                                objective.computeCurrentFitness(fisher, friend),
                                fromMapGroupToBanditArm(discretization.getGroup(
                                    friendTrip.getMostFishedTileInTrip()))
                            );
                        }


                    }

                choose(lastDestination, reward, random);

            }

            @Override
            public void start(final FishState model, final Fisher fisher) {

            }

            @Override
            public void turnOff(final Fisher fisher) {

            }
        };
        fisher.addPerTripAdaptation(concreteAdaptation);

    }

    private int fromMapGroupToBanditArm(final int mapGroup) {

        return banditSwitch.getArm(mapGroup);

    }


    public void choose(final SeaTile lastDestination, final double reward, final MersenneTwisterFast random) {


        final Integer group = discretization.getGroup(lastDestination);
        if (group != null) {
            final int armPlayed = fromMapGroupToBanditArm(group);
            algorithm.observeReward(reward, armPlayed);
        }
        //make new decision
        final int armToPlay = algorithm.chooseArm(random);
        final int groupToFishIn = fromBanditArmToMapGroup(armToPlay);
        assert discretization.isValid(groupToFishIn);
        //assuming here the map discretization has already removed all the land tiles
        final List<SeaTile> mapGroup = discretization.getGroup(groupToFishIn);
        final SeaTile tile = FishStateUtilities.getValidSeatileFromGroup(
            random,
            mapGroup,
            respectMPA,
            fisher,
            model,
            ignoreWastelands,
            100
        );
        if (tile != null)
            delegate.setFavoriteSpot(tile);
    }

    private int fromBanditArmToMapGroup(final int banditArm) {

        return banditSwitch.getGroup(banditArm);
    }

    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
        final Fisher fisher, final MersenneTwisterFast random, final FishState model, final Action currentAction
    ) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }


    @Override
    public void turnOff(final Fisher fisher) {
        delegate.turnOff(fisher);
        if (concreteAdaptation != null)
            fisher.removePerTripAdaptation(concreteAdaptation);

    }

    public int getNumberOfObservations(final int arm) {
        return banditAverage.getNumberOfObservations(arm);
    }

    public double getAverage(final int arm) {
        return banditAverage.getAverage(arm);
    }

    public int getNumberOfArms() {
        return banditAverage.getNumberOfArms();
    }

    /**
     * Getter for property 'algorithm'.
     *
     * @return Value for property 'algorithm'.
     */
    public BanditAlgorithm getAlgorithm() {
        return algorithm;
    }

    public SeaTile getFavoriteSpot() {
        return delegate.getFavoriteSpot();
    }

    /**
     * Getter for property 'discretization'.
     *
     * @return Value for property 'discretization'.
     */
    public MapDiscretization getDiscretization() {
        return discretization;
    }

    /**
     * Getter for property 'banditSwitch'.
     *
     * @return Value for property 'banditSwitch'.
     */
    public BanditSwitch getBanditSwitch() {
        return banditSwitch;
    }

    /**
     * Getter for property 'imitate'.
     *
     * @return Value for property 'imitate'.
     */
    public boolean isImitate() {
        return imitate;
    }

    /**
     * Setter for property 'imitate'.
     *
     * @param imitate Value to set for property 'imitate'.
     */
    public void setImitate(final boolean imitate) {
        this.imitate = imitate;
    }
}
