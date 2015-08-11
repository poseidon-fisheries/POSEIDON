package uk.ac.ox.oxfish.model.market;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class FixedPriceMarketTest {


    @Test
    public void transaction() throws Exception {

        FixedPriceMarket market = new FixedPriceMarket(2.0);

        Fisher seller = mock(Fisher.class);

        market.sellFishImplementation(100.0,seller,new Anarchy(),mock(FishState.class),mock(Specie.class));
        verify(seller).earn(200.0);

    }

    @Test
    public void regTransaction() throws Exception {

        FixedPriceMarket market = new FixedPriceMarket(2.0);

        Fisher seller = mock(Fisher.class);
        Regulation regulation = mock(Regulation.class);
        when(regulation.maximumBiomassSellable(any(),any(),any())).thenReturn(50.0); //can only sell 50!


        market.sellFishImplementation(100.0,seller, regulation,mock(FishState.class),mock(Specie.class));
        verify(seller).earn(100.0);

    }
}