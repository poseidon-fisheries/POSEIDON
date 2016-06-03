package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 5/31/16.
 */
public class NoFriendsHereExtractorTest {


    @Test
    public void doNotGoWhereYourFriendIs() throws Exception {

        SeaTile empty = mock(SeaTile.class);
        SeaTile full = mock(SeaTile.class);
        Fisher friend = mock(Fisher.class);
        TripRecord record = mock(TripRecord.class);
        when(record.getTilesFished()).thenReturn(
                Sets.newHashSet(full)
        );
        when(friend.getLastFinishedTrip()).thenReturn(record);

        NoFriendsHereExtractor extractor =
                new NoFriendsHereExtractor(true);

        Fisher chooser = mock(Fisher.class);
        when(chooser.getDirectedFriends()).thenReturn(
                Lists.newArrayList(friend));

        List<SeaTile> options =Lists.newArrayList(empty,full);
        Map<SeaTile, Double> featureMap = extractor.extractFeature(
                options,
                mock(FishState.class),
                chooser
        );

        Assert.assertEquals(featureMap.size(),2);
        Assert.assertEquals(featureMap.get(empty),1,.0001);
        Assert.assertEquals(featureMap.get(full),-1,.0001);


    }
}