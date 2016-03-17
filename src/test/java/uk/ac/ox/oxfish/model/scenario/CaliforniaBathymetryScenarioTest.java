package uk.ac.ox.oxfish.model.scenario;

import com.esotericsoftware.minlog.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 3/17/16.
 */
public class CaliforniaBathymetryScenarioTest {


    @Test
    public void readsTheRightAmountOfShortspineBiomass() throws Exception {

        double target = 274210064.370047;
        CaliforniaBathymetryScenario scenario = new CaliforniaBathymetryScenario();
        FishState model = new FishState(System.currentTimeMillis());
        model.setScenario(scenario);
        model.start();
        List<SeaTile> tiles = model.getMap().getAllSeaTilesAsList();
        double totalShortspineBiomass = 0;
        Species shortSpine = model.getBiology().getSpecie("Shortspine Thornyhead");
        for(SeaTile tile : tiles)
        {
            totalShortspineBiomass += tile.getBiomass(shortSpine);
        }

        //different by at most 5%
        Log.info("From data we know that the shortspine biomass is " + target + ", after many transformations our" +
                "model has " + totalShortspineBiomass + " in the model  ");
        Log.info("They differ by " + 100*Math.abs(totalShortspineBiomass-target)/target + "% the maximum allowed is 5%");
        assertEquals(totalShortspineBiomass,target,target*.05);


    }
}