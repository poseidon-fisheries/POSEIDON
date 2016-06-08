package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractors;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.SNALSARutilities;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.SNALSARDestinationFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FixedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 6/8/16.
 */
public class SNALSARDestinationStrategyTest {


    @Test
    public void extractorsStart() throws Exception {


        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();
        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        SNALSARDestinationStrategy strategy = factory.apply(model);
        Fisher test = mock(Fisher.class);

        strategy.start(model,test);
        verify(test,times(6)).addFeatureExtractor(anyString(),any());
    }

    @Test
    public void randomizesAsDefault() throws Exception {

        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        SNALSARDestinationStrategy strategy = factory.apply(model);
        SeaTile option1 = mock(SeaTile.class);
        SeaTile option2 = mock(SeaTile.class);
        ArrayList<SeaTile> options = Lists.newArrayList(option1, option2);

        defaultSetup(model, strategy,options);



        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for(int i=0; i<100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class));
            if(strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                assertEquals(strategy.getFavoriteSpot(),(option2));
                timesOption2WasChosen++;
            }

        }
        assertTrue(timesOption1WasChosen>10);
        assertTrue(timesOption2WasChosen>10);

    }

    @Test
    public void safeWins() throws Exception {

        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        SNALSARDestinationStrategy strategy = factory.apply(model);
        SeaTile option1 = mock(SeaTile.class);
        SeaTile option2 = mock(SeaTile.class);
        ArrayList<SeaTile> options = Lists.newArrayList(option1, option2);
        FeatureExtractors extractors = defaultSetup(model, strategy,options);

        HashMap<SeaTile,Double> safety = new HashMap<>();


        safety.put(option1,1d);
        safety.put(option2,-1d);
        when(extractors.extractFeature(
                eq(SNALSARutilities.SAFE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(safety);

        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for(int i=0; i<100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class));
            if(strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                assertEquals(strategy.getFavoriteSpot(),(option2));
                timesOption2WasChosen++;
            }

        }
        assertTrue(timesOption1WasChosen==100);
        assertTrue(timesOption2WasChosen==0);

    }




    @Test
    public void sociallyAcceptable() throws Exception {

        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        SNALSARDestinationStrategy strategy = factory.apply(model);
        SeaTile option1 = mock(SeaTile.class);
        SeaTile option2 = mock(SeaTile.class);
        SeaTile option3 = mock(SeaTile.class);
        ArrayList<SeaTile> options = Lists.newArrayList(option1, option2,option3);
        FeatureExtractors extractors = defaultSetup(model, strategy,options);

        //option 3 is unsafe
        HashMap<SeaTile,Double> safety = new HashMap<>();
        safety.put(option1,1d);
        safety.put(option2,1d);
        safety.put(option3,-1d);
        when(extractors.extractFeature(
                eq(SNALSARutilities.SAFE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(safety);

        //option 1 is socially unacceptable
        HashMap<SeaTile,Double> acceptable = new HashMap<>();
        acceptable.put(option1,-1d);
        acceptable.put(option2,1d);
        acceptable.put(option3,1d);
        when(extractors.extractFeature(
                eq(SNALSARutilities.SOCIALLY_APPROPRIATE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(acceptable);


        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for(int i=0; i<100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class));
            if(strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                assertEquals(strategy.getFavoriteSpot(),(option2));
                timesOption2WasChosen++;
            }

        }
        assertTrue(timesOption1WasChosen==0);
        assertTrue(timesOption2WasChosen==100);

    }

    @Test
    public void avoidFailures() throws Exception {

        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        SNALSARDestinationStrategy strategy = factory.apply(model);

        SeaTile option1 = mock(SeaTile.class);
        SeaTile option2 = mock(SeaTile.class);
        ArrayList<SeaTile> options = Lists.newArrayList(option1, option2);

        FeatureExtractors extractors = defaultSetup(model, strategy,options);

        //threshold profits are 10
        HashMap<SeaTile,Double> profits = new HashMap<>();


        profits.put(option1,5d);
        profits.put(option2,11d);
        when(extractors.extractFeature(
                eq(SNALSARutilities.PROFIT_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(profits);

        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for(int i=0; i<100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class));
            if(strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                assertEquals(strategy.getFavoriteSpot(),(option2));
                timesOption2WasChosen++;
            }

        }
        assertTrue(timesOption1WasChosen==0);
        assertTrue(timesOption2WasChosen==100);

    }




    //creates an ALL PASS extractor
    public FeatureExtractors defaultSetup(FishState model,
                                          SNALSARDestinationStrategy strategy,
                                          List<SeaTile> options) {
        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);

        strategy.start(model,fisher);
        FeatureExtractors<SeaTile> extractors = mock(FeatureExtractors.class);
        when(fisher.getTileRepresentation()).thenReturn(extractors);
        //all safe profitable and legal
        when(extractors.extractFeature(
                eq(SNALSARutilities.SAFE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(1d, options));
        when(extractors.extractFeature(
                eq(SNALSARutilities.SOCIALLY_APPROPRIATE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(1d,options ));
        when(extractors.extractFeature(
                eq(SNALSARutilities.LEGAL_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(1d, options));
        //everything good on the profits side
        when(extractors.extractFeature(
                eq(SNALSARutilities.PROFIT_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(100d,options ));
        when(extractors.extractFeature(
                eq(SNALSARutilities.FAILURE_THRESHOLD),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(10d,options ));
        when(extractors.extractFeature(
                eq(SNALSARutilities.ACCEPTABLE_THRESHOLD),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(50d,options ));

        return extractors;
    }
}