package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TemporaryRegulationTest {

    @Test public void isActiveWhenStartLessThanEnd() {
        isActiveTest(10, 20, ImmutableMap.of(
            1, false,
            10, true,
            15, true,
            20, true,
            365, false
        ));
    }

    @Test public void isActiveWhenEndLessThanStart() {
        isActiveTest(20, 10, ImmutableMap.of(
            1, true,
            10, true,
            15, false,
            20, true,
            365, true
        ));
    }

    @Test public void isActiveWhenStartEqualsEnd() {
        isActiveTest(20, 20, ImmutableMap.of(
            1, false,
            10, false,
            15, false,
            20, true,
            365, false
        ));
    }

    private void isActiveTest(int startDay, int endDay, ImmutableMap<Integer, Boolean> cases) {
        final TemporaryRegulation temporaryRegulation =
            new TemporaryRegulation(startDay, endDay, mock(Regulation.class));
        final FishState fishState = mock(FishState.class);
        temporaryRegulation.start(fishState, mock(Fisher.class));
        cases.forEach((day, expected) -> {
            assertEquals("on day " + day, expected, temporaryRegulation.isActive(day));
        });
    }

    @Test
    public void canFishHere() {

        final Fisher fisher = mock(Fisher.class);
        final SeaTile tile = mock(SeaTile.class);
        when(tile.isProtected()).thenReturn(true);
        final FishState state = mock(FishState.class);
        when(state.getDayOfTheYear()).thenReturn(100);
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

}