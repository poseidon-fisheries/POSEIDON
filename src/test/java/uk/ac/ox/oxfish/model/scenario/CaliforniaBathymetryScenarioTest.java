package uk.ac.ox.oxfish.model.scenario;

import com.esotericsoftware.minlog.Log;
import com.vividsolutions.jts.geom.Coordinate;
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



        //Morro Bay: Easting: 695337, Northing: 3915757.7
        //San Francisco: 553454.67, 4178621.91
        SeaTile morro = model.getMap().getSeaTile(new Coordinate(695337, 3915757.7));
        SeaTile sf = model.getMap().getSeaTile(new Coordinate(553454.67, 4178621.91));
        double km = model.getMap().distance(morro, sf);
        Log.info("the distance between Morro Bay and San Francisco is about 300km, the distance calculator thinks it is "+ km);
        assertEquals(km,300,10);



        morro = model.getMap().getSeaTile(29,104);
        for(int i=0; i<10; i++)
        {
            Log.info("Distance " + i + " cells away from Morro Bay is " +
                             model.getMap().distance(morro,model.getMap().getSeaTile(29-i,104)));
        }
    }
}