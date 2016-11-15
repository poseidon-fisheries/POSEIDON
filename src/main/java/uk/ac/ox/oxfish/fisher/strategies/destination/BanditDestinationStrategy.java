package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.MapDiscretizer;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.bandit.BanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.factory.BanditSupplier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * A purely bandit-algorithm using destination strategy
 * Created by carrknight on 11/10/16.
 */
public class BanditDestinationStrategy implements DestinationStrategy{



    private final BanditAlgorithm algorithm;

    private final MapDiscretizer discretizer;

    private final FavoriteDestinationStrategy delegate;

    /**
     * the index represents the "bandit arm" index, the number in teh array at that index represents
     * the discretemap group. This is done to skip areas that are just land
     */
    private final int[] filteredGroups;
    private final BanditAverage banditAverage;


    /**
     * the constructor looks a bit weird but it's just due to the fact that we need to first
     * figure out the right number of arms from the discretizer
     * @param averagerMaker builds the bandit average given the number of arms
     * @param banditMaker builds the bandit algorithm given the bandit average
     * @param delegate the delegate strategy
     */
    public BanditDestinationStrategy(
            Function<Integer, BanditAverage> averagerMaker,
            BanditSupplier banditMaker,
            MapDiscretizer discretizer,
            FavoriteDestinationStrategy delegate) {
        //map arms to valid map groups
        this.discretizer = discretizer;
        ArrayList<Integer> validGroups = new ArrayList<>();
        for(int i=0; i<discretizer.getNumberOfGroups(); i++)
        {
            if(discretizer.isValid(i))
                validGroups.add(i);
        }
        filteredGroups = new int[validGroups.size()];
        for(int i=0; i<filteredGroups.length; i++)
            filteredGroups[i]=validGroups.get(i);
        //create the bandit algorithm
        banditAverage = averagerMaker.apply(filteredGroups.length);
        algorithm = banditMaker.apply(banditAverage);

        this.delegate = delegate;
    }

    private int fromBanditArmToMapGroup(int banditArm){
        return filteredGroups[banditArm];
    }

    private int fromMapGroupToBanditArm(int mapGroup){
        for(int i=0; i<filteredGroups.length; i++)
            if(filteredGroups[i]==mapGroup)
                return i;
        throw new RuntimeException("trying to fish in a all-land map rectangle. Not good");
    }


    public void choose(SeaTile lastDestination, double reward, MersenneTwisterFast random){


        int armPlayed = fromMapGroupToBanditArm(discretizer.getGroup(lastDestination));
        algorithm.observeReward(reward,armPlayed);

        //make new decision
        int armToPlay = algorithm.chooseArm(random);
        int groupToFishIn = fromBanditArmToMapGroup(armToPlay);
        assert discretizer.isValid(groupToFishIn);
        //assuming here the map discretizer has already removed all the land tiles
        List<SeaTile> mapGroup = discretizer.getGroup(groupToFishIn);
        SeaTile tile = mapGroup.get(random.nextInt(
                mapGroup.size()));
        delegate.setFavoriteSpot(tile);
    }

    @Override
    public void start(FishState model, Fisher fisher) {

        delegate.start(model,fisher);
        fisher.addPerTripAdaptation(new Adaptation() {
            @Override
            public void adapt(Fisher toAdapt, FishState state, MersenneTwisterFast random) {
                // observe previous trip
                SeaTile lastDestination = delegate.getFavoriteSpot();
                assert toAdapt.getLastFinishedTrip().getMostFishedTileInTrip() == null ||
                        toAdapt.getLastFinishedTrip().getMostFishedTileInTrip().equals(lastDestination);
                double reward = toAdapt.getLastFinishedTrip().getProfitPerHour(true);
                choose(lastDestination,reward,random);

            }

            @Override
            public void start(FishState model, Fisher fisher) {
                discretizer.filterOutAllLandTiles();
            }

            @Override
            public void turnOff(Fisher fisher) {

            }
        });

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
            Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }


    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }

    public int getNumberOfObservations(int arm) {
        return banditAverage.getNumberOfObservations(arm);
    }

    public double getAverage(int arm) {
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
}
