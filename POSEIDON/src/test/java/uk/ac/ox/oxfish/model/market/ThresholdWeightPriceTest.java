package uk.ac.ox.oxfish.model.market;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ThresholdWeightPriceTest {


    @Test
    public void sellsAndNotifiesCorrectly() {

        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{10, 50, 100}, 2);
        Meristics second = new FromListMeristics(new double[]{100, 200}, 2);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second", second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);


        Hold hold = new Hold(
            1000d,
            bio
        );

        //you catch 1000kg of species 1
        hold.load(
            new Catch(
                new double[]{0, 2, 3}, // 100 + 300
                new double[]{5, 0, 0}, // 500!
                firstSpecies,
                bio

            )
        );


        FlexibleAbundanceMarket market = new FlexibleAbundanceMarket(
            new ThresholdWeightPrice(30, 20, 100)
        );

        market.setSpecies(firstSpecies);
        market.start(mock(FishState.class));
        Regulation regulation = mock(Regulation.class);
        when(regulation.maximumBiomassSellable(
            any(),
            any(),
            any()
        )).thenReturn(1500d);

        //sell the fish
        Fisher fisher = mock(Fisher.class);
        market.sellFish(
            hold,
            fisher,
            regulation,
            mock(FishState.class),
            firstSpecies
        );
        verify(fisher).earn(
            (20 * 5 * 10 + 20 * 2 * 50 + 30 * 3 * 100)

        );


    }
}