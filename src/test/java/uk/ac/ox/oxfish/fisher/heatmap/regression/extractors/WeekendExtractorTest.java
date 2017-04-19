package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 4/19/17.
 */
public class WeekendExtractorTest {


    @Test
    public void isItTheWeekend() throws Exception {


        FishState model = mock(FishState.class);
        when(model.getStepsPerDay()).thenReturn(2);
        when(model.getHoursPerStep()).thenReturn(12d);
        WeekendExtractor extractor = new WeekendExtractor();
        //no:
        assertEquals(extractor.extract(null,12d,null,model),0,.0001);
        assertEquals(extractor.extract(null,12d+24,null,model),0,.0001);
        assertEquals(extractor.extract(null,12d+24*2,null,model),0,.0001);
        assertEquals(extractor.extract(null,12d+24*3,null,model),0,.0001);
        assertEquals(extractor.extract(null,12d+24*4,null,model),0,.0001);


        //yes:
        assertEquals(extractor.extract(null,12d+24*5,null,model),1,.0001);
        assertEquals(extractor.extract(null,12d+24*6,null,model),1,.0001);

    }
}