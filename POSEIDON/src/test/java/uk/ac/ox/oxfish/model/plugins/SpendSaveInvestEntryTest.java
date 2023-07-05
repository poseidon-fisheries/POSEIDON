package uk.ac.ox.oxfish.model.plugins;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import static org.mockito.Mockito.*;

public class SpendSaveInvestEntryTest {


    @Test
    public void spendSaveInvest() {

        final SpendSaveInvestEntry spendSaveInvestEntry = new SpendSaveInvestEntry(
            100,
            10,
            "population0"
        );
        //there are 2 fishers; one has 150$ and the other 100;
        //after expenditure there ought to be 140 and 90
        //only one new fisher!
        final Fisher first = mock(Fisher.class);
        when(first.getBankBalance()).thenReturn(150d);
        when(first.getTagsList()).thenReturn(Lists.newArrayList("population0"));
        when(first.hasBeenActiveThisYear()).thenReturn(true);

        final Fisher second = mock(Fisher.class);
        when(second.getBankBalance()).thenReturn(100d);
        when(second.getTagsList()).thenReturn(Lists.newArrayList("population0"));
        when(second.hasBeenActiveThisYear()).thenReturn(true);


        final FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(state.getFishers()).thenReturn(
            ObservableList.observableList(
                first, second));
        spendSaveInvestEntry.step(state);

        verify(first, times(1)).spendExogenously(10d);
        verify(first, times(1)).spendExogenously(100d);
        verify(second, times(1)).spendExogenously(10d);
        verify(second, never()).spendExogenously(100d);


        verify(
            state,
            times(1)
        ).createFisher("population0");


    }
}