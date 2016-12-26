package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.behavior.functionapproximation.dense.NormalizedVariableFeatures;
import burlap.mdp.core.state.vardomain.VariableDomain;
import com.esotericsoftware.minlog.Log;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by carrknight on 12/25/16.
 */
public class BurlapQuota {


    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {


        Log.set(Log.LEVEL_INFO);

        Path containerPath = Paths.get("runs", "burlap_quota");
        containerPath.toFile().mkdirs();
        containerPath.resolve("data").toFile().mkdirs();

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(300);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setPortPositionX(40);
        scenario.setPortPositionY(25);
        scenario.setMapInitializer(mapInitializer);



        //imagine a 10% yearly interest rate this means that yearly the discount rate is:
        // 1/(1+.1/12)
        //which is about 99%
        NormalizedVariableFeatures features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.BIOMASS, new VariableDomain(0, 10500000)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 365)).
                        variableDomain(ShodanStateOil.MONTHS_LEFT, new VariableDomain(0, 243));


        /*
        BurlapShodan.sarsaRunNormalized(.99,
                                        "99_sarsa_biomass2",
                                        1,
                                        .01,
                                        .3,
                                        features,
                                        scenario,
                                        containerPath, (Steppable) simState -> {},
                                        ShodanStateOil.BIOMASS,
                                        ShodanStateOil.MONTHS_LEFT);
*/
        BurlapShodan.sarsaRunNormalized(.99,
                                        "99_sarsa_biomass_highlambda",
                                        1,
                                        .01,
                                        .85,
                                        features,
                                        scenario,
                                        containerPath, (Steppable) simState -> {},
                                        ShodanStateOil.BIOMASS,
                                        ShodanStateOil.MONTHS_LEFT);
        /*
        //try 90% just to see the difference
        BurlapShodan.sarsaRunNormalized(.90,
                                        "90_sarsa_biomass2",
                                        1,
                                        .01,
                                        .3,
                                        features,
                                         scenario,
                                        containerPath, (Steppable) simState -> {},
                                        ShodanStateOil.BIOMASS,
                                        ShodanStateOil.MONTHS_LEFT);


*/
    }
}