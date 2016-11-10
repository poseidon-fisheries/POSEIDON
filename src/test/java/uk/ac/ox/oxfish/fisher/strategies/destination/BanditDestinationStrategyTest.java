package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.BanditDestinationFactory;
import uk.ac.ox.oxfish.geography.MapDiscretizer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.ExponentialMovingAverage;
import uk.ac.ox.oxfish.model.data.IterativeAverage;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.EpsilonGreedyBanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.SoftmaxBanditAlgorithm;

import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 11/10/16.
 */
public class BanditDestinationStrategyTest {


    @Test
    public void NineBoxes() throws Exception {

        SimpleMapInitializer map = new SimpleMapInitializer(50,50, 0, 0, 1);
        MersenneTwisterFast randomizer = new MersenneTwisterFast();
        NauticalMap chart = map.makeMap(randomizer,
                                        mock(GlobalBiology.class),
                                        mock(FishState.class));
        MapDiscretizer discretizer = new MapDiscretizer(chart, 2, 2);
        BanditDestinationStrategy strategy = new BanditDestinationStrategy(
                (Function<Integer, BanditAverage>) integer -> new BanditAverage(integer,
                                                                                () -> new ExponentialMovingAverage<>(.5)),
                banditAverage -> new EpsilonGreedyBanditAlgorithm(banditAverage,.1),
                discretizer,
                new FavoriteDestinationStrategy(chart.getRandomBelowWaterLineSeaTile(randomizer))
        );

        discretizer.filterOutAllLandTiles();

        //option 2 is the best, you should pick it!


        for (int i = 0; i < 1000; i++) {
            SeaTile tile = strategy.getFavoriteSpot();
            int armPlayed = discretizer.getGroup(tile);
            double reward = -Math.pow(armPlayed-2,2)+randomizer.nextGaussian()/2;
            strategy.choose(tile,reward,randomizer);
        }

        ((EpsilonGreedyBanditAlgorithm) strategy.getAlgorithm()).setExplorationProbability(0);
        assertEquals(2,strategy.getAlgorithm().chooseArm(randomizer));
    }
}