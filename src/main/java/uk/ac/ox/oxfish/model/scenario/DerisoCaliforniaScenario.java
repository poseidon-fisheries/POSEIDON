package uk.ac.ox.oxfish.model.scenario;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.AllocatedBiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesDerisoInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.event.AbundanceDrivenFixedExogenousCatches;
import uk.ac.ox.oxfish.model.event.BiomassDrivenFixedExogenousCatches;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.gas.FixedGasFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by carrknight on 6/19/17.
 */
public class DerisoCaliforniaScenario extends CaliforniaAbstractScenario {



    private MultipleSpeciesDerisoInitializer initializer;

    private HashMap<String,String> movement = new HashMap<>();
    {
        movement.put("Sablefish","0.0001");
    }

    /**
     * build the biology part!
     *
     * @param model
     * @param folderMap
     * @return
     */
    @Override
    protected GlobalBiology buildBiology(
            FishState model, LinkedHashMap<String, Path> folderMap) {
        initializer = new MultipleSpeciesDerisoInitializer(folderMap,true);



        GlobalBiology biology = initializer.generateGlobal(model.getRandom(),
                                             model);

        HashMap<Species,Double>  recast = new HashMap<>();
        for (Map.Entry<String, String> exogenous : movement.entrySet()) {
            recast.put(biology.getSpecie(exogenous.getKey()),Double.parseDouble(exogenous.getValue()));
        }
        initializer.setMovementRate(recast);

        return biology;
    }

    @Override
    public AllocatedBiologyInitializer getBiologyInitializer() {
        return initializer;
    }

    @NotNull
    @Override
    protected ExogenousCatches turnIntoExogenousCatchesObject(
            HashMap<Species, Double> recast) {

            return new BiomassDrivenFixedExogenousCatches(recast);

    }


    {

        super.setMainDirectory(Paths.get("inputs","simple_california"));
        // https://dataexplorer.northwestscience.fisheries.noaa.gov/fisheye/PerformanceMetrics/
        //median variable cost per day in  in 2009 was  2,789$  to which we remove the fuel costs of 546
        //that makes for 93.46$ dollars in 2009 which is deflated to 82.29 for 2004
        //deflated at this website http://www.usinflationcalculator.com/
        //because i lost all arithmetic abilities at the age of 16
        super.setHourlyTravellingCosts(new FixedDoubleParameter((2789d-546d)/24d));

        //average gasoline prices in california was 2.166 gallons an hour in 2004
        super.setGasPriceMaker(new FixedGasFactory(0.476453195));

        //prices are just annual revenues/landings in the noaa data-set for 2004
        super.setPriceMap(
                "Dover Sole:0.772359928,Sablefish:3.391484698" +
                       /// ",Shortspine Thornyhead:xxx,Longspine Thornyhead:xxx" +
                        ",Yelloweye Rockfish:2.1262"
                        //this one I just deflated
                        +"," + MultipleSpeciesAbundanceInitializer.FAKE_SPECIES_NAME+":1.48"
        );

        //use 2009 port data (pre-itq but post-buyback)
        super.setPortFileName("dts_ports_2009.csv");
        super.setUsePremadeInput(false);
    }

    /**
     * Getter for property 'movement'.
     *
     * @return Value for property 'movement'.
     */
    public HashMap<String, String> getMovement() {
        return movement;
    }

    /**
     * Setter for property 'movement'.
     *
     * @param movement Value to set for property 'movement'.
     */
    public void setMovement(HashMap<String, String> movement) {
        this.movement = movement;
    }
}
