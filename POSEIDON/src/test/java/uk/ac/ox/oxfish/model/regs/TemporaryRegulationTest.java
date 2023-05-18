package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TemporaryRegulationTest {

    @Test
    public void isActiveWhenStartLessThanEnd() {
        isActiveTest(10, 20, ImmutableMap.of(
            1, false,
            10, true,
            15, true,
            20, true,
            365, false
        ));
    }

    private void isActiveTest(int startDay, int endDay, ImmutableMap<Integer, Boolean> cases) {
        final TemporaryRegulation temporaryRegulation =
            new TemporaryRegulation(startDay, endDay, mock(Regulation.class));
        cases.forEach((day, expected) ->
            assertEquals("on day " + day, expected, temporaryRegulation.isActive(day))
        );
    }

    @Test
    public void isActiveWhenEndLessThanStart() {
        isActiveTest(20, 10, ImmutableMap.of(
            1, true,
            10, true,
            15, false,
            20, true,
            365, true
        ));
    }

    @Test
    public void isActiveWhenStartEqualsEnd() {
        isActiveTest(20, 20, ImmutableMap.of(
            1, false,
            10, false,
            15, false,
            20, true,
            365, false
        ));
    }

    @Test
    public void canFishHere() {

        final Fisher fisher = mock(Fisher.class);
        final SeaTile tile = mock(SeaTile.class);
        when(tile.isProtected()).thenReturn(true);
        final FishState state = mock(FishState.class);
        when(state.getDayOfTheYear(anyInt())).thenReturn(100);
        final ProtectedAreasOnly protectedAreasOnly = new ProtectedAreasOnly();

        ImmutableMap.of(
            new TemporaryRegulation(10, 300, protectedAreasOnly), false,
            new TemporaryRegulation(100, 100, protectedAreasOnly), false,
            new TemporaryRegulation(10, 30, protectedAreasOnly), true,
            new TemporaryRegulation(150, 300, protectedAreasOnly), true
        ).forEach((reg, expected) -> {
            reg.start(state, fisher);
            assertEquals(reg.canFishHere(fisher, tile, state), expected);
        });
    }


    @Test
    public void doubleDelegate() {
        //check that the right policy is active at the right time

        //
        final FishState state = mock(FishState.class);
        when(state.getDayOfTheYear(anyInt())).thenReturn(0);
        //active regulation mean you can't go out
        final Regulation active = mock(Regulation.class);
        when(active.allowedAtSea(any(), any())).thenReturn(false);
        //inactive regulation means you can go out
        final Regulation inactive = mock(Regulation.class);
        when(active.allowedAtSea(any(), any())).thenReturn(true);

        TemporaryRegulationFactory factory =
            new TemporaryRegulationFactory(
                100, 200,
                fishState -> active
            );
        factory.setInactiveDelegate(fishState -> inactive);

        final TemporaryRegulation regulation = factory.apply(state);

        //day 10 :  allowed at sea
        final Fisher fisher = mock(Fisher.class);
        when(state.getDayOfTheYear(anyInt())).thenReturn(10);
        assertTrue(regulation.allowedAtSea(fisher, state));

        //day 150: not allowed at sea
        when(state.getDayOfTheYear(anyInt())).thenReturn(150);
        assertTrue(!regulation.allowedAtSea(fisher, state));


        //day 250: allowed at sea
        when(state.getDayOfTheYear(anyInt())).thenReturn(250);
        assertTrue(regulation.allowedAtSea(fisher, state));

    }
}