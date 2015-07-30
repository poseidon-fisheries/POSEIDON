package uk.ac.ox.oxfish.experiments;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.scenario.OsmosePrototype;

import java.io.File;

/**
 * Gulf of mexico Osmose requires a long burn-in process. Here we burn-in 1000 simulations so we can grab one of these
 * and start our ABM immediately
 * Created by carrknight on 7/3/15.
 */
public class PreBurnOSMOSE {


    /**
     * generated 1000 random starting point for the gulf of mexico setup
     */
    public static void WFS(String[] args)
    {


        //because i am planning on running this once I am just going to hard-code this in.
        String configurationLocation =  "/home/carrknight/code/config_OSMOSE-WFS_v3u2/osm_all-parameters.csv";
        final String outputDir = "/home/carrknight/code/config_OSMOSE-WFS_v3u2/output/restart/";
        String output = outputDir + "osm_snapshot_step1379.nc.0";

        for(int i=0; i<1000; i++)
        {
            OsmosePrototype scenario = new OsmosePrototype();
            scenario.setFishers(0); //no fishers
            scenario.setNetworkBuilder(new EmptyNetworkBuilder()); //no social network
            scenario.setBuninLength(115*12);
            scenario.setOsmoseConfigurationFile(configurationLocation);
            FishState state = new FishState(i);
            state.setScenario(scenario);
            state.start();

            File file = new File(output);
            File renamedTo = new File(outputDir + "start" + i + ".nc");
            Preconditions.checkState(file.exists());
            Preconditions.checkState(!renamedTo.exists());
            final boolean renamedCorrectly = file.renameTo(renamedTo);
            Preconditions.checkState(!file.exists());
            Preconditions.checkState(renamedTo.exists());
            Preconditions.checkState(renamedCorrectly);


        }


    }
    /**
     * generated 1000 random starting point for the default OSMOSE scenario
     */
    public static void main(String[] args)
    {


        //because i am planning on running this once I am just going to hard-code this in.
        String configurationLocation =  "/home/carrknight/code/osmose-v3u2_src/config/burnin.csv";
        final String outputDir = "/home/carrknight/code/osmose-v3u2_src/config/output/restart/";
        String output = outputDir + "osm_snapshot_step2799.nc.0";

        for(int i=0; i<1000; i++)
        {
            OsmosePrototype scenario = new OsmosePrototype();
            scenario.setFishers(0); //no fishers
            scenario.setNetworkBuilder(new EmptyNetworkBuilder()); //no social network
            scenario.setBuninLength(114*25);
            scenario.setOsmoseConfigurationFile(configurationLocation);
            FishState state = new FishState(i);
            state.setScenario(scenario);
            state.start();

            File file = new File(output);
            System.out.println(output);
            File renamedTo = new File(outputDir + "start" + i + ".nc");
            Preconditions.checkState(file.exists());
            Preconditions.checkState(!renamedTo.exists());
            final boolean renamedCorrectly = file.renameTo(renamedTo);
            Preconditions.checkState(!file.exists());
            Preconditions.checkState(renamedTo.exists());
            Preconditions.checkState(renamedCorrectly);


        }


    }

}
