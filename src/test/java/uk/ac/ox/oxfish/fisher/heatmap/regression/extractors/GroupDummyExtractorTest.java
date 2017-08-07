package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import org.junit.Test;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 8/7/17.
 */
public class GroupDummyExtractorTest {


    @Test
    public void dummies() throws Exception {

        MapDiscretization discretization = mock(MapDiscretization.class);
        GroupDummyExtractor extractor = new GroupDummyExtractor(
                2,
                discretization
        );

        SeaTile tile = mock(SeaTile.class);
        when(discretization.getGroup(tile)).thenReturn(2);
        assertEquals(extractor.extract(
                tile,0,null,null
        ),1,.001);

        when(discretization.getGroup(tile)).thenReturn(0);
        assertEquals(extractor.extract(
                tile,0,null,null
        ),0,.001);
    }

}