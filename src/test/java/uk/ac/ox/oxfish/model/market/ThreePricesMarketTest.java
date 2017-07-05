package uk.ac.ox.oxfish.model.market;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 7/4/17.
 */
public class ThreePricesMarketTest {


    @Test
    public void sellsAndNotifiesCorrectly() throws Exception {

        //set up copied from the holdsize test
        Species first = mock(Species.class);
        when(first.getIndex()).thenReturn(0);
        Species second = mock(Species.class);
        when(second.getIndex()).thenReturn(1);

        GlobalBiology bio = new GlobalBiology(first, second);

        when(first.getMaxAge()).thenReturn(2);
        when(second.getMaxAge()).thenReturn(1);

        when(first.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d,100d
                )
        );
        when(first.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d,100d
                )
        );


        when(second.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );
        when(second.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );

        Hold hold = new Hold(1000d,
                             bio);

        //you catch 1000kg of species 2
        hold.load(
                new Catch(
                        new int[]{0,2,3},
                        new int[]{5,0,0},
                        first,
                        bio

                )
        );


        ThreePricesMarket market = new ThreePricesMarket(
                0,1,
                10,
                20,
                30
        );
        market.start(mock(FishState.class));
        Regulation regulation = mock(Regulation.class);
        when(regulation.maximumBiomassSellable(any(),
                                               any(),
                                               any())).thenReturn(1500d);

        //sell the fish
        Fisher fisher = mock(Fisher.class);
        market.sellFish(hold,
                        fisher,
                        regulation,
                        mock(FishState.class),
                        first);
        verify(fisher).earn(
                (10*5*100d+20*2*100d+30*3*100d)

        );




    }

    @Test
    public void regulationHalves() throws Exception {

        //set up copied from the holdsize test
        Species first = mock(Species.class);
        when(first.getIndex()).thenReturn(0);
        Species second = mock(Species.class);
        when(second.getIndex()).thenReturn(1);

        GlobalBiology bio = new GlobalBiology(first, second);

        when(first.getMaxAge()).thenReturn(2);
        when(second.getMaxAge()).thenReturn(1);

        when(first.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d,100d
                )
        );
        when(first.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d,100d
                )
        );


        when(second.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );
        when(second.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );

        Hold hold = new Hold(1000d,
                             bio);

        //you catch 1000kg of species 2
        //but regulations will only allow to sell 500kg
        hold.load(
                new Catch(
                        new int[]{0,2,3},
                        new int[]{5,0,0},
                        first,
                        bio

                )
        );


        ThreePricesMarket market = new ThreePricesMarket(
                0,1,
                10,
                20,
                30
        );
        market.start(mock(FishState.class));
        Regulation regulation = mock(Regulation.class);
        when(regulation.maximumBiomassSellable(any(),
                                               any(),
                                               any())).thenReturn(500d);

        //sell the fish
        Fisher fisher = mock(Fisher.class);
        market.sellFish(hold,
                        fisher,
                        regulation,
                        mock(FishState.class),
                        first);
        verify(fisher).earn(
                //you only sold half of the total value
                (10*5*100d+20*2*100d+30*3*100d)/2d

        );




    }
}