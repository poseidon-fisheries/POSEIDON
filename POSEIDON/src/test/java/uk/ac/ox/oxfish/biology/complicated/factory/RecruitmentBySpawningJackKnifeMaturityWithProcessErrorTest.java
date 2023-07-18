package uk.ac.ox.oxfish.biology.complicated.factory;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import java.util.DoubleSummaryStatistics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecruitmentBySpawningJackKnifeMaturityWithProcessErrorTest {


    @Test
    public void lognormalIsCorrectlyDistributed() {


        final FishState mocked = mock(FishState.class);
        when(mocked.getRandom()).thenReturn(new MersenneTwisterFast());
        RecruitmentBySpawningJackKnifeMaturityWithProcessError.YearAwareLogNormalNoiseMaker maker =
            new RecruitmentBySpawningJackKnifeMaturityWithProcessError.YearAwareLogNormalNoiseMaker(
                5,
                .4,
                mocked
            );

        //pre-year it will return always 0
        when(mocked.getYear()).thenReturn(3);
        for (int attempts = 0; attempts < 10; attempts++) {
            Assertions.assertEquals(maker.get(), 0.0, .0001);
        }

        when(mocked.getYear()).thenReturn(10);
        DoubleSummaryStatistics collectorOfSamples = new DoubleSummaryStatistics();
        for (int attempts = 0; attempts < 100000; attempts++) {
            collectorOfSamples.accept(1 + maker.get());
        }
        final double averageObserved = collectorOfSamples.getAverage();
        System.out.println(averageObserved);
        //this is a bit tricky, but according to R it's unlikely to go past these values...
        Assertions.assertTrue(averageObserved >= 0.99);
        Assertions.assertTrue(averageObserved <= 1.01);

    }
}