package uk.ac.ox.oxfish.model.market;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.Regulations;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class FixedPriceMarketTest {


    @Test
    public void transaction() throws Exception {

        FixedPriceMarket market = new FixedPriceMarket(new Specie("pippo"),2.0);

        Fisher seller = mock(Fisher.class);

        market.sellFishImplementation(100.0,seller,new Anarchy(),mock(FishState.class));
        verify(seller).earn(200.0);

    }

    @Test
    public void regTransaction() throws Exception {

        FixedPriceMarket market = new FixedPriceMarket(new Specie("pippo"),2.0);

        Fisher seller = mock(Fisher.class);
        Regulations regulations = mock(Regulations.class);
        when(regulations.maximumBiomassSellable(any(),any(),any())).thenReturn(50.0); //can only sell 50!


        market.sellFishImplementation(100.0,seller, regulations,mock(FishState.class));
        verify(seller).earn(100.0);

    }
}