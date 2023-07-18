package uk.ac.ox.oxfish.model.market;

import org.junit.jupiter.api.Test;
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

public class WeightLimitMarketTest {


    @Test
    public void sellsAndNotifiesCorrectly() {

        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{100, 200, 300}, 2);
        Species firstSpecies = new Species("first", first);


        GlobalBiology bio = new GlobalBiology(firstSpecies);


        Hold hold = new Hold(
            500000d,
            bio
        );

        //you catch 1000kg of species 1
        hold.load(
            new Catch(
                new double[]{0, 2, 3},
                new double[]{5, 0, 0},
                firstSpecies,
                bio

            )
        );


        WeightLimitMarket market = new WeightLimitMarket(
            1,
            2,
            250
        );
        market.setSpecies(firstSpecies);
        market.start(mock(FishState.class));
        Regulation regulation = mock(Regulation.class);
        when(regulation.maximumBiomassSellable(
            any(),
            any(),
            any()
        )).thenReturn(150000d);

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
            (1 * 5 * 100d + 1 * 2 * 200d + 2 * 3 * 300d)

        );


    }
}