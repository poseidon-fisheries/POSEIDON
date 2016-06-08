package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import javafx.collections.FXCollections;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.NobodyFishesHereExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 6/7/16.
 */
public class NobodyFishesHereExtractorTest {

    @Test
    public void WorksAsIntended() throws Exception {

        SeaTile good = mock(SeaTile.class);
        SeaTile bad1 = mock(SeaTile.class);
        SeaTile bad2 = mock(SeaTile.class);

        Fisher dude1 = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Fisher dude2 = mock(Fisher.class,RETURNS_DEEP_STUBS);
        when(dude1.getLastFinishedTrip().getTilesFished()).thenReturn(Sets.newHashSet(bad1));
        when(dude2.getLastFinishedTrip().getTilesFished()).thenReturn(Sets.newHashSet(bad2));

        FishState model = mock(FishState.class);
        when(model.getFishers()).thenReturn(FXCollections.observableArrayList(Lists.newArrayList(dude1, dude2)));

        NobodyFishesHereExtractor extractor = new NobodyFishesHereExtractor();
        Map<SeaTile, Double> map = extractor.extractFeature(
                Lists.newArrayList(good, bad1, bad2),
                model,
                mock(Fisher.class));
        assertEquals(map.size(),3);
        assertEquals(map.get(good),1,.001);
        assertEquals(map.get(bad1),-1,.001);
        assertEquals(map.get(bad2),-1,.001);

    }
}