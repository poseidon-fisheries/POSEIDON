package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.ConstantLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DelayGearDecoratorTest {


    @Test
    public void tenHourDelay() {


        ConstantLocalBiology biology = new ConstantLocalBiology(1000);
        FixedProportionGear delegate = spy(new FixedProportionGear(.5));
        DelayGearDecorator gear = new DelayGearDecorator(
                delegate,
                10
        );

        for(int hour=0; hour<9; hour++) {
            Catch catchMade = gear.fish(
                    mock(Fisher.class),
                    biology,
                    mock(SeaTile.class),
                    1,
                    new GlobalBiology(new Species("fake"))
            );
            //should not have called the original proportion gear at all
            verify(delegate,never()).fish(any(),any(),any(),anyInt(),any());
            assertEquals(catchMade.getTotalWeight(),0,.001);
        }

        //10th hour it should catch!
        Catch catchMade = gear.fish(
                mock(Fisher.class),
                biology,
                mock(SeaTile.class),
                1,
                new GlobalBiology(new Species("fake"))
        );
        //should not have called the original proportion gear at all
        verify(delegate,times(1)).fish(any(),any(),any(),anyInt(),any());
        //should have caught 500kg!
        assertEquals(catchMade.getTotalWeight(),500,.001);
    }

    @Test
    public void fishThreeTimesInTwentyFoursHours() {


        Gear delegate = mock(Gear.class);
        DelayGearDecorator gear = new DelayGearDecorator(
                delegate,
                8
        );

        for(int hour=0; hour<24; hour++) {
            Catch catchMade = gear.fish(
                    mock(Fisher.class),
                    mock(LocalBiology.class),
                    mock(SeaTile.class),
                    1,
                    new GlobalBiology(new Species("fake"))
            );

        }

        verify(delegate,times(3)).fish(any(),any(),any(),anyInt(),any());

    }
}