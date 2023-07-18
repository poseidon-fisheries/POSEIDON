package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;

import java.util.Map;

import static org.junit.Assert.*;
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

    private void isActiveTest(final int startDay, final int endDay, final Map<Integer, Boolean> cases) {
        final TemporaryRegulation temporaryRegulation =
            new TemporaryRegulation(new NoFishing(), startDay, endDay);
        cases.forEach((day, expected) ->
            assertEquals("on day " + day, expected, temporaryRegulation.appliesOn(day))
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
        when(fisher.grabState()).thenReturn(state);
        when(state.getDayOfTheYear(anyInt())).thenReturn(100);
        final ProtectedAreasOnly protectedAreasOnly = new ProtectedAreasOnly();

        ImmutableMap.of(
            new TemporaryRegulation(protectedAreasOnly, 10, 300), false,
            new TemporaryRegulation(protectedAreasOnly, 100, 100), false,
            new TemporaryRegulation(protectedAreasOnly, 10, 30), true,
            new TemporaryRegulation(protectedAreasOnly, 150, 300), true
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

        final TemporaryRegulationFactory factory =
            new TemporaryRegulationFactory(
                fishState -> active, 100, 200
            );

        final TemporaryRegulation regulation = factory.apply(state);

        //day 10 :  allowed at sea
        final Fisher fisher = mock(Fisher.class);
        when(fisher.grabState()).thenReturn(state);
        when(state.getDayOfTheYear(anyInt())).thenReturn(10);
        assertTrue(regulation.allowedAtSea(fisher, state));

        //day 150: not allowed at sea
        when(state.getDayOfTheYear(anyInt())).thenReturn(150);
        assertFalse(regulation.allowedAtSea(fisher, state));


        //day 250: allowed at sea
        when(state.getDayOfTheYear(anyInt())).thenReturn(250);
        assertTrue(regulation.allowedAtSea(fisher, state));

    }
}